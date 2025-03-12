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
import de.gematik.ws.conn.certificateservice.v6.ReadCardCertificate.CertRefList;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.certificateservicecommon.v2.CertRefEnum;
import de.gematik.ws.conn.certificateservicecommon.v2.X509DataInfoListType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.v7.GetCards;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDStatusType;
import de.gematik.ws.fa.vsdm.vsd.v5.UCAllgemeineVersicherungsdatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCGeschuetzteVersichertendatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCPersoenlicheVersichertendatenXML;
import de.health.service.cetp.IKonnektorClient;
import de.health.service.cetp.domain.eventservice.Subscription;
import de.health.service.cetp.domain.eventservice.card.Card;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.HexFormat;
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
import java.util.zip.GZIPInputStream;

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

    private static Map<RuntimeConfig, Map<String,String>> runtimeConfigToTelematikIdToSmcbHandle = new HashMap<>();

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
        
        KVNRAndTelematikId kvnrAndTelematikId = extractKVNRAndTelematikId(readVSD);

        smcbHandle = getSMCBHandleForTelematikId(kvnrAndTelematikId.telematikId, runtimeConfig);
        if(smcbHandle != null) {
            runtimeConfig.setSMCBHandle(smcbHandle);
        }

        try (Response response = client.target(appConfig.getPrescriptionServiceURL()).path("/Task")
                .queryParam("kvnr", kvnrAndTelematikId.kvnr)
                .queryParam("hcv", extractHCV(readVSD))
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
            generateRuntimeConfigToTelematikIdToSmcbHandle(runtimeConfig);
            log.log(Level.SEVERE, String.format("[%s] Could not read response from Fachdienst", correlationId), e);
            readEPrescriptionsMXBean.increaseTasksFailed();
            throw new WebApplicationException("Could not read response from Fachdienst", e);
        }
    }

    String getSMCBHandleForTelematikId(String telematikId, RuntimeConfig runtimeConfig) {
        reads++;
        // all 1000 reads clear the cache
        if(reads % 1000 == 0) {
            runtimeConfigToTelematikIdToSmcbHandle.clear();
            runtimeConfigToTelematikIdToSmcbHandle = new HashMap<>();
        }
        if(runtimeConfigToTelematikIdToSmcbHandle.containsKey(runtimeConfig) && runtimeConfigToTelematikIdToSmcbHandle.get(runtimeConfig).containsKey(telematikId)) {
            return runtimeConfigToTelematikIdToSmcbHandle.get(runtimeConfig).get(telematikId);
        }
        generateRuntimeConfigToTelematikIdToSmcbHandle(runtimeConfig);
        if(runtimeConfigToTelematikIdToSmcbHandle.containsKey(runtimeConfig)) {
            return runtimeConfigToTelematikIdToSmcbHandle.get(runtimeConfig).get(telematikId);
        }
        return null;
    }

    void generateRuntimeConfigToTelematikIdToSmcbHandle(RuntimeConfig runtimeConfig) {
        try {
            if(connectorServicesProvider == null) {
                log.warning("connectorServicesProvider is null");
                return;
            }
            EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);
            CertificateServicePortType certificateServicePortType = connectorServicesProvider.getCertificateServicePortType(runtimeConfig);
            GetCards getCards = new GetCards();
            ContextType contextType = connectorServicesProvider.getContextType(runtimeConfig);
            getCards.setContext(contextType);
            getCards.setCardType(CardTypeType.SMC_B);
            GetCardsResponse getCardsRespone = eventService.getCards(getCards);

            ReadCardCertificate.CertRefList certRefList = new ReadCardCertificate.CertRefList();
            certRefList.getCertRef().add(CertRefEnum.C_AUT);

            Holder<Status> statusHolder = new Holder<>();
            Holder<X509DataInfoListType> certHolder = new Holder<>();

            for(CardInfoType cif : getCardsRespone.getCards().getCard()) {
                try {
                    certificateServicePortType.readCardCertificate(cif.getCardHandle(), contextType, certRefList, CryptType.ECC,statusHolder, certHolder);
                } catch (de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage e) {
                    log.log(Level.WARNING, "Could not get ECC certificate", e);
                    try {
                        certificateServicePortType.readCardCertificate(cif.getCardHandle(), contextType, certRefList, CryptType.RSA, statusHolder, certHolder);
                    } catch (de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage e1) {
                        log.log(Level.WARNING, "Could not get RSA certificate", e);
                    }
                }
                if(statusHolder.value != null && statusHolder.value.getResult().equals("OK")) {
                   // get telematik id from certificate
                    try {
                        X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(certHolder.value.getX509DataInfo().get(0).getX509Data().getX509Certificate()));
                        String extractedTelematikId = extractTelematikIdFromCertificate(cert);
                        if(extractedTelematikId != null) {
                            if(!runtimeConfigToTelematikIdToSmcbHandle.containsKey(runtimeConfig)) {
                                runtimeConfigToTelematikIdToSmcbHandle.put(runtimeConfig, new HashMap<>());
                            }
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

    static String extractTelematikIdFromCertificate(X509Certificate cert) {
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
            log.log(Level.WARNING, "Could not extract telematif id from cert: "+cert, ex);
        }
        return null;
    }
    

    static synchronized String extractHCV(ReadVSDResult readVSDResult) {
        try {
            UCAllgemeineVersicherungsdatenXML allgemeineVersicherungsdatenXML = getAllgemeineVersicherungsdaten(readVSDResult);
            String vb = allgemeineVersicherungsdatenXML.getVersicherter().getVersicherungsschutz().getBeginn().replaceAll(" ", "");
            UCPersoenlicheVersichertendatenXML patient = getPatient(readVSDResult);
            String sas = patient.getVersicherter().getPerson().getStrassenAdresse().getStrasse() == null ? "" : patient.getVersicherter().getPerson().getStrassenAdresse().getStrasse().trim();
            return calculateHCV(vb, sas);
        } catch (IOException | NullPointerException | JAXBException | NoSuchAlgorithmException e) {
            String msg = "Could generate HCV message";
            log.log(Level.WARNING, msg, e);
            return "";
        }
    }

    static String calculateHCV(String vb, String sas)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte [] vbb = vb.getBytes("ISO-8859-1");
        byte[] sasb = sas.getBytes("ISO-8859-1");

        byte[] combined = new byte[vbb.length + sasb.length];

        System.arraycopy(vbb,0,combined,0         ,vbb.length);
        System.arraycopy(sasb,0,combined,vbb.length,sasb.length);

        byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(combined);
        byte[] first5 = new byte[5];
        System.arraycopy(sha256, 0, first5, 0, 5);
        first5[0] = (byte) (first5[0] & 127);
        return Base64.getUrlEncoder().encodeToString(first5);
    }

    static synchronized KVNRAndTelematikId extractKVNRAndTelematikId(ReadVSDResult readVSDResult) {
        try {
            UCPersoenlicheVersichertendatenXML patient = getPatient(readVSDResult);
            if(patient == null) {
                return new KVNRAndTelematikId(null, null);
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

    private static UCPersoenlicheVersichertendatenXML getPatient(ReadVSDResult readVSDResult)
            throws IOException, JAXBException {
        if(readVSDResult.persoenlicheVersichertendaten.value == null) {
            return null;
        }
        InputStream isPersoenlicheVersichertendaten = new GZIPInputStream(
            new ByteArrayInputStream(readVSDResult.persoenlicheVersichertendaten.value));
        UCPersoenlicheVersichertendatenXML patient = (UCPersoenlicheVersichertendatenXML) jaxbContext
                .createUnmarshaller().unmarshal(isPersoenlicheVersichertendaten);
        return patient;
    }

    private static UCAllgemeineVersicherungsdatenXML getAllgemeineVersicherungsdaten(ReadVSDResult readVSDResult)
            throws IOException, JAXBException {
        InputStream isPersoenlicheVersichertendaten = new GZIPInputStream(
            new ByteArrayInputStream(readVSDResult.allgemeineVersicherungsdaten.value));
            UCAllgemeineVersicherungsdatenXML allgemeineVersicherungsdatenXML = (UCAllgemeineVersicherungsdatenXML) jaxbContext
                .createUnmarshaller().unmarshal(isPersoenlicheVersichertendaten);
        return allgemeineVersicherungsdatenXML;
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
}
