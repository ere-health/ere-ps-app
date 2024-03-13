package health.ere.ps.service.gematik;

import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.xml.ws.Holder;

import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDStatusType;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.fhir.FHIRService;

@ApplicationScoped
public class PharmacyService extends BearerTokenManageService {

    private final static Logger log = Logger.getLogger(PharmacyService.class.getName()); 

    @Inject
    AppConfig appConfig;
    @Inject
    UserConfig userConfig;

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;
    @Inject
    ConnectorCardsService connectorCardsService;

    private static final FhirContext fhirContext = FHIRService.getFhirContext();

    Client client;

    @PostConstruct
    public void init() throws SecretsManagerException {
        client = ERezeptWorkflowService.initClientWithVAU(appConfig);
    }

    public Bundle getEPrescriptionsForCardHandle(String egkHandle, String smcbHandle, RuntimeConfig runtimeConfig) throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        
        if(runtimeConfig == null) {
            runtimeConfig = new RuntimeConfig();
        }
        runtimeConfig.setSMCBHandle(smcbHandle);
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
        
        Holder<byte[]> persoenlicheVersichertendaten = new Holder<>();
			Holder<byte[]> allgemeineVersicherungsdaten = new Holder<>();
			Holder<byte[]> geschuetzteVersichertendaten = new Holder<>();
			Holder<VSDStatusType> vSD_Status = new Holder<>();
			Holder<byte[]> pruefungsnachweis = new Holder<>();

        EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);
        if(egkHandle == null) {
            egkHandle = PrefillPrescriptionService.getFirstCardOfType(eventService, CardTypeType.EGK, context);
            
        }
        if(smcbHandle == null) {
            smcbHandle = PrefillPrescriptionService.getFirstCardOfType(eventService, CardTypeType.SMC_B, context);
            runtimeConfig.setSMCBHandle(smcbHandle);
        }
        requestNewAccessTokenIfNecessary(runtimeConfig, null, null);
        log.info(egkHandle+" "+smcbHandle);
        connectorServicesProvider.getVSDServicePortType(runtimeConfig).readVSD(egkHandle, smcbHandle, true, true,
                context, persoenlicheVersichertendaten, allgemeineVersicherungsdaten, geschuetzteVersichertendaten,
                vSD_Status, pruefungsnachweis);

        String pnw = Base64.getEncoder().encodeToString(pruefungsnachweis.value);

        try (Response response = client.target(appConfig.getPrescriptionServiceURL()).path("/Task")
                .queryParam("pnw", pnw).request()
                .header("User-Agent", appConfig.getUserAgent())
                .header("Authorization", "Bearer " + bearerToken.get(runtimeConfig))
                .get()) {

            String bundleString = response.readEntity(String.class);
            
            if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                throw new WebApplicationException("Error on "+appConfig.getPrescriptionServiceURL()+" "+bundleString, response.getStatus());
            }
            return fhirContext.newXmlParser().parseResource(Bundle.class, bundleString);
        }
    
    }

    public Bundle accept(String token, RuntimeConfig runtimeConfig) {
        requestNewAccessTokenIfNecessary(runtimeConfig, null, null);
        String secret = "";
        String prescriptionId = "";
        try (Response response = client.target(appConfig.getPrescriptionServiceURL()+token).request()
                .header("User-Agent", appConfig.getUserAgent())
                .header("Authorization", "Bearer " + bearerToken.get(runtimeConfig))
                .post(Entity.entity("", "application/fhir+xml; charset=UTF-8"))) {

            String bundleString = response.readEntity(String.class);
            
            if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                throw new WebApplicationException("Error on "+appConfig.getPrescriptionServiceURL()+" "+bundleString, response.getStatus());
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

            try (Response response2 = client.target(appConfig.getPrescriptionServiceURL()).path("/Task/"+prescriptionId+"/$reject")
                .queryParam("secret", secret).request()
                .header("User-Agent", appConfig.getUserAgent())
                .header("Authorization", "Bearer " + bearerToken.get(runtimeConfig))
                .post(Entity.entity("", "application/fhir+xml; charset=UTF-8"))) {
                    String rejectResponse = response2.readEntity(String.class);
                    if (Response.Status.Family.familyOf(response2.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                        log.warning("Could not reject "+token+"prescriptionId: "+prescriptionId+" secret: "+secret+" "+rejectResponse);
                    }
                    response2.close();
                }

            // todo: print bundle to pdf if configured

            return fhirContext.newXmlParser().parseResource(Bundle.class, new String(data));
        } catch(Throwable t) {
            log.log(Level.SEVERE, "Could not process "+token+"prescriptionId: "+prescriptionId+" secret: "+secret+" ", t);
            return null;
        }

    }

}
