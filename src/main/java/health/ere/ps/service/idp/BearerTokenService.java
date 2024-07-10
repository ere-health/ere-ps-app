package health.ere.ps.service.idp;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.annotations.VisibleForTesting;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.client.token.JsonWebToken;
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
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static health.ere.ps.service.connector.cards.ConnectorCardsService.CardHandleType.SMC_B;

@ApplicationScoped
@Startup
public class BearerTokenService {
    private static final Logger log = Logger.getLogger(BearerTokenService.class.getName());

    private final AppConfig appConfig;
    private final IdpClient idpClient;
    private final CardCertificateReaderService cardCertificateReaderService;
    private final ConnectorCardsService connectorCardsService;
    private final Event<Exception> exceptionEvent;
    private final LoadingCache<RuntimeConfig, String> tokenCache;
    private final int refreshDurationInSeconds;

    private ScheduledExecutorService emergencyExecutor;

    @Inject
    public BearerTokenService(AppConfig appConfig,
                              IdpClient idpClient,
                              CardCertificateReaderService cardCertificateReaderService,
                              ConnectorCardsService connectorCardsService,
                              Event<Exception> exceptionEvent,
                              ExecutorService managedExecutor,
                              @ConfigProperty(name = "BearerTokenService.refreshDurationInSeconds", defaultValue = "240") int refreshDurationInSeconds,
                              @ConfigProperty(name = "BearerTokenService.expireDurationInSeconds", defaultValue = "290") int expireDurationInSeconds) {
        this.appConfig = appConfig;
        this.idpClient = idpClient;
        this.cardCertificateReaderService = cardCertificateReaderService;
        this.connectorCardsService = connectorCardsService;
        this.exceptionEvent = exceptionEvent;
        this.refreshDurationInSeconds = refreshDurationInSeconds;
        if (expireDurationInSeconds < 1) throw new IllegalArgumentException("expireDurationInSeconds must be positive");
        if (refreshDurationInSeconds >= expireDurationInSeconds)
            throw new IllegalArgumentException("refreshDurationInSeconds must be less then expireDurationInSeconds");
        tokenCache = Caffeine.newBuilder()
                .refreshAfterWrite(Duration.ofSeconds(refreshDurationInSeconds))
                .expireAfterAccess(Duration.ofSeconds(expireDurationInSeconds))
                .executor(managedExecutor)
                .build(this::requestBearerToken);
    }

    @PostConstruct
    public void init() {
        Thread thread = new Thread(() -> {
            List<Integer> retryMillis = appConfig.getIdpInitializationRetriesMillis();
            int retryPeriodMs = appConfig.getIdpInitializationPeriodMs();
            boolean initialized = Retrier.callAndRetry(retryMillis, retryPeriodMs, this::initializeIdp, bool -> bool);
            if (!initialized) {
                String msg = String.format("Failed to init IDP client within %d seconds", retryPeriodMs / 1000);
                throw new RuntimeException(msg);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private boolean initializeIdp() {
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

    @VisibleForTesting
    String requestBearerToken(RuntimeConfig runtimeConfig) {
        try {
            boolean smcbHandleValid = runtimeConfig != null && runtimeConfig.getSMCBHandle() != null;
            String cardHandle = smcbHandleValid
                    ? runtimeConfig.getSMCBHandle()
                    : connectorCardsService.getConnectorCardHandle(SMC_B, runtimeConfig);
            log.info(() -> "Request new bearer token for " + cardHandle);

            X509Certificate x509Certificate = cardCertificateReaderService.retrieveSmcbCardCertificate(
                    cardHandle, runtimeConfig
            );
            IdpTokenResult idpTokenResult = idpClient.login(x509Certificate, runtimeConfig);

            JsonWebToken accessToken = idpTokenResult.getAccessToken();
            ZonedDateTime expiresAt = accessToken.getExpiresAt();
            ZonedDateTime refreshAt = ZonedDateTime.now().plusSeconds(refreshDurationInSeconds);
            if (expiresAt.isBefore(refreshAt)) {
                //we expect a fix expiry duration of the jwt. This is a backup strategy if somebody changes the expiry but this is not supposed to happen
                log.severe(() -> "Bearer token expires before it is refreshed! Please change config 'BearerTokenService.refreshDurationInMinutes' to a value less then " + Duration.between(accessToken.getIssuedAt(), expiresAt));
                evictCacheEntryAt(runtimeConfig, expiresAt.minusSeconds(10));
            }
            return accessToken.getRawString();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Bearer token could not be obtained for IDP login", e);
            exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, null, null));
            throw new RuntimeException("Idp login did not work, couldn't request bearer token", e);
        }
    }

    @VisibleForTesting
    void evictCacheEntryAt(RuntimeConfig runtimeConfig, ZonedDateTime targetTime) {
        if (emergencyExecutor == null) {
            emergencyExecutor = Executors.newScheduledThreadPool(1);
        }
        long millisToSleep = Duration.between(ZonedDateTime.now(), targetTime).toMillis();
        if (millisToSleep > 0) {
            emergencyExecutor.schedule(() -> tokenCache.refresh(runtimeConfig), millisToSleep, TimeUnit.MILLISECONDS);
        }
    }

    @VisibleForTesting
    public void addBearerToken(RuntimeConfig config, String bearerTokens) {
        this.tokenCache.put(config, bearerTokens);
    }

    public String getBearerToken(RuntimeConfig runtimeConfig, Session replyTo, String messageId) {
        try {
            return tokenCache.get(runtimeConfig);
        } catch (Exception e) {
            exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, replyTo, messageId));
            throw new RuntimeException("Error requesting bearer token", e);
        }
    }

    public String getBearerToken(RuntimeConfig runtimeConfig) {
        return tokenCache.get(runtimeConfig);
    }
}
