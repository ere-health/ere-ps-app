package health.ere.ps.service.cardlink;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.HandshakeResponse;

import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.gematik.BearerTokenManageService;
import health.ere.ps.service.gematik.PharmacyService;

public class AddJWTConfigurator extends ClientEndpointConfig.Configurator {

    private static final Logger log = Logger.getLogger(AddJWTConfigurator.class.getName());
    
    @Inject
    BearerTokenManageService bearerTokenManageService;

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;


    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        RuntimeConfig runtimeConfig = new RuntimeConfig();
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
        EventServicePortType eventServicePortType = connectorServicesProvider.getEventServicePortType(runtimeConfig);
        try {
            PharmacyService.setAndGetSMCBHandleForPharmacy(runtimeConfig, context, eventServicePortType);
        } catch (FaultMessage | de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage e) {
            log.log(Level.SEVERE, "Could not get SMC-B for pharmacy", e);
        }

        bearerTokenManageService.requestNewAccessTokenIfNecessary(runtimeConfig, null, null);
        headers.put("Authorization", Arrays.asList("Bearer "+bearerTokenManageService.getBearerToken(runtimeConfig)));
    }

    @Override
    public void afterResponse(HandshakeResponse handshakeResponse) {

    }
}
