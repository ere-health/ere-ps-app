package health.ere.ps.service.idp;

import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.idp.client.IdpClient;
import health.ere.ps.service.idp.client.IdpHttpClientService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
    AppConfig appConfig;
    @Inject
    IdpClient idpClient;
    @Inject
    CardCertificateReaderService cardCertificateReaderService;
    @Inject
    ConnectorCardsService connectorCardsService;
    @Inject
    Event<Exception> exceptionEvent;


    public String requestBearerToken() {
        try {
            String discoveryDocumentUrl = appConfig.getIdpBaseURL() + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;
            idpClient.init(appConfig.getIdpClientId(), appConfig.getIdpAuthRequestRedirectURL(), discoveryDocumentUrl, true);
            idpClient.initializeClient();

            String cardHandle = connectorCardsService.getConnectorCardHandle(
                    ConnectorCardsService.CardHandleType.SMC_B);

            X509Certificate x509Certificate =
                    cardCertificateReaderService.retrieveSmcbCardCertificate(cardHandle);
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
