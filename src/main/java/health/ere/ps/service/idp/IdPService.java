package health.ere.ps.service.idp;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;

import health.ere.ps.config.AppConfig;
import health.ere.ps.event.RequestBearerTokenFromIdpEvent;
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

@ApplicationScoped
public class IdPService {

    private static final Logger log = Logger.getLogger(IdPService.class.getName());
    
    @Inject
    IdpClient idpClient;

    @Inject
    CardCertificateReaderService cardCertificateReaderService;
    
    @Inject
    AppConfig appConfig;

    @Inject
    ConnectorCardsService connectorCardsService;

    @Inject
    SecureSoapTransportConfigurer secureSoapTransportConfigurer;


    @Inject
    Event<Exception> exceptionEvent;

    @PostConstruct
    void init() throws SecretsManagerException {
        secureSoapTransportConfigurer.init(connectorCardsService);
        secureSoapTransportConfigurer.configureSecureTransport(
                appConfig.getEventServiceEndpointAddress(),
                SecretsManagerService.SslContextType.TLS,
                appConfig.getConnectorCertAuthStoreFile(),
                appConfig.getConnectorCertAuthStoreFilePwd());
    }
    
    public void requestBearerToken(@Observes RequestBearerTokenFromIdpEvent requestBearerTokenFromIdpEvent) {
        try {
            String discoveryDocumentUrl = appConfig.getIdpBaseURL() + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;
            idpClient.init(appConfig.getClientId(), appConfig.getIdpAuthRequestRedirectURL(), discoveryDocumentUrl, true);
            idpClient.initializeClient();

            String cardHandle = connectorCardsService.getConnectorCardHandle(
                    ConnectorCardsService.CardHandleType.SMC_B);

            X509Certificate x509Certificate =
                    cardCertificateReaderService.retrieveSmcbCardCertificate(appConfig.getMandantId(),
                            appConfig.getClientSystemId(), appConfig.getWorkplaceId(),
                            cardHandle);

            IdpTokenResult idpTokenResult = idpClient.login(x509Certificate);
            requestBearerTokenFromIdpEvent.setBearerToken(idpTokenResult.getAccessToken().getRawString());
        } catch(IdpClientException | IdpException | IdpJoseException |
                ConnectorCardCertificateReadException | ConnectorCardsException e) {
            log.log(Level.WARNING, "Idp login did not work", e);
            exceptionEvent.fireAsync(e);
        }
    }
}
