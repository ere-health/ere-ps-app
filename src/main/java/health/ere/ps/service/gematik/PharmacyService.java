package health.ere.ps.service.gematik;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.certificateservice.v6.CryptType;
import de.gematik.ws.conn.certificateservice.v6.ReadCardCertificate;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.certificateservicecommon.v2.CertRefEnum;
import de.gematik.ws.conn.certificateservicecommon.v2.X509DataInfoListType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.v7.GetCards;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import de.gematik.ws.conn.vsds.vsdservice.v5.ReadVSDResponse;
import de.gematik.ws.fa.vsdm.vsd.v5.UCAllgemeineVersicherungsdatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCGeschuetzteVersichertendatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCPersoenlicheVersichertendatenXML;
import de.health.service.cetp.IKonnektorClient;
import de.health.service.cetp.domain.eventservice.Subscription;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.jmx.ReadEPrescriptionsMXBeanImpl;
import health.ere.ps.jmx.TelematikMXBeanRegistry;
import health.ere.ps.service.cetp.tracker.PrescriptionTracker;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.fhir.FHIRService;
import health.ere.ps.service.gematik.popp.PoppClient;
import health.ere.ps.service.idp.BearerTokenService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.ws.Holder;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.isismtt.ISISMTTObjectIdentifiers;
import org.bouncycastle.asn1.isismtt.x509.AdmissionSyntax;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Task;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import static health.ere.ps.service.gematik.ReadVSDHelper.ungzip;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * Note: reading, writing and resending of failed rejects are done by one Thread (see scheduledExecutorService),
 * no additional synchronization for retrying reject is needed
 */
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
    PoppClient poppClient;

    @Inject
    VSDService vsdService;

    @Inject
    IKonnektorClient konnektorClient;

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;

    @Inject
    @Setter // for tests without cdi
    ReadEPrescriptionsMXBeanImpl readEPrescriptionsMXBean;

    @Inject
    TelematikMXBeanRegistry telematikMXBeanRegistry;

    @Inject
    PrescriptionTracker prescriptionTracker;

    @Inject
    BearerTokenService bearerTokenService;

    @ConfigProperty(name = "preferred.smcb")
    Optional<String> preferredSmcb;

    /**
     * By default, should be the static file.
     * A test case can change this to a temporary file.
     */
    Path failedRejectsFile = Paths.get(FAILED_REJECTS_FILE);

    private static final FhirContext fhirContext = FHIRService.getFhirContext();

    Client client;

    static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    static DocumentBuilder builder;

    private static Map<RuntimeConfig, Map<String, String>> runtimeConfigToTelematikIdToSmcbHandle = new HashMap<>();

    private int reads = 0;

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
            return JAXBContext.newInstance(
                UCPersoenlicheVersichertendatenXML.class,
                UCAllgemeineVersicherungsdatenXML.class,
                UCGeschuetzteVersichertendatenXML.class
            );
        } catch (JAXBException e) {
            log.log(Level.SEVERE, "Could not init jaxb context", e);
            return null;
        }
    }

    @PostConstruct
    public void init() {
        client = ERezeptWorkflowService.initClientWithVAU(appConfig);
        int retryInterval = 1;  // TODO: make configurable
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

    public PrescriptionContext getEPrescriptionsForCardHandle(
        String correlationId,
        String egkHandle,
        String smcbHandle,
        RuntimeConfig runtimeConfig
    ) throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        return getEPrescriptionsForCardHandle(correlationId, egkHandle, smcbHandle, runtimeConfig, null);
    }

    private boolean valid(String telematikId) {
        return telematikId != null && !telematikId.isEmpty();
    }

    public PrescriptionContext getEPrescriptionsForCardHandle(
        String correlationId,
        String egkHandle,
        String smcbHandle,
        RuntimeConfig runtimeConfig,
        String kvnr
    ) throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        if (runtimeConfig == null) {
            runtimeConfig = new RuntimeConfig();
        }
        runtimeConfig.setSMCBHandle(smcbHandle);
        ReadVSDResponse readVSDResponse = readVSD(correlationId, egkHandle, smcbHandle, runtimeConfig);
        String pnw = Base64.getEncoder().encodeToString(readVSDResponse.getPruefungsnachweis());

        KVNRAndTelematikId kvnrAndTelematikId = extractKVNRAndTelematikId(readVSDResponse);
        String telematikId = kvnrAndTelematikId.telematikId;

        if (valid(telematikId)) {
            telematikMXBeanRegistry.registerTelematikIdBean(telematikId);
            smcbHandle = getSMCBHandleForTelematikId(telematikId, runtimeConfig);
            if (smcbHandle != null) {
                log.fine("Found SMCB handle for telematik id: '" + telematikId + "' " + smcbHandle);
                runtimeConfig.setSMCBHandle(smcbHandle);
            }
        }

        Invocation.Builder builder = client.target(appConfig.getPrescriptionServiceURL()).path("/Task")
            .queryParam("kvnr", kvnr != null ? kvnr : kvnrAndTelematikId.kvnr)
            .queryParam("hcv", extractHCV(readVSDResponse))
            .queryParam("pnw", pnw).request()
            .header("Content-Type", "application/fhir+xml")
            .header("User-Agent", appConfig.getUserAgent())
            .header("Authorization", "Bearer " + bearerTokenService.getBearerToken(runtimeConfig));

        if (appConfig.isZetaEnabled()) {
            String poppToken = poppClient.getToken(runtimeConfig, egkHandle);
            builder = builder.header("X-Popp-Token", poppToken);
        }
        try (Response response = builder.get()) {
            String event = getEvent(factory.newDocumentBuilder(), readVSDResponse.getPruefungsnachweis());
            String bundleString = new String(response.readEntity(InputStream.class).readAllBytes(), getCharset(response));

            if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                readEPrescriptionsMXBean.increaseTasksFailed();
                throw new WebApplicationException("Error on " + appConfig.getPrescriptionServiceURL() + " " + bundleString, response.getStatus());
            }
            readEPrescriptionsMXBean.increaseTasks();
            if (valid(telematikId)) {
                telematikMXBeanRegistry.countAccepted(telematikId);
                prescriptionTracker.countSuccessfulPrescription(telematikId);
            }
            return new PrescriptionContext(
                fhirContext.newXmlParser().parseResource(Bundle.class, bundleString),
                event,
                ReadVSDHelper.asString(readVSDResponse)
            );
        } catch (IOException | ParserConfigurationException | SAXException e) {
            generateRuntimeConfigToTelematikIdToSmcbHandle(runtimeConfig);
            log.log(Level.SEVERE, String.format("[%s] Could not read response from Fachdienst", correlationId), e);
            readEPrescriptionsMXBean.increaseTasksFailed();
            if (valid(telematikId)) {
                telematikMXBeanRegistry.countRejected(telematikId);
            }
            throw new WebApplicationException("Could not read response from Fachdienst", e);
        }
    }

    String getSMCBHandleForTelematikId(String telematikId, RuntimeConfig runtimeConfig) {
        reads++;
        // all 1000 reads clear the cache
        if (reads % 1000 == 0) {
            runtimeConfigToTelematikIdToSmcbHandle.clear();
            runtimeConfigToTelematikIdToSmcbHandle = new HashMap<>();
        }
        if (runtimeConfigToTelematikIdToSmcbHandle.containsKey(runtimeConfig) && runtimeConfigToTelematikIdToSmcbHandle.get(runtimeConfig).containsKey(telematikId)) {
            return runtimeConfigToTelematikIdToSmcbHandle.get(runtimeConfig).get(telematikId);
        }
        generateRuntimeConfigToTelematikIdToSmcbHandle(runtimeConfig);
        if (runtimeConfigToTelematikIdToSmcbHandle.containsKey(runtimeConfig)) {
            return runtimeConfigToTelematikIdToSmcbHandle.get(runtimeConfig).get(telematikId);
        }
        return null;
    }

    void generateRuntimeConfigToTelematikIdToSmcbHandle(RuntimeConfig runtimeConfig) {
        try {
            if (connectorServicesProvider == null) {
                log.warning("connectorServicesProvider is null");
                return;
            }
            EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);
            CertificateServicePortType certificateServicePortType = connectorServicesProvider.getCertificateServicePortType(runtimeConfig);
            GetCards getCards = new GetCards();
            ContextType contextType = connectorServicesProvider.getContextType(runtimeConfig);
            getCards.setContext(contextType);
            getCards.setCardType(CardTypeType.SMC_B);
            GetCardsResponse getCardsResponse = eventService.getCards(getCards);

            ReadCardCertificate.CertRefList certRefList = new ReadCardCertificate.CertRefList();
            certRefList.getCertRef().add(CertRefEnum.C_AUT);

            Holder<Status> statusHolder = new Holder<>();
            Holder<X509DataInfoListType> certHolder = new Holder<>();

            for (CardInfoType cif : getCardsResponse.getCards().getCard()) {
                try {
                    certificateServicePortType.readCardCertificate(
                        cif.getCardHandle(), contextType, certRefList, CryptType.ECC, statusHolder, certHolder
                    );
                } catch (de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage e) {
                    log.log(Level.WARNING, "Could not get ECC certificate", e);
                    try {
                        certificateServicePortType.readCardCertificate(
                            cif.getCardHandle(), contextType, certRefList, CryptType.RSA, statusHolder, certHolder
                        );
                    } catch (de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage e1) {
                        log.log(Level.WARNING, "Could not get RSA certificate", e);
                    }
                }
                if (statusHolder.value != null && statusHolder.value.getResult().equals("OK")) {
                    // get telematik id from certificate
                    try {
                        CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
                        byte[] x509Certificate = certHolder.value.getX509DataInfo().getFirst().getX509Data().getX509Certificate();
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(x509Certificate);
                        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(inputStream);
                        String extractedTelematikId = extractTelematikIdFromCertificate(cert);
                        if (extractedTelematikId != null) {
                            if (!runtimeConfigToTelematikIdToSmcbHandle.containsKey(runtimeConfig)) {
                                runtimeConfigToTelematikIdToSmcbHandle.put(runtimeConfig, new HashMap<>());
                            }
                            log.fine("Extracted telematik id from certificate of " + cif.getCardHandle() + " " + extractedTelematikId);
                            runtimeConfigToTelematikIdToSmcbHandle.get(runtimeConfig).put(extractedTelematikId, cif.getCardHandle());
                        } else {
                            log.warning("Could not extract telematik id from certificate of " + cif.getCardHandle());
                        }
                    } catch (CertificateException e) {
                        log.log(Level.WARNING, "Could not parse certificate", e);
                    }
                }
            }
        } catch (de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage e) {
            log.log(Level.WARNING, "Could not read cards", e);
        }
    }

    public static String extractTelematikIdFromCertificate(X509Certificate cert) {
        // https://oidref.com/1.3.36.8.3.3
        byte[] admission = cert.getExtensionValue(ISISMTTObjectIdentifiers.id_isismtt_at_admission.toString());
        try (ASN1InputStream input = new ASN1InputStream(admission)) {
            ASN1Primitive p = input.readObject();
            if (p != null) {
                // Based on https://stackoverflow.com/a/20439748
                DEROctetString derOctetString = (DEROctetString) p;
                try (ASN1InputStream asnInputStream = new ASN1InputStream(new ByteArrayInputStream(derOctetString.getOctets()))) {
                    ASN1Primitive asn1 = asnInputStream.readObject();
                    AdmissionSyntax admissionSyntax = AdmissionSyntax.getInstance(asn1);
                    return admissionSyntax.getContentsOfAdmissions()[0].getProfessionInfos()[0].getRegistrationNumber();
                }
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Could not extract telematif id from cert: " + cert, ex);
        }
        return null;
    }

    static synchronized String extractHCV(ReadVSDResponse readVSDResponse) {
        if (readVSDResponse.getAllgemeineVersicherungsdaten() == null) {
            return "";
        }
        try {
            UCAllgemeineVersicherungsdatenXML allgemeineVersicherungsdatenXML = getAllgemeineVersicherungsdaten(readVSDResponse);
            String vb = allgemeineVersicherungsdatenXML.getVersicherter().getVersicherungsschutz().getBeginn().replaceAll(" ", "");
            UCPersoenlicheVersichertendatenXML patient = getPatient(readVSDResponse);
            String sas = patient.getVersicherter().getPerson().getStrassenAdresse().getStrasse() == null
                ? ""
                : patient.getVersicherter().getPerson().getStrassenAdresse().getStrasse().trim();
            return calculateHCV(vb, sas);
        } catch (IOException | NullPointerException | JAXBException | NoSuchAlgorithmException e) {
            String msg = "Could generate HCV message";
            log.log(Level.WARNING, msg, e);
            return "";
        }
    }

    static String calculateHCV(String vb, String sas)
        throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] vbb = vb.getBytes(ISO_8859_1);
        byte[] sasb = sas.getBytes(ISO_8859_1);

        byte[] combined = new byte[vbb.length + sasb.length];

        System.arraycopy(vbb, 0, combined, 0, vbb.length);
        System.arraycopy(sasb, 0, combined, vbb.length, sasb.length);

        byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(combined);
        byte[] first5 = new byte[5];
        System.arraycopy(sha256, 0, first5, 0, 5);
        first5[0] = (byte) (first5[0] & 127);
        return Base64.getUrlEncoder().encodeToString(first5);
    }

    static synchronized KVNRAndTelematikId extractKVNRAndTelematikId(ReadVSDResponse readVSDResponse) {
        try {
            UCPersoenlicheVersichertendatenXML patient = getPatient(readVSDResponse);
            if (patient == null) {
                return new KVNRAndTelematikId("", "");
            }
            String versichertenID = patient.getVersicherter().getVersichertenID();
            String telematikId = patient.getVersicherter().getPerson().getStrassenAdresse().getAnschriftenzusatz();
            return new KVNRAndTelematikId(versichertenID, telematikId);
        } catch (IOException | NullPointerException | JAXBException e) {
            String msg = "Could not extract KVNR message";
            log.log(Level.WARNING, msg, e);
            return new KVNRAndTelematikId("", "");
        }
    }

    public static class KVNRAndTelematikId {
        String kvnr;
        String telematikId;

        public KVNRAndTelematikId(String kvnr, String telematikId) {
            this.kvnr = kvnr;
            this.telematikId = telematikId;
        }
    }

    private static UCPersoenlicheVersichertendatenXML getPatient(ReadVSDResponse readVSDResponse) throws IOException, JAXBException {
        if (readVSDResponse.getPersoenlicheVersichertendaten() == null) {
            return null;
        }
        return (UCPersoenlicheVersichertendatenXML) jaxbContext.createUnmarshaller()
            .unmarshal(ungzip(readVSDResponse.getPersoenlicheVersichertendaten()));
    }

    private static UCAllgemeineVersicherungsdatenXML getAllgemeineVersicherungsdaten(ReadVSDResponse readVSDResponse)
        throws IOException, JAXBException {
        return (UCAllgemeineVersicherungsdatenXML) jaxbContext.createUnmarshaller()
            .unmarshal(ungzip(readVSDResponse.getAllgemeineVersicherungsdaten()));
    }

    public ReadVSDResponse readVSD(
        String correlationId,
        String egkHandle,
        String smcbHandle,
        RuntimeConfig runtimeConfig
    ) throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
        if ("".equals(context.getUserId()) || context.getUserId() == null) {
            context.setUserId(UUID.randomUUID().toString());
        }

        EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);
        if (egkHandle == null) {
            egkHandle = PrefillPrescriptionService.getFirstCardOfType(eventService, CardTypeType.EGK, context, null);

        }
        if (smcbHandle == null) {
            smcbHandle = setAndGetSMCBHandleForPharmacy(runtimeConfig, context, eventService);
        }
        log.fine(egkHandle + " " + smcbHandle);
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
            log.fine(String.format(
                "[%s] readVSD for cardHandle=%s, smcbHandle=%s, subscriptions: %s", correlationId, egkHandle, smcbHandle, listString
            ));

            ReadVSDResponse readVSDResponse = vsdService.read(egkHandle, smcbHandle, runtimeConfig, true, true);
            readEPrescriptionsMXBean.increaseVSDRead();
            return readVSDResponse;
        } catch (Throwable t) {
            readEPrescriptionsMXBean.increaseVSDFailed();
            throw new RuntimeException(t);
        }
    }

    private String getEvent(DocumentBuilder builder, byte[] pruefnachweisBytes) throws IOException, SAXException {
        try (InputStream inputStream = ungzip(pruefnachweisBytes)) {
            String decodedXMLFromPNW = new String(inputStream.readAllBytes());
            Document doc = builder.parse(new ByteArrayInputStream(decodedXMLFromPNW.getBytes()));
            return doc.getElementsByTagName("E").item(0).getTextContent();
        }
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

    public Bundle accept(String correlationId, String token, String egkHandle, RuntimeConfig runtimeConfig) {
        String secret = "";
        byte[] data;
        Task task;
        Invocation.Builder builder = client.target(appConfig.getPrescriptionServiceURL() + token).request()
            .header("Content-Type", "application/fhir+xml")
            .header("User-Agent", appConfig.getUserAgent())
            .header("Authorization", "Bearer " + bearerTokenService.getBearerToken(runtimeConfig));
        if (appConfig.isZetaEnabled()) {
            String poppToken = poppClient.getToken(runtimeConfig, egkHandle);
            builder = builder.header("X-Popp-Token", poppToken);
        }
        try (Response response = builder
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
}
