package health.ere.ps.service.connector.provider;

import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureServicePortType;
import de.gematik.ws.conn.cardservice.wsdl.v8_2.CardServicePortType;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortTypeV740;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortTypeV755;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;
import de.health.service.config.api.UserRuntimeConfig;
import health.ere.ps.config.UserConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class MultiConnectorServicesProvider {

    @Inject
    DefaultConnectorServicesProvider defaultConnectorServicesProvider;

    @Inject
    Event<Exception> eventException;

    ConcurrentHashMap<KonnektorKey, SingleConnectorServicesProvider> portMap = new ConcurrentHashMap<>();

    public AbstractConnectorServicesProvider getSingleConnectorServicesProvider(UserRuntimeConfig userConfig) {
        if (userConfig == null) {
            return defaultConnectorServicesProvider;
        } else {
            return portMap.computeIfAbsent(new KonnektorKey(userConfig), kk ->
                new SingleConnectorServicesProvider(userConfig, eventException)
            );
        }
    }

    public boolean isInitialized(Set<KonnektorKey> keys) {
        Collection<SingleConnectorServicesProvider> providers = portMap.entrySet().stream()
            .filter(e -> keys.isEmpty() || keys.contains(e.getKey()))
            .map(Map.Entry::getValue)
            .toList();
        return providers.stream().allMatch(SingleConnectorServicesProvider::isInitialized);
    }

    public CardServicePortType getCardServicePortType(UserConfig userConfig) {
        return getSingleConnectorServicesProvider(userConfig).getCardServicePortType();
    }

    public CertificateServicePortType getCertificateServicePortType(UserRuntimeConfig userConfig) {
        return getSingleConnectorServicesProvider(userConfig).getCertificateService();
    }

    public EventServicePortType getEventServicePortType(UserRuntimeConfig userConfig) {
        return getSingleConnectorServicesProvider(userConfig).getEventServicePortType();
    }

    public AuthSignatureServicePortType getAuthSignatureServicePortType(UserRuntimeConfig userConfig) {
        return getSingleConnectorServicesProvider(userConfig).getAuthSignatureServicePortType();
    }

    public SignatureServicePortTypeV740 getSignatureServicePortType(UserRuntimeConfig userConfig) {
        return getSingleConnectorServicesProvider(userConfig).getSignatureServicePortType();
    }

    public SignatureServicePortTypeV755 getSignatureServicePortTypeV755(UserRuntimeConfig userConfig) {
        return getSingleConnectorServicesProvider(userConfig).getSignatureServicePortTypeV755();
    }

    public VSDServicePortType getVSDServicePortType(UserRuntimeConfig userConfig) {
        return getSingleConnectorServicesProvider(userConfig).getVSDServicePortType();
    }

    public ContextType getContextType(UserRuntimeConfig userConfig) {
        if (userConfig == null) {
            return defaultConnectorServicesProvider.getContextType();
        }
        ContextType contextType = new ContextType();
        // Use deprecated until default Konnektor config is still used
        contextType.setMandantId(userConfig.getMandantId());
        contextType.setClientSystemId(userConfig.getClientSystemId());
        contextType.setWorkplaceId(userConfig.getWorkplaceId());
        contextType.setUserId(userConfig.getUserId());
        return contextType;
    }

    public void clearAll() {
        portMap.clear();
    }
}
