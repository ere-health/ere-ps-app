package health.ere.ps.service.gematik;

import java.util.Base64;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.xml.ws.Holder;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDStatusType;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;

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

    private static final FhirContext fhirContext = FhirContext.forR4();

    Client client;

    @PostConstruct
    public void init() throws SecretsManagerException {
        client = ERezeptWorkflowService.initClientWithVAU(appConfig);
    }

    public Bundle getEPrescriptionsForCardHandle(String egkHandle, String smbcHandle, RuntimeConfig runtimeConfig) throws FaultMessage {
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);

        Holder<byte[]> persoenlicheVersichertendaten = new Holder<>();
			Holder<byte[]> allgemeineVersicherungsdaten = new Holder<>();
			Holder<byte[]> geschuetzteVersichertendaten = new Holder<>();
			Holder<VSDStatusType> vSD_Status = new Holder<>();
			Holder<byte[]> pruefungsnachweis = new Holder<>();
        connectorServicesProvider.getVSDServicePortType(runtimeConfig).readVSD(egkHandle, smbcHandle, true, true,
                context, persoenlicheVersichertendaten, allgemeineVersicherungsdaten, geschuetzteVersichertendaten,
                vSD_Status, pruefungsnachweis);

        String pnw = Base64.getUrlEncoder().encodeToString(pruefungsnachweis.value);

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
    
}
