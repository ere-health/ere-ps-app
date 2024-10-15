package health.ere.ps.service.gematik;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.entity.ContentType;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Task;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDStatusType;
import de.gematik.ws.fa.vsdm.vsd.v5.UCAllgemeineVersicherungsdatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCGeschuetzteVersichertendatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCPersoenlicheVersichertendatenXML;
import de.health.service.cetp.IKonnektorClient;
import de.health.service.cetp.domain.eventservice.Subscription;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.jmx.ReadEPrescriptionsMXBeanImpl;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.fhir.FHIRService;
import health.ere.ps.service.idp.BearerTokenService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.DatatypeConverter;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.ws.Holder;

/* Note: reading, writing and resending of failed rejects are done by one Thread (see scheduledExecutorService), no additional synchronization for retrying reject is need */
@ApplicationScoped
public class PharmacyService implements AutoCloseable {

    private static final Logger log = Logger.getLogger(PharmacyService.class.getName());
    static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    private static final String FAILED_REJECTS_FILE = "dangling-e-prescriptions.dat";

    @Inject
    AppConfig appConfig;

    @Inject
    IKonnektorClient konnektorClient;

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;

    @Inject
    ReadEPrescriptionsMXBeanImpl readEPrescriptionsMXBean;

    @Inject
    BearerTokenService bearerTokenService;

    @ConfigProperty(name = "preferred.smcb")
    Optional<String> preferredSmcb;

    /**
     * By default should be the static file.
     * A test case can change this to a temporary file.
     */
    Path failedRejectsFile = Paths.get(FAILED_REJECTS_FILE);

    private static final FhirContext fhirContext = FHIRService.getFhirContext();

    Client client;

    static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    static DocumentBuilder builder;

    static {
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.log(Level.SEVERE, "Could create  parser", e);
        }
    }

    static final JAXBContext jaxbContext = createJaxbContext();

    static JAXBContext createJaxbContext() {
        try {
            return JAXBContext.newInstance(UCPersoenlicheVersichertendatenXML.class,
                    UCAllgemeineVersicherungsdatenXML.class, UCGeschuetzteVersichertendatenXML.class);
        } catch (JAXBException e) {
            log.log(Level.SEVERE, "Could not init jaxb context", e);
            return null;
        }
    }

    @PostConstruct
    public void init() {
        client = ERezeptWorkflowService.initClientWithVAU(appConfig);
        int retryInterval = 1;  //TODO: make configurable
        scheduledExecutorService.scheduleAtFixedRate(this::retryFailedRejects, retryInterval, retryInterval, TimeUnit.MINUTES);
    }

    @PreDestroy
    @Override
    public void close() throws Exception {
        scheduledExecutorService.shutdown();
        var isTerminated = scheduledExecutorService.awaitTermination(1, TimeUnit.MINUTES);
        if (!isTerminated) {
            log.severe("Reject scheduledExecutorService did not shutdown!");
        }
    }

    public Pair<Bundle, String> getEPrescriptionsForCardHandle(
            String correlationId,
            String egkHandle,
            String smcbHandle,
            RuntimeConfig runtimeConfig
    ) throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        if (runtimeConfig == null) {
            runtimeConfig = new RuntimeConfig();
        }
        runtimeConfig.setSMCBHandle(smcbHandle);
        ReadVSDResult readVSD = readVSD(correlationId, egkHandle, smcbHandle, runtimeConfig);
        Holder<byte[]> pruefungsnachweis = readVSD.pruefungsnachweis;
        String pnw = Base64.getEncoder().encodeToString(pruefungsnachweis.value);
        try (Response response = client.target(appConfig.getPrescriptionServiceURL()).path("/Task")
                .queryParam("kvnr", extractKVNR(readVSD))
                .queryParam("pnw", pnw).request()
                .header("Content-Type", "application/fhir+xml")
                .header("User-Agent", appConfig.getUserAgent())
                .header("Authorization", "Bearer " + bearerTokenService.getBearerToken(runtimeConfig))
                .get()) {

            String event = getEvent(factory.newDocumentBuilder(), pruefungsnachweis.value);
            String bundleString = new String(response.readEntity(InputStream.class).readAllBytes(), getCharset(response));

            if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                readEPrescriptionsMXBean.increaseTasksFailed();
                throw new WebApplicationException("Error on " + appConfig.getPrescriptionServiceURL() + " " + bundleString, response.getStatus());
            }
            readEPrescriptionsMXBean.increaseTasks();
            return Pair.of(fhirContext.newXmlParser().parseResource(Bundle.class, bundleString), event);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            log.log(Level.SEVERE, String.format("[%s] Could not read response from Fachdienst", correlationId), e);
            readEPrescriptionsMXBean.increaseTasksFailed();
            throw new WebApplicationException("Could not read response from Fachdienst", e);
        }
    }

    static synchronized String extractKVNR(ReadVSDResult readVSDResult) {
        try {
            Holder<byte[]> pnw = readVSDResult.pruefungsnachweis;
            String decodedXMLFromPNW = new String(new GZIPInputStream(new ByteArrayInputStream(pnw.value)).readAllBytes());
            Document doc = builder.parse(new ByteArrayInputStream(decodedXMLFromPNW.getBytes()));
            String e = doc.getElementsByTagName("E").item(0).getTextContent();
            if (e.equals("3")) {
                InputStream isPersoenlicheVersichertendaten = new GZIPInputStream(
                    new ByteArrayInputStream(readVSDResult.persoenlicheVersichertendaten.value));
                UCPersoenlicheVersichertendatenXML patient = (UCPersoenlicheVersichertendatenXML) jaxbContext
                        .createUnmarshaller().unmarshal(isPersoenlicheVersichertendaten);
                
                String versichertenID = patient.getVersicherter().getVersichertenID();
                log.fine("VSDM result: "+e+" VersichertenID: " + versichertenID);
                return versichertenID;
            } else {
                String pn = doc.getElementsByTagName("PZ").item(0).getTextContent();
                String base64PN = new String(DatatypeConverter.parseBase64Binary(pn));
                String kvnrFromPn = base64PN.substring(0, 10);
                return kvnrFromPn;
            }
        } catch (SAXException | IOException | NullPointerException | JAXBException e) {
            String msg = "Could not parse PNW message";
            log.log(Level.WARNING, msg, e);                    
            return "";
        }
    }

    public ReadVSDResult readVSD(
            String correlationId,
            String egkHandle,
            String smcbHandle,
            RuntimeConfig runtimeConfig
    ) throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
        if ("".equals(context.getUserId()) || context.getUserId() == null) {
            context.setUserId(UUID.randomUUID().toString());
        }

        Holder<byte[]> persoenlicheVersichertendaten = new Holder<>();
        Holder<byte[]> allgemeineVersicherungsdaten = new Holder<>();
        Holder<byte[]> geschuetzteVersichertendaten = new Holder<>();
        Holder<VSDStatusType> vSD_Status = new Holder<>();
        Holder<byte[]> pruefungsnachweis = new Holder<>();

        EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);
        if (egkHandle == null) {
            egkHandle = PrefillPrescriptionService.getFirstCardOfType(eventService, CardTypeType.EGK, context, null);

        }
        if (smcbHandle == null) {
            smcbHandle = setAndGetSMCBHandleForPharmacy(runtimeConfig, context, eventService);
        }
        log.info(egkHandle + " " + smcbHandle);
        VSDServicePortType vsdServicePortType = connectorServicesProvider.getVSDServicePortType(runtimeConfig);
        try {
            String listString;
            try {
                List<Subscription> subscriptions = konnektorClient.getSubscriptions(runtimeConfig);
                listString = subscriptions.stream()
                        .map(s -> String.format("[id=%s eventTo=%s topic=%s]", s.getSubscriptionId(), s.getEventTo(), s.getTopic()))
                        .collect(Collectors.joining(","));
            } catch (Throwable e) {
                String msg = String.format("[%s] Could not get active getSubscriptions", correlationId);
                log.log(Level.SEVERE, msg, e);
                listString = "not available";
            }
            log.info(String.format(
                    "[%s] readVSD for cardHandle=%s, smcbHandle=%s, subscriptions: %s", correlationId, egkHandle, smcbHandle, listString
            ));
            vsdServicePortType.readVSD(
                    egkHandle, smcbHandle, true, true, context,
                    persoenlicheVersichertendaten,
                    allgemeineVersicherungsdaten,
                    geschuetzteVersichertendaten,
                    vSD_Status,
                    pruefungsnachweis
            );
            readEPrescriptionsMXBean.increaseVSDRead();
        } catch (Throwable t) {
            readEPrescriptionsMXBean.increaseVSDFailed();
            throw t;
        }
        ReadVSDResult readVSDResult = new ReadVSDResult();
        readVSDResult.persoenlicheVersichertendaten = persoenlicheVersichertendaten;
        readVSDResult.allgemeineVersicherungsdaten = allgemeineVersicherungsdaten;
        readVSDResult.geschuetzteVersichertendaten = geschuetzteVersichertendaten;
        readVSDResult.vSD_Status = vSD_Status;
        readVSDResult.pruefungsnachweis = pruefungsnachweis;
        return readVSDResult;
    }

    public class ReadVSDResult {
        Holder<byte[]> persoenlicheVersichertendaten;
        Holder<byte[]> allgemeineVersicherungsdaten;
        Holder<byte[]> geschuetzteVersichertendaten;
        Holder<VSDStatusType> vSD_Status;
        Holder<byte[]> pruefungsnachweis;
    }

    private String getEvent(DocumentBuilder builder, byte[] pruefnachweisBytes) throws IOException, SAXException {
        String decodedXMLFromPNW = new String(new GZIPInputStream(new ByteArrayInputStream(pruefnachweisBytes)).readAllBytes());
        Document doc = builder.parse(new ByteArrayInputStream(decodedXMLFromPNW.getBytes()));
        return doc.getElementsByTagName("E").item(0).getTextContent();
    }

    public String setAndGetSMCBHandleForPharmacy(
            RuntimeConfig runtimeConfig,
            ContextType context,
            EventServicePortType eventService
    ) throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        String smcbHandle = PrefillPrescriptionService.getFirstCardOfType(eventService, CardTypeType.SMC_B, context, preferredSmcb.orElse(null));
        runtimeConfig.setSMCBHandle(smcbHandle);
        return smcbHandle;
    }

    public Bundle accept(String correlationId, String token, RuntimeConfig runtimeConfig) {
        String secret = "";
        byte[] data;
        Task task;
        try (Response response = client.target(appConfig.getPrescriptionServiceURL() + token).request()
                .header("Content-Type", "application/fhir+xml")
                .header("User-Agent", appConfig.getUserAgent())
                .header("Authorization", "Bearer " + bearerTokenService.getBearerToken(runtimeConfig))
                .post(Entity.entity("", "application/fhir+xml"))) {

            String bundleString = new String(response.readEntity(InputStream.class).readAllBytes(), getCharset(response));

            if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                readEPrescriptionsMXBean.increaseAcceptFailed();
                throw new WebApplicationException("Error on " + appConfig.getPrescriptionServiceURL() + " " + bundleString, response.getStatus());
            }
            Bundle bundle = fhirContext.newXmlParser().parseResource(Bundle.class, bundleString);
            task = (Task) bundle.getEntry().get(0).getResource();
            secret = task.getIdentifier().stream().filter(t -> "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_Secret".equals(t.getSystem())).map(t -> t.getValue()).findAny().orElse(null);
            Binary binary = (Binary) bundle.getEntry().get(1).getResource();
            byte[] pkcs7Data = binary.getData();
            CMSSignedData signedData = new CMSSignedData(pkcs7Data);
            CMSProcessableByteArray signedContent = (CMSProcessableByteArray) signedData.getSignedContent();
            data = (byte[]) signedContent.getContent();
            readEPrescriptionsMXBean.increaseAccept();
        } catch (Throwable t) {
            readEPrescriptionsMXBean.increaseAcceptFailed();
            String msg = String.format("[%s] Could not process %s secret: %s", correlationId, token, secret);
            log.log(Level.SEVERE, msg, t);
            return null;
        }

        String prescriptionId = task.getIdentifier().stream().filter(t -> "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId".equals(t.getSystem())).map(t -> t.getValue()).findAny().orElse(null);

        try (Response response2 = client.target(appConfig.getPrescriptionServiceURL()).path("/Task/" + prescriptionId + "/$reject")
                .queryParam("secret", secret).request()
                .header("User-Agent", appConfig.getUserAgent())
                .header("Authorization", "Bearer " + bearerTokenService.getBearerToken(runtimeConfig))
                .post(Entity.entity("", "application/fhir+xml"))) {

            InputStream is = response2.readEntity(InputStream.class);
            String rejectResponse = "";
            if (is != null) {
                rejectResponse = new String(is.readAllBytes(), getCharset(response2));
            }
            if (Response.Status.Family.familyOf(response2.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                appendFailedRejectToFile(prescriptionId, secret, runtimeConfig);
                log.warning("Could not reject " + token + "prescriptionId: " + prescriptionId + " secret: " + secret + " " + rejectResponse);
            }
            readEPrescriptionsMXBean.increaseReject();

            // todo: print bundle to pdf if configured

            return fhirContext.newXmlParser().parseResource(Bundle.class, new String(data));
        } catch (Throwable t) {
            readEPrescriptionsMXBean.increaseRejectFailed();
            appendFailedRejectToFile(prescriptionId, secret, runtimeConfig);
            String msg = String.format(
                    "[%s] Could not process %s prescriptionId: %s secret: %s", correlationId, token, prescriptionId, secret
            );
            log.log(Level.SEVERE, msg, t);
            return null;
        }
    }

    private static @NotNull Charset getCharset(Response response) {
        String contentTypeHeader = response.getHeaderString("Content-Type");
        if (StringUtils.isEmpty(contentTypeHeader)) {
            log.warning("No content-type header found in response, using UTF-8");
            return StandardCharsets.UTF_8;
        }
        return ContentType.parse(contentTypeHeader).getCharset();
    }

    Future<?> appendFailedRejectToFile(String prescriptionId, String secret, RuntimeConfig runtimeConfig) {
        return scheduledExecutorService.submit(() -> {
            try {
                try (BufferedWriter writer = Files.newBufferedWriter(failedRejectsFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    String json = objectMapper.writeValueAsString(new FailedRejectEntry(prescriptionId, secret, runtimeConfig));
                    writer.write(json + System.lineSeparator());
                }
            } catch (IOException e) {
                log.log(Level.SEVERE, "Failed to write " + failedRejectsFile, e);
            }
        });
    }

    void retryFailedRejects() {
        if (Files.notExists(failedRejectsFile))
            return;
        try {
            List<String> lines = Files.readAllLines(failedRejectsFile);
            Stream<FailedRejectEntry> failedRejectEntries = lines.stream().map(s -> {
                try {
                    return objectMapper.readValue(s, FailedRejectEntry.class);
                } catch (JsonProcessingException e) {
                    log.log(Level.SEVERE, "Failed to deserialize FailedRejectEntry: " + s, e);
                    return null;
                }
            }).filter(Objects::nonNull);
            List<String> stillFailingEntries = reprocessFailingEntries(failedRejectEntries);
            Files.write(failedRejectsFile, stillFailingEntries, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to read/write dangling-e-prescriptions.json", e);
        }
    }

    List<String> reprocessFailingEntries(Stream<FailedRejectEntry> failedRejectEntries) {
        return failedRejectEntries.filter(entry -> !attemptReject(entry.getPrescriptionId(), entry.getSecret(), entry.getRuntimeConfig()))
                .map(o -> {
                    try {
                        return objectMapper.writeValueAsString(o);
                    } catch (JsonProcessingException e) {
                        log.log(Level.SEVERE, "Failed to serialize object", e);
                        return null;
                    }
                }).filter(Objects::nonNull)
                .toList();
    }

    public boolean attemptReject(String prescriptionId, String secret, RuntimeConfig runtimeConfig) {
        try (Response response = client.target(appConfig.getPrescriptionServiceURL()).path("/Task/" + prescriptionId + "/$reject")
                .queryParam("secret", secret).request()
                .header("User-Agent", appConfig.getUserAgent())
                .header("Authorization", "Bearer " + bearerTokenService.getBearerToken(runtimeConfig))
                .post(Entity.entity("", "application/fhir+xml"))) {

            String rejectResponse = "";
            if (response.hasEntity()) {
                InputStream is = response.readEntity(InputStream.class);
                if (is != null) {
                    rejectResponse = new String(is.readAllBytes(), "ISO-8859-15");
                }
            }
            if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                log.warning("Retry reject failed for prescriptionId: " + prescriptionId + " secret: " + secret + " " + rejectResponse);
                return false;
            } else {
                readEPrescriptionsMXBean.increaseReject();
                return true;
            }
        } catch (Throwable t) {
            log.log(Level.SEVERE, "Retry reject failed for prescriptionId: " + prescriptionId + " secret: " + secret, t);
            return false;
        }
    }

    /**
     * for tests without cdi
     */
    public void setReadEPrescriptionsMXBean(ReadEPrescriptionsMXBeanImpl readEPrescriptionsMXBean) {
        this.readEPrescriptionsMXBean = readEPrescriptionsMXBean;
    }

    public BearerTokenService getBearerTokenService() {
        return bearerTokenService;
    }
}
