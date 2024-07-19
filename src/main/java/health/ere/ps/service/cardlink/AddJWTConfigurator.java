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
    private static final String ORIGIN_HEADER = "origin";

    PharmacyService pharmacyService;
    MultiConnectorServicesProvider connectorServicesProvider;

    private static final Map<String, RuntimeConfig> configMap = new HashMap<>();

    public static void initConfigs(Collection<KonnektorConfig> konnektorConfigs) {
        konnektorConfigs.forEach(kc ->
            configMap.put(kc.getCardlinkEndpoint().getHost(), new RuntimeConfig(kc.getUserConfigurations()))
        );
    }

    private boolean checkServicesAreValid() {
        if (pharmacyService == null) {
            pharmacyService = Arc.container().select(PharmacyService.class).get();
        }
        if (connectorServicesProvider == null) {
            connectorServicesProvider = Arc.container().select(MultiConnectorServicesProvider.class).get();
        }
        return pharmacyService != null && connectorServicesProvider != null;
    }

    private RuntimeConfig buildRuntimeConfig(Map<String, List<String>> headers) {
        String origin = null;
        RuntimeConfig runtimeConfig = null;
        List<String> originHeaderValues = headers.get(ORIGIN_HEADER);
        if (!originHeaderValues.isEmpty()) {
            origin = originHeaderValues.get(0);
            try {
                runtimeConfig = configMap.get(new URI(origin).getHost());
            } catch (URISyntaxException e) {
                String msg = String.format("Origin header is wrong URI: %s", origin);
                log.log(Level.SEVERE, msg, e);
            }
        }
        if (runtimeConfig == null) {
            log.warning(String.format("No KonnektorConfig is found for origin=%s, using random config", origin));
            runtimeConfig = new RuntimeConfig();
        }
        return runtimeConfig;
    }

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        if (checkServicesAreValid()) {
            RuntimeConfig runtimeConfig = buildRuntimeConfig(headers);
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
            String msg = "Could not get pharmacyService or connectorServicesProvider, won't add JWT to websocket connection";
            log.log(Level.SEVERE, msg);
        }
    }
}
