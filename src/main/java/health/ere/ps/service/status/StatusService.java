package health.ere.ps.service.status;

import de.gematik.ws.conn.eventservice.v7.GetCards;
import de.servicehealth.config.api.IUserConfigurations;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.event.RequestStatusEvent;
import health.ere.ps.event.StatusResponseEvent;
import health.ere.ps.model.status.Status;
import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.cards.ConnectorCardsService.CardHandleType;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.gematik.ERezeptWorkflowService;
import health.ere.ps.service.idp.BearerTokenService;
import health.ere.ps.service.idp.client.IdpClient;
import health.ere.ps.websocket.ExceptionWithReplyToException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class StatusService {

    private static final Logger log = Logger.getLogger(StatusService.class.getName());

    private final ExecutorService scheduledThreadPool = Executors.newFixedThreadPool(5);

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;

    @Inject
    UserConfig userConfig;

    @Inject
    AppConfig appConfig;

    @Inject
    SecretsManagerService secretsManagerService;

    @Inject
    BearerTokenService bearerTokenService;

    @Inject
    ConnectorCardsService connectorCardsService;

    @Inject
    CardCertificateReaderService cardCertificateReaderService;

    @Inject
    ERezeptWorkflowService eRezeptWorkflowService;

    @Inject
    Event<Exception> exceptionEvent;

    @Inject
    Event<StatusResponseEvent> statusResponseEvent;

    @Inject
    IdpClient idpClient;

    public void onRequestStatus(@ObservesAsync RequestStatusEvent requestStatusEvent) {
        try {
            Status status = getStatus(requestStatusEvent.getRuntimeConfig());
            Session session = requestStatusEvent.getReplyTo();
            String id = requestStatusEvent.getId();
            // create status response event with the data
            statusResponseEvent.fireAsync(new StatusResponseEvent(status, session, id));
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not get status", e);
            exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, requestStatusEvent.getReplyTo(), requestStatusEvent.getId()));
        }
    }

    private Pair<String, String> getClientCertificatePair(IUserConfigurations configurations) {
        String clientCertificate = configurations.getClientCertificate();
        String clientCertificatePassword = configurations.getClientCertificatePassword();
        try {
            if (clientCertificate == null && new File(appConfig.getCertAuthStoreFile().get()).exists()) {
                clientCertificate = appConfig.getCertAuthStoreFile().get();
            }
            if (clientCertificatePassword == null) {
                clientCertificatePassword = appConfig.getCertAuthStoreFilePassword().get();
            }
            return Pair.of(clientCertificate, clientCertificatePassword);
        } catch (Exception ex) {
            log.info("Did not find client certificate in app config.");
            return Pair.of(null, null);
        }
    }

    public Status getStatus(RuntimeConfig runtimeConfig) {
        Status status = new Status();
        String connectorBaseURL = userConfig.getConnectorBaseURL();

        IUserConfigurations configurations = runtimeConfig != null
            ? runtimeConfig.getConfigurations()
            : userConfig.getConfigurations();
        String basicAuthUsername = configurations.getBasicAuthUsername();
        String basicAuthPassword = configurations.getBasicAuthPassword();

        List<Future<?>> futures = new ArrayList<>();
        futures.add(scheduledThreadPool.submit(() -> {
            // ConnectorReachable
            Pair<String, String> pair = getClientCertificatePair(configurations);
            String clientCertificate = pair.getKey();
            String clientCertificatePassword = pair.getValue();
            try {
                connectorServicesProvider.getSingleConnectorServicesProvider(runtimeConfig).initializeServices(true);
                GetCards parameter = new GetCards();
                parameter.setContext(connectorServicesProvider.getContextType(runtimeConfig));
                connectorServicesProvider.getEventServicePortType(runtimeConfig).getCards(parameter);
                status.setConnectorReachable(true, connectorBaseURL);
            } catch (Exception ex) {
                status.setConnectorReachable(false, connectorBaseURL + ", "
                        + clientCertificate + ":" + clientCertificatePassword + ", "
                        + basicAuthUsername + ":" + basicAuthPassword + ", " +
                        secretsManagerService.getSslContext());
            }
        }));

        futures.add(scheduledThreadPool.submit(() -> {
            // IdpReachable
            String discoveryUrl = "Not given";
            try {
                idpClient.initializeClient();
                discoveryUrl = idpClient.getDiscoveryDocumentUrl();
                status.setIdpReachable(true, discoveryUrl);
            } catch (Throwable e) {
                status.setIdpReachable(false, discoveryUrl + " Exception: " + e.getMessage());
            }
        }));

        futures.add(scheduledThreadPool.submit(() -> {
            String discoveryUrl = "Not given";
            try {
                // IdpaccesstokenObtainable
                String bearerToken = bearerTokenService.getBearerToken(runtimeConfig);
                if (bearerToken != null && !bearerToken.isEmpty()) {
                    status.setIdpaccesstokenObtainable(true, "Bearer Token: " + bearerToken, bearerToken);
                    // FachdienstReachable
                    status.setFachdienstReachable(eRezeptWorkflowService.isERezeptServiceReachable(runtimeConfig, bearerToken), "");
                } else {
                    status.setIdpaccesstokenObtainable(false, "");
                }
            } catch (Exception e) {
                status.setIdpReachable(false, discoveryUrl + " Exception: " + e.getMessage());
            }
        }));

        futures.add(scheduledThreadPool.submit(() -> {
            // SmcbAvailable
            String smcbHandle = null;
            try {
                smcbHandle = connectorCardsService.getConnectorCardHandle(CardHandleType.SMC_B, runtimeConfig);
                status.setSmcbAvailable(true, "Card Handle: " + smcbHandle);
            } catch (Exception e) {
                status.setSmcbAvailable(false, "Exception: " + e.getMessage() + " Cause: " + (e.getCause() != null ? e.getCause().getMessage() : ""));
            }
            // CautReadable
            try {
                cardCertificateReaderService.doReadCardCertificate(smcbHandle, runtimeConfig);
                status.setCautReadable(true, "");
            } catch (Exception e) {
                status.setCautReadable(false, "Exception: " + e.getMessage() + " Cause: " + (e.getCause() != null ? e.getCause().getMessage() : ""));
            }
        }));

        futures.add(scheduledThreadPool.submit(() -> {
            // EhbaAvailable
            try {
                String ehbaHandle = connectorCardsService.getConnectorCardHandle(CardHandleType.HBA, runtimeConfig);
                status.setEhbaAvailable(true, "Card Handle: " + ehbaHandle);
            } catch (Exception e) {
                status.setEhbaAvailable(false, "Exception: " + e.getMessage() + " Cause: " + (e.getCause() != null ? e.getCause().getMessage() : ""));
            }
        }));

        futures.add(scheduledThreadPool.submit(() -> {
            // ComfortsignatureAvailable
            // Connector is PTV4+
            // check if basic auth or ssl certificate is enabled
            try {
                Pair<String, String> pair = getClientCertificatePair(configurations);
                String clientCertificate = pair.getKey();
                String connectorVersion = runtimeConfig != null ? runtimeConfig.getConnectorVersion() : null;
                connectorVersion = connectorVersion == null ? userConfig.getConnectorVersion() : null;
                if ("PTV4+".equals(connectorVersion) && (basicAuthUsername != null || clientCertificate != null)) {
                    status.setComfortsignatureAvailable(true, "");
                } else {
                    status.setComfortsignatureAvailable(false, "");
                }
            } catch (Exception e) {
                status.setComfortsignatureAvailable(false, e.getMessage());
            }
        }));

        for (Future<?> future : futures) {
            try {
                future.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error while building status", e);
            }
        }
        return status;
    }
}
