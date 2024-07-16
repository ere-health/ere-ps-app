package health.ere.ps.service.gematik;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.v7.GetCards;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.v7.SubscriptionType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDStatusType;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.jmx.ReadEPrescriptionsMXBeanImpl;
import health.ere.ps.service.cetp.KonnektorClient;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.fhir.FHIRService;
import health.ere.ps.service.idp.BearerTokenService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import jakarta.xml.ws.Holder;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Task;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@ApplicationScoped
public class PharmacyService {

    private static final Logger log = Logger.getLogger(PharmacyService.class.getName());

    @Inject
    AppConfig appConfig;

    @Inject
    KonnektorClient konnektorClient;

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;

    @Inject
    ReadEPrescriptionsMXBeanImpl readEPrescriptionsMXBean;

    @Inject
    BearerTokenService bearerTokenService;

    private static final FhirContext fhirContext = FHIRService.getFhirContext();

    Client client;

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    @PostConstruct
    public void init() {
        client = ERezeptWorkflowService.initClientWithVAU(appConfig);
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
        Holder<byte[]> pruefungsnachweis = readVSD(correlationId, egkHandle, smcbHandle, runtimeConfig);
        String pnw = Base64.getEncoder().encodeToString(pruefungsnachweis.value);
        try (Response response = client.target(appConfig.getPrescriptionServiceURL()).path("/Task")
                .queryParam("pnw", pnw).request()
                .header("Content-Type", "application/fhir+xml")
                .header("User-Agent", appConfig.getUserAgent())
                .header("Authorization", "Bearer " + bearerTokenService.getBearerToken(runtimeConfig))
                .get()) {

            String event = getEvent(factory.newDocumentBuilder(), pruefungsnachweis.value);
            String bundleString = new String(response.readEntity(InputStream.class).readAllBytes(), "ISO-8859-15");

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

    public Holder<byte[]> readVSD(
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
            egkHandle = PrefillPrescriptionService.getFirstCardOfType(eventService, CardTypeType.EGK, context);

        }
        if (smcbHandle == null) {
            smcbHandle = setAndGetSMCBHandleForPharmacy(runtimeConfig, context, eventService);
        }
        log.info(egkHandle + " " + smcbHandle);
        VSDServicePortType vsdServicePortType = connectorServicesProvider.getVSDServicePortType(runtimeConfig);
        try {
            String listString;
            try {
                List<SubscriptionType> subscriptions = konnektorClient.getSubscriptions(runtimeConfig);
                listString = subscriptions.stream()
                        .map(s -> String.format("[id=%s eventTo=%s topic=%s]", s.getSubscriptionID(), s.getEventTo(), s.getTopic()))
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
        return pruefungsnachweis;
    }

    private String getEvent(DocumentBuilder builder, byte[] pruefnachweisBytes) throws IOException, SAXException {
        String decodedXMLFromPNW = new String(new GZIPInputStream(new ByteArrayInputStream(pruefnachweisBytes)).readAllBytes());
        Document doc = builder.parse(new ByteArrayInputStream(decodedXMLFromPNW.getBytes()));
        return doc.getElementsByTagName("E").item(0).getTextContent();
    }

    public static String setAndGetSMCBHandleForPharmacy(
            RuntimeConfig runtimeConfig,
            ContextType context,
            EventServicePortType eventService
    ) throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        String smcbHandle;
        smcbHandle = getFirstCardWithName(eventService, CardTypeType.SMC_B, context, "Bad ApothekeTEST-ONLY");
        if (smcbHandle == null) {
            smcbHandle = PrefillPrescriptionService.getFirstCardOfType(eventService, CardTypeType.SMC_B, context);
        }
        runtimeConfig.setSMCBHandle(smcbHandle);
        return smcbHandle;
    }

    static String getFirstCardWithName(
        EventServicePortType eventService,
        CardTypeType type,
        ContextType context,
        String name
    ) {
        try {
            GetCards parameter = new GetCards();
            parameter.setContext(context);
            parameter.setCardType(type);
            GetCardsResponse getCardsResponse = eventService.getCards(parameter);

            List<CardInfoType> cards = getCardsResponse.getCards().getCard();
            if (cards.isEmpty()) {
                return null;
            } else {
                CardInfoType cardHandleType = cards.stream().filter(card -> card.getCardHolderName().equals(name)).findAny().orElse(null);
                return cardHandleType != null ? cardHandleType.getCardHandle() : null;
            }
        } catch (Throwable t) {
            log.log(Level.SEVERE, "Error while getting cards", t);
            return null;
        }
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

            String bundleString = new String(response.readEntity(InputStream.class).readAllBytes(), "ISO-8859-15");

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
                rejectResponse = new String(is.readAllBytes(), "ISO-8859-15");
            }
            if (Response.Status.Family.familyOf(response2.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                log.warning("Could not reject " + token + "prescriptionId: " + prescriptionId + " secret: " + secret + " " + rejectResponse);
            }
            readEPrescriptionsMXBean.increaseReject();

            // todo: print bundle to pdf if configured

            return fhirContext.newXmlParser().parseResource(Bundle.class, new String(data));
        } catch (Throwable t) {
            readEPrescriptionsMXBean.increaseRejectFailed();
            String msg = String.format(
                    "[%s] Could not process %s prescriptionId: %s secret: %s", correlationId, token, prescriptionId, secret
            );
            log.log(Level.SEVERE, msg, t);
            return null;
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
