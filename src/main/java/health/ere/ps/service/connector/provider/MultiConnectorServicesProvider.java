package health.ere.ps.service.connector.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureServicePortType;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardServicePortType;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortTypeV740;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortTypeV755;
import health.ere.ps.config.UserConfig;

@ApplicationScoped
public class MultiConnectorServicesProvider {
    private final static Logger log = Logger.getLogger(MultiConnectorServicesProvider.class.getName());

    @Inject
    DefaultConnectorServicesProvider defaultConnectorServicesProvider;
    Map<UserConfig,SingleConnectorServicesProvider> singleConnectorServicesProvider = new HashMap<>();

    public CardServicePortType getCardServicePortType(UserConfig userConfig) {
        CardServicePortType cardServicePortType = getSingleConnectorServicesProvider(userConfig).getCardServicePortType();
        return cardServicePortType;
    }

    private AbstractConnectorServicesProvider getSingleConnectorServicesProvider(UserConfig userConfig) {
        return userConfig == null ? defaultConnectorServicesProvider : singleConnectorServicesProvider.get(userConfig);
    }

    public CertificateServicePortType getCertificateService(UserConfig userConfig) {
        CertificateServicePortType certificateService = getSingleConnectorServicesProvider(userConfig).getCertificateService();
        return certificateService;
    }

    public EventServicePortType getEventServicePortType(UserConfig userConfig) {
        EventServicePortType eventServicePortType = getSingleConnectorServicesProvider(userConfig).getEventServicePortType();
        return eventServicePortType;
    }

    public AuthSignatureServicePortType getAuthSignatureServicePortType(UserConfig userConfig) {
        AuthSignatureServicePortType authSignatureServicePortType = getSingleConnectorServicesProvider(userConfig).getAuthSignatureServicePortType();
        return authSignatureServicePortType;
    }

    public SignatureServicePortTypeV740 getSignatureServicePortType(UserConfig userConfig) {
        SignatureServicePortTypeV740 signatureServicePortType = getSingleConnectorServicesProvider(userConfig).getSignatureServicePortType();
        return signatureServicePortType;
    }

    public SignatureServicePortTypeV755 getSignatureServicePortTypeV755(UserConfig userConfig) {
        SignatureServicePortTypeV755 signatureServicePortTypeV755 = getSingleConnectorServicesProvider(userConfig).getSignatureServicePortTypeV755();
        return signatureServicePortTypeV755;
    }

    public ContextType getContextType(UserConfig userConfig) {
        return null;
    }
}
