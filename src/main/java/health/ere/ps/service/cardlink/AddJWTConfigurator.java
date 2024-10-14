package health.ere.ps.service.cardlink;

import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage;
import de.health.service.cetp.konnektorconfig.KonnektorConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.gematik.PharmacyService;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.ClientEndpointConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.handler.codec.http.HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL;

@Unremovable
@ApplicationScoped
public class AddJWTConfigurator extends ClientEndpointConfig.Configurator {

    private static final Logger log = Logger.getLogger(AddJWTConfigurator.class.getName());

    @Inject
    PharmacyService pharmacyService;

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;

    private static final Map<String, RuntimeConfig> configMap = new HashMap<>();

    public static void initConfigs(Collection<KonnektorConfig> konnektorConfigs) {
        konnektorConfigs.forEach(kc ->
            configMap.put(
                kc.getCardlinkEndpoint().getPath().replace("/websocket/", "").trim(),
                new RuntimeConfig(kc.getUserConfigurations())
            )
        );
    }

    private boolean checkServicesAreValid() {
        return pharmacyService != null && connectorServicesProvider != null;
    }

    private RuntimeConfig buildRuntimeConfig(Map<String, List<String>> headers) {
        String serialNumber = null;
        RuntimeConfig runtimeConfig = null;
        
        List<String> wsProtocolHeaderValues = headers.get(SEC_WEBSOCKET_PROTOCOL.toString());
        if (!wsProtocolHeaderValues.isEmpty()) {
            serialNumber = wsProtocolHeaderValues.get(0);
            runtimeConfig = configMap.get(serialNumber);
        }
        if (runtimeConfig == null) {
            log.warning(String.format("No KonnektorConfig is found for serialNumber=%s, using random config", serialNumber));
            runtimeConfig = new RuntimeConfig();
        }
        return runtimeConfig;
    }

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        if (checkServicesAreValid()) {
            RuntimeConfig runtimeConfig = buildRuntimeConfig(headers);
            headers.remove(SEC_WEBSOCKET_PROTOCOL.toString());
            ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
            EventServicePortType eventServicePortType = connectorServicesProvider.getEventServicePortType(runtimeConfig);
            try {
                pharmacyService.setAndGetSMCBHandleForPharmacy(runtimeConfig, context, eventServicePortType);
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
