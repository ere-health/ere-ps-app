package health.ere.ps.service.cardlink;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.HandshakeResponse;

import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.gematik.BearerTokenManageService;
import health.ere.ps.service.gematik.PharmacyService;

public class AddJWTConfigurator extends ClientEndpointConfig.Configurator {

    @Inject
    BearerTokenManageService bearerTokenManageService;

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;


    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        RuntimeConfig runtimeConfig = new RuntimeConfig();
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
        EventServicePortType eventServicePortType = connectorServicesProvider.getEventServicePortType(runtimeConfig);
        PharmacyService.setAndGetSMCBHandleForPharmacy(runtimeConfig, context, eventServicePortType)

        bearerTokenManageService.requestNewAccessTokenIfNecessary(runtimeConfig, null, null);
        headers.put("Authorization", Arrays.asList("Bearer "+bearerTokenManageService.getBearerToken(runtimeConfig)));
    }

    @Override
    public void afterResponse(HandshakeResponse handshakeResponse) {

    }
}
