package health.ere.ps.service.gematik;

import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.vsds.kvkservice.v4.FaultMessage;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class KVKService {

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;

    public byte[] readKVK(String kvkHandle, RuntimeConfig runtimeConfig) throws FaultMessage {
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
        return connectorServicesProvider.getKVKServicePortType(runtimeConfig).readKVK(kvkHandle, context);
    }
}