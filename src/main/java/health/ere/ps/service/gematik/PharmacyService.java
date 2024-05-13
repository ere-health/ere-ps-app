package health.ere.ps.service.gematik;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Task;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.v7.GetCards;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.v7.SubscriptionType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDStatusType;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.service.cetp.CETPServer;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.fhir.FHIRService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import jakarta.xml.ws.Holder;

@ApplicationScoped
public class PharmacyService extends BearerTokenManageService {

    private final static Logger log = Logger.getLogger(PharmacyService.class.getName());

    @Inject
    AppConfig appConfig;

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;

    private static final FhirContext fhirContext = FHIRService.getFhirContext();

    Client client;

    List<String> subscriptionsIds = new ArrayList<>();

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    @PostConstruct
    public void init() throws SecretsManagerException {
        client = ERezeptWorkflowService.initClientWithVAU(appConfig);
    }

    public Pair<Bundle, String> getEPrescriptionsForCardHandle(
        String egkHandle,
        String smcbHandle,
        RuntimeConfig runtimeConfig
    ) throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        Holder<byte[]> pruefungsnachweis = readVSD(egkHandle, smcbHandle, runtimeConfig);
        String pnw = Base64.getEncoder().encodeToString(pruefungsnachweis.value);
        try (Response response = client.target(appConfig.getPrescriptionServiceURL()).path("/Task")
            .queryParam("pnw", pnw).request()
            .header("Content-Type", "application/fhir+xml")
            .header("User-Agent", appConfig.getUserAgent())
            .header("Authorization", "Bearer " + bearerToken.get(runtimeConfig))
            .get()) {

            String event = getEvent(factory.newDocumentBuilder(), pruefungsnachweis.value);
            String bundleString = new String(response.readEntity(InputStream.class).readAllBytes(), "ISO-8859-15");

            if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                throw new WebApplicationException("Error on " + appConfig.getPrescriptionServiceURL() + " " + bundleString, response.getStatus());
            }
            return Pair.of(fhirContext.newXmlParser().parseResource(Bundle.class, bundleString), event);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            log.log(Level.SEVERE, "Could not read response from Fachdienst", e);
            throw new WebApplicationException("Could not read response from Fachdienst", e);
        }
    }

    public Holder<byte[]> readVSD(
        String egkHandle,
        String smcbHandle,
        RuntimeConfig runtimeConfig
    ) throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        if (runtimeConfig == null) {
            runtimeConfig = new RuntimeConfig();
        }
        runtimeConfig.setSMCBHandle(smcbHandle);
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
        requestNewAccessTokenIfNecessary(runtimeConfig, null, null);
        log.info(egkHandle + " " + smcbHandle);
        connectorServicesProvider.getVSDServicePortType(runtimeConfig).readVSD(
            egkHandle, smcbHandle, true, true, context,
            persoenlicheVersichertendaten,
            allgemeineVersicherungsdaten,
            geschuetzteVersichertendaten,
            vSD_Status,
            pruefungsnachweis
        );

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
    ) throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        String smcbHandle;
        smcbHandle = getFirstCardWithName(eventService, CardTypeType.SMC_B, context, "Bad ApothekeTEST-ONLY");
        if (smcbHandle == null) {
            smcbHandle = PrefillPrescriptionService.getFirstCardOfType(eventService, CardTypeType.SMC_B, context);
        }
        runtimeConfig.setSMCBHandle(smcbHandle);
        return smcbHandle;
    }

    static String getFirstCardWithName(EventServicePortType eventService, CardTypeType type, ContextType context, String name)
        throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
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
    }

    public Bundle accept(String token, RuntimeConfig runtimeConfig) {
        requestNewAccessTokenIfNecessary(runtimeConfig, null, null);
        String secret = "";
        String prescriptionId = "";
        try (Response response = client.target(appConfig.getPrescriptionServiceURL() + token).request()
            .header("Content-Type", "application/fhir+xml")
            .header("User-Agent", appConfig.getUserAgent())
            .header("Authorization", "Bearer " + bearerToken.get(runtimeConfig))
            .post(Entity.entity("", "application/fhir+xml"))) {

            String bundleString = new String(response.readEntity(InputStream.class).readAllBytes(), "ISO-8859-15");

            if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                throw new WebApplicationException("Error on " + appConfig.getPrescriptionServiceURL() + " " + bundleString, response.getStatus());
            }
            Bundle bundle = fhirContext.newXmlParser().parseResource(Bundle.class, bundleString);
            Task task = (Task) bundle.getEntry().get(0).getResource();
            secret = task.getIdentifier().stream().filter(t -> "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_Secret".equals(t.getSystem())).map(t -> t.getValue()).findAny().orElse(null);
            Binary binary = (Binary) bundle.getEntry().get(1).getResource();
            byte[] pkcs7Data = binary.getData();
            CMSSignedData signedData = new CMSSignedData(pkcs7Data);
            CMSProcessableByteArray signedContent = (CMSProcessableByteArray) signedData.getSignedContent();
            byte[] data = (byte[]) signedContent.getContent();

            response.close();

            prescriptionId = task.getIdentifier().stream().filter(t -> "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId".equals(t.getSystem())).map(t -> t.getValue()).findAny().orElse(null);

            try (Response response2 = client.target(appConfig.getPrescriptionServiceURL()).path("/Task/" + prescriptionId + "/$reject")
                .queryParam("secret", secret).request()
                .header("User-Agent", appConfig.getUserAgent())
                .header("Authorization", "Bearer " + bearerToken.get(runtimeConfig))
                .post(Entity.entity("", "application/fhir+xml"))) {
                
                InputStream is = response2.readEntity(InputStream.class);
                String rejectResponse = "";
                if (is != null) {
                    rejectResponse = new String(is.readAllBytes(), "ISO-8859-15");
                }
                if (Response.Status.Family.familyOf(response2.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                    log.warning("Could not reject " + token + "prescriptionId: " + prescriptionId + " secret: " + secret + " " + rejectResponse);
                }
                response2.close();
            }

            // todo: print bundle to pdf if configured

            return fhirContext.newXmlParser().parseResource(Bundle.class, new String(data));
        } catch (Throwable t) {
            log.log(Level.SEVERE, "Could not process " + token + "prescriptionId: " + prescriptionId + " secret: " + secret + " ", t);
            return null;
        }
    }

    public String subscribe(RuntimeConfig runtimeConfig, String host) throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        if (runtimeConfig == null) {
            runtimeConfig = new RuntimeConfig();
        }
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);

        EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);
        SubscriptionType subscriptionType = new SubscriptionType();

        subscriptionType.setEventTo("cetp://" + host + ":" + CETPServer.PORT);
        subscriptionType.setTopic("CARD/INSERTED");
        Holder<Status> status = new Holder<>();
        Holder<String> subscriptionId = new Holder<>();
        Holder<XMLGregorianCalendar> terminationTime = new Holder<>();
        eventService.subscribe(context, subscriptionType, status, subscriptionId, terminationTime);
        subscriptionsIds.add(subscriptionId.value);
        return status.value.getResult() + " " + subscriptionId.value + " " + terminationTime.value.toString();
    }

    public void unsubscribeAll(RuntimeConfig runtimeConfig, String host) throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        if (runtimeConfig == null) {
            runtimeConfig = new RuntimeConfig();
        }
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);

        EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);

        for (String subscriptionId : subscriptionsIds) {
            try {
                eventService.unsubscribe(context, subscriptionId, "cetp://" + host + ":" + CETPServer.PORT);
            } catch (Throwable t) {
                log.log(Level.WARNING, "Could not unsubscribe " + subscriptionId, t);
            }
        }
        subscriptionsIds.clear();
    }
}
