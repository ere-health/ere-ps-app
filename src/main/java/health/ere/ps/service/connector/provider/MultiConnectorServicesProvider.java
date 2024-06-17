package health.ere.ps.service.connector.provider;

import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureServicePortType;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardServicePortType;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortTypeV740;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortTypeV755;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;
import health.ere.ps.config.SimpleUserConfig;
import health.ere.ps.config.UserConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@ApplicationScoped
public class MultiConnectorServicesProvider {

    private static final Logger log = Logger.getLogger(MultiConnectorServicesProvider.class.getName());

    @Inject
    DefaultConnectorServicesProvider defaultConnectorServicesProvider;

    @Inject
    Event<Exception> eventException;

    //Map<UserConfig, SingleConnectorServicesProvider> singleConnectorServicesProvider = new ConcurrentHashMap<>();
    Map<SimpleUserConfig, SingleConnectorServicesProvider> singleConnectorServicesProvider = Collections.synchronizedMap(new HashMap<SimpleUserConfig, SingleConnectorServicesProvider>());

    public AbstractConnectorServicesProvider getSingleConnectorServicesProvider(UserConfig userConfig) {
        if (userConfig == null) {
            return defaultConnectorServicesProvider;
        } else {
        	SimpleUserConfig simpleUserConfig = new SimpleUserConfig(userConfig);
        	if(!singleConnectorServicesProvider.containsKey(simpleUserConfig)) {
        		log.info("This key is not present in the map and will be inserted: " + simpleUserConfig.toString());
        		log.info("The hashkey for it is: " + simpleUserConfig.hashCode());
        		singleConnectorServicesProvider.put(simpleUserConfig, new SingleConnectorServicesProvider(userConfig, eventException));
            }
            return singleConnectorServicesProvider.get(simpleUserConfig);
        }
    }

    public CardServicePortType getCardServicePortType(UserConfig userConfig) {
        return getSingleConnectorServicesProvider(userConfig).getCardServicePortType();
    }

    public CertificateServicePortType getCertificateServicePortType(UserConfig userConfig) {
        return getSingleConnectorServicesProvider(userConfig).getCertificateService();
    }

    public EventServicePortType getEventServicePortType(UserConfig userConfig) {
        return getSingleConnectorServicesProvider(userConfig).getEventServicePortType();
    }

    public AuthSignatureServicePortType getAuthSignatureServicePortType(UserConfig userConfig) {
        return getSingleConnectorServicesProvider(userConfig).getAuthSignatureServicePortType();
    }

    public SignatureServicePortTypeV740 getSignatureServicePortType(UserConfig userConfig) {
        return getSingleConnectorServicesProvider(userConfig).getSignatureServicePortType();
    }

    public SignatureServicePortTypeV755 getSignatureServicePortTypeV755(UserConfig userConfig) {
        return getSingleConnectorServicesProvider(userConfig).getSignatureServicePortTypeV755();
    }

    public VSDServicePortType getVSDServicePortType(UserConfig userConfig) {
        return getSingleConnectorServicesProvider(userConfig).getVSDServicePortType();
    }

    public ContextType getContextType(UserConfig userConfig) {
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
    	singleConnectorServicesProvider = Collections.synchronizedMap(new HashMap<SimpleUserConfig, SingleConnectorServicesProvider>());
    }
}
