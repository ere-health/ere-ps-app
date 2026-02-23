package health.ere.ps.service.connector.provider;

import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureServicePortType;
import de.gematik.ws.conn.cardservice.wsdl.v8_2.CardServicePortType;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortTypeV740;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortTypeV755;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;
import de.health.service.cetp.SubscriptionManager;
import de.health.service.cetp.config.KonnektorConfig;
import de.health.service.config.api.UserRuntimeConfig;
import health.ere.ps.config.SimpleUserConfig;
import health.ere.ps.config.UserConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@ApplicationScoped
public class MultiConnectorServicesProvider {

    private static final Logger log = Logger.getLogger(MultiConnectorServicesProvider.class.getName());

    @Inject
    SubscriptionManager subscriptionManager;

    @Inject
    DefaultConnectorServicesProvider defaultConnectorServicesProvider;

    @Inject
    Event<Exception> eventException;

    Map<SimpleUserConfig, SingleConnectorServicesProvider> singleConnectorServicesProvider = Collections.synchronizedMap(new HashMap<>());

    public CardServicePortType getCardServicePortType(UserConfig userConfig) {
        return getSingleConnectorServicesProvider(userConfig).getCardServicePortType();
    }

    public AbstractConnectorServicesProvider getSingleConnectorServicesProvider(UserRuntimeConfig userConfig) {
        if (userConfig == null) {
            return defaultConnectorServicesProvider;
        } else {
            SimpleUserConfig simpleUserConfig = new SimpleUserConfig(userConfig);
            if (!singleConnectorServicesProvider.containsKey(simpleUserConfig)) {
                log.fine("This key is not present in the map and will be inserted: " + simpleUserConfig);
                log.fine("The hashkey for it is: " + simpleUserConfig.hashCode());
                SingleConnectorServicesProvider servicesProvider = new SingleConnectorServicesProvider(userConfig, eventException);
                singleConnectorServicesProvider.put(simpleUserConfig, servicesProvider);
            }
            return singleConnectorServicesProvider.get(simpleUserConfig);
        }
    }

    public boolean isInitialized() {
        Collection<KonnektorConfig> konnektorConfigs = subscriptionManager.getKonnektorConfigs(null, null);
        Collection<SingleConnectorServicesProvider> providers = singleConnectorServicesProvider.values();
        int configsSize = konnektorConfigs.size();
        int providersSize = providers.size();
        return configsSize == providersSize && providers.stream().allMatch(SingleConnectorServicesProvider::isInitialized);
    }

    public CardServicePortType getCardServicePortType(UserRuntimeConfig userConfig) {
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
        contextType.setMandantId(userConfig.getMandantId());
        contextType.setClientSystemId(userConfig.getClientSystemId());
        contextType.setWorkplaceId(userConfig.getWorkplaceId());
        contextType.setUserId(userConfig.getUserId());
        return contextType;
    }

    public void clearAll() {
        singleConnectorServicesProvider = Collections.synchronizedMap(new HashMap<>());
    }
}
