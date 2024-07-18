package health.ere.ps.service.cardlink;

import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.cetp.config.KonnektorConfig;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.gematik.PharmacyService;
import io.quarkus.arc.Arc;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;
import jakarta.websocket.ClientEndpointConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Unremovable
@Dependent
public class AddJWTConfigurator extends ClientEndpointConfig.Configurator {

    private static final Logger log = Logger.getLogger(AddJWTConfigurator.class.getName());

    PharmacyService pharmacyService;

    MultiConnectorServicesProvider connectorServicesProvider;

    private static final Map<String, RuntimeConfig> configMap = new HashMap<>();

    public static void initConfigs(Collection<KonnektorConfig> konnektorConfigs) {
        konnektorConfigs.forEach(kc ->
            configMap.put(kc.getCardlinkEndpoint().getHost(), new RuntimeConfig(kc.getUserConfigurations()))
        );
    }

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        if (pharmacyService == null) {
            pharmacyService = Arc.container().select(PharmacyService.class).get();
        }
        if (connectorServicesProvider == null) {
            connectorServicesProvider = Arc.container().select(MultiConnectorServicesProvider.class).get();
        }
        String originHeader = headers.get("origin").get(0);
        try {
            String originHost = new URI(originHeader).getHost();
            RuntimeConfig runtimeConfig = configMap.get(originHost);
            if (connectorServicesProvider != null && pharmacyService != null) {
                ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
                EventServicePortType eventServicePortType = connectorServicesProvider.getEventServicePortType(runtimeConfig);
                try {
                    PharmacyService.setAndGetSMCBHandleForPharmacy(runtimeConfig, context, eventServicePortType);
                } catch (FaultMessage e) {
                    log.log(Level.SEVERE, "Could not get SMC-B for pharmacy", e);
                }

                String bearerToken = pharmacyService.getBearerTokenService().getBearerToken(runtimeConfig);
                headers.put("Authorization", List.of("Bearer " + bearerToken));
            } else {
                log.log(Level.SEVERE, "Could not get bearer token or connector services provider, won't add JWT to websocket connection.");
            }
        } catch (URISyntaxException e) {
            log.log(Level.SEVERE, String.format("Could not get bearer token or connector, origin header is wrong URI: %s", originHeader), e);
        }
    }
}
