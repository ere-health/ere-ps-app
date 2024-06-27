package health.ere.ps.service.idp;

import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.retry.Retrier;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.idp.client.IdpClient;
import health.ere.ps.service.idp.client.IdpHttpClientService;
import health.ere.ps.websocket.ExceptionWithReplyToException;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.websocket.Session;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static health.ere.ps.service.connector.cards.ConnectorCardsService.CardHandleType.SMC_B;

@ApplicationScoped
@Startup
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


    @PostConstruct
    public void init() throws Exception {
        Thread thread = new Thread(() -> {
            List<Integer> retrySeconds = appConfig.getIdpInitializationRetriesSeconds();
            int retryPeriodMs = appConfig.getIdpInitializationPeriodMs();
            boolean initialized = Retrier.callAndRetry(retrySeconds, retryPeriodMs, this::initializeIdp, bool -> bool);
            if (!initialized) {
                String msg = String.format("Failed to init IDP client within %d seconds", retryPeriodMs / 1000);
                throw new RuntimeException(msg);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private Boolean initializeIdp() {
        String discoveryDocumentUrl = appConfig.getIdpBaseURL() + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;
        try {
            idpClient.init(appConfig.getIdpClientId(), appConfig.getIdpAuthRequestRedirectURL(), discoveryDocumentUrl, true);
            idpClient.initializeClient();
            return true;
        } catch (Exception e) {
            log.log(Level.WARNING, "IDP client initialization error: ", e);
            return false;
        }
    }

    public IdpClient getIdpClient(RuntimeConfig runtimeConfig) {
        return idpClient;
    }

    public String requestBearerToken() {
        return requestBearerToken(null);
    }

    public String requestBearerToken(RuntimeConfig runtimeConfig) {
        return requestBearerToken(runtimeConfig, null, null);
    }

    public String requestBearerToken(RuntimeConfig runtimeConfig, Session replyTo, String replyToMessageId) {
        try {
            boolean smcbHandleValid = runtimeConfig != null && runtimeConfig.getSMCBHandle() != null;
            String cardHandle = smcbHandleValid
                ? runtimeConfig.getSMCBHandle()
                : connectorCardsService.getConnectorCardHandle(SMC_B, runtimeConfig);

            X509Certificate x509Certificate = cardCertificateReaderService.retrieveSmcbCardCertificate(
                cardHandle, runtimeConfig
            );
            IdpTokenResult idpTokenResult = idpClient.login(x509Certificate, runtimeConfig);

            return idpTokenResult.getAccessToken().getRawString();
        } catch (Exception e) {
            log.log(Level.WARNING, "Idp login did not work, couldn't request bearer token", e);
            exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, replyTo, replyToMessageId));
            throw new RuntimeException(e);
        }
    }
}
