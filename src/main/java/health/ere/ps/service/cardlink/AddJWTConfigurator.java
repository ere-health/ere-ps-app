package health.ere.ps.service.cardlink;

import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.gematik.BearerTokenManageService;
import health.ere.ps.service.gematik.PharmacyService;
import health.ere.ps.service.idp.BearerTokenService;
import io.quarkus.arc.Arc;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.HandshakeResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Unremovable
@Dependent
public class AddJWTConfigurator extends ClientEndpointConfig.Configurator {

    private static final Logger log = Logger.getLogger(AddJWTConfigurator.class.getName());

    BearerTokenService bearerTokenService;

    // In the future it should be managed automatically by the webclient, including its renewal
    Map<RuntimeConfig, String> bearerToken = new HashMap<>();

    MultiConnectorServicesProvider connectorServicesProvider;


    @Override
    public void beforeRequest(Map<String, List<String>> headers) {

        if (bearerTokenService == null) {
            bearerTokenService = Arc.container().select(BearerTokenService.class).get();
        }
        if (connectorServicesProvider == null) {
            connectorServicesProvider = Arc.container().select(MultiConnectorServicesProvider.class).get();
        }

        RuntimeConfig runtimeConfig = new RuntimeConfig();
        if (connectorServicesProvider != null && bearerTokenService != null) {
            ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
            EventServicePortType eventServicePortType = connectorServicesProvider.getEventServicePortType(runtimeConfig);
            try {
                PharmacyService.setAndGetSMCBHandleForPharmacy(runtimeConfig, context, eventServicePortType);
            } catch (FaultMessage | de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage e) {
                log.log(Level.SEVERE, "Could not get SMC-B for pharmacy", e);
            }

            BearerTokenManageService.requestNewAccessTokenIfNecessary(runtimeConfig, bearerToken, bearerTokenService, null, null);
            headers.put("Authorization", List.of("Bearer " + bearerToken.get(runtimeConfig)));
        } else {
            log.log(Level.SEVERE, "Could not get bearer token or connector services provider, won't add JWT to websocket connection.");
        }
    }

    @Override
    public void afterResponse(HandshakeResponse handshakeResponse) {

    }
}
