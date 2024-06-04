package health.ere.ps.service.cardlink;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.gematik.PharmacyService;
import io.quarkus.arc.Arc;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.HandshakeResponse;

@Unremovable
@Dependent
public class AddJWTConfigurator extends ClientEndpointConfig.Configurator {

    private static final Logger log = Logger.getLogger(AddJWTConfigurator.class.getName());

    PharmacyService bearerTokenService;

    MultiConnectorServicesProvider connectorServicesProvider;

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {

        if (bearerTokenService == null) {
            bearerTokenService = Arc.container().select(PharmacyService.class).get();
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

            bearerTokenService.requestNewAccessTokenIfNecessary(runtimeConfig, null, null);
            headers.put("Authorization", List.of("Bearer " + bearerTokenService.getBearerToken(runtimeConfig)));
        } else {
            log.log(Level.SEVERE, "Could not get bearer token or connector services provider, won't add JWT to websocket connection.");
        }
    }

    @Override
    public void afterResponse(HandshakeResponse handshakeResponse) {

    }
}
