package health.ere.ps.service.connector.provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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

@ApplicationScoped
public class MultiConnectorServicesProvider {
    private final static Logger log = Logger.getLogger(MultiConnectorServicesProvider.class.getName());

    @Inject
    DefaultConnectorServicesProvider defaultConnectorServicesProvider;

    @Inject
    Event<Exception> eventException;

    Map<SimpleUserConfig, SingleConnectorServicesProvider> singleConnectorServicesProvider = Collections.synchronizedMap(new HashMap<SimpleUserConfig, SingleConnectorServicesProvider>());

    public CardServicePortType getCardServicePortType(UserConfig userConfig) {
        CardServicePortType cardServicePortType = getSingleConnectorServicesProvider(userConfig).getCardServicePortType();
        return cardServicePortType;
    }

    public AbstractConnectorServicesProvider getSingleConnectorServicesProvider(UserConfig userConfig) {
        if(userConfig == null) {
            return defaultConnectorServicesProvider;
        } else {
        	SimpleUserConfig simpleUserConfig = new SimpleUserConfig(userConfig);
        	if(!singleConnectorServicesProvider.containsKey(simpleUserConfig)) {
        		singleConnectorServicesProvider.put(simpleUserConfig, new SingleConnectorServicesProvider(userConfig, eventException));
            }
            return singleConnectorServicesProvider.get(simpleUserConfig);
        }
    }

    public CertificateServicePortType getCertificateServicePortType(UserConfig userConfig) {
        CertificateServicePortType certificateService = getSingleConnectorServicesProvider(userConfig).getCertificateService();
        checkNull("certificateService", certificateService);
        return certificateService;
    }

    private void checkNull(String serviceName, Object service) {
        if(service == null) {
            log.warning(serviceName+" is null. This means it was not properly initialized when communicating with the connector. Please check the log for more information.");
        }
    }

    public EventServicePortType getEventServicePortType(UserConfig userConfig) {
        EventServicePortType eventServicePortType = getSingleConnectorServicesProvider(userConfig).getEventServicePortType();
        checkNull("eventServicePortType", eventServicePortType);
        return eventServicePortType;
    }

    public AuthSignatureServicePortType getAuthSignatureServicePortType(UserConfig userConfig) {
        AuthSignatureServicePortType authSignatureServicePortType = getSingleConnectorServicesProvider(userConfig).getAuthSignatureServicePortType();
        checkNull("authSignatureServicePortType", authSignatureServicePortType);
        return authSignatureServicePortType;
    }

    public SignatureServicePortTypeV740 getSignatureServicePortType(UserConfig userConfig) {
        SignatureServicePortTypeV740 signatureServicePortType = getSingleConnectorServicesProvider(userConfig).getSignatureServicePortType();
        checkNull("signatureServicePortType", signatureServicePortType);
        return signatureServicePortType;
    }

    public SignatureServicePortTypeV755 getSignatureServicePortTypeV755(UserConfig userConfig) {
        SignatureServicePortTypeV755 signatureServicePortTypeV755 = getSingleConnectorServicesProvider(userConfig).getSignatureServicePortTypeV755();
        checkNull("signatureServicePortTypeV755", signatureServicePortTypeV755);
        return signatureServicePortTypeV755;
    }

    public VSDServicePortType getVSDServicePortType(UserConfig userConfig) {
        VSDServicePortType vsdServicePortType = getSingleConnectorServicesProvider(userConfig).getVSDServicePortType();
        checkNull("vsdServicePortType", vsdServicePortType);
        return vsdServicePortType;
    }

    public ContextType getContextType(UserConfig userConfig) {
        if(userConfig == null) {
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
