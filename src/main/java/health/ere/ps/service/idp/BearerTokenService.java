package health.ere.ps.service.idp;

import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.common.security.SecureSoapTransportConfigurer;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.idp.client.IdpClient;
import health.ere.ps.service.idp.client.IdpHttpClientService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class BearerTokenService {

    private static final Logger log = Logger.getLogger(BearerTokenService.class.getName());

    @Inject
    IdpClient idpClient;

    @Inject
    CardCertificateReaderService cardCertificateReaderService;

    @Inject
    AppConfig appConfig;

    @ConfigProperty(name = "idp.client.id")
    String clientId;

    @Inject
    ConnectorCardsService connectorCardsService;

    @Inject
    SecureSoapTransportConfigurer secureSoapTransportConfigurer;

    @ConfigProperty(name = "idp.base.url")
    String idpBaseUrl;

    @ConfigProperty(name = "idp.auth.request.redirect.url")
    String redirectUrl;


    @Inject
    Event<Exception> exceptionEvent;

    @PostConstruct
    void init() throws SecretsManagerException {
        secureSoapTransportConfigurer.init(connectorCardsService);
        secureSoapTransportConfigurer.configureSecureTransport(
                appConfig.getEventServiceEndpointAddress(),
                SecretsManagerService.SslContextType.TLS,
                appConfig.getIdpConnectorTlsCertTrustStore(),
                appConfig.getIdpConnectorTlsCertTustStorePwd());
    }

    public String requestBearerToken() {
        try {
            String discoveryDocumentUrl = idpBaseUrl + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;
            idpClient.init(clientId, redirectUrl, discoveryDocumentUrl, true);
            idpClient.initializeClient();

            String cardHandle = connectorCardsService.getConnectorCardHandle(
                    ConnectorCardsService.CardHandleType.SMC_B);

            X509Certificate x509Certificate =
                    cardCertificateReaderService.retrieveSmcbCardCertificate(appConfig.getMandantId(),
                            appConfig.getClientSystem(), appConfig.getWorkplace(),
                            cardHandle);
            IdpTokenResult idpTokenResult = idpClient.login(x509Certificate);

            return idpTokenResult.getAccessToken().getRawString();
        } catch (IdpClientException | IdpException | IdpJoseException |
                ConnectorCardCertificateReadException | ConnectorCardsException e) {
            log.log(Level.WARNING, "Idp login did not work, couldn't request bearer token", e);
            exceptionEvent.fireAsync(e);
        }
        return "";
    }
}
