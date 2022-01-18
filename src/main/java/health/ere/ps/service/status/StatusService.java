package health.ere.ps.service.status;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.websocket.Session;

import de.gematik.ws.conn.eventservice.v7.GetCards;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.event.RequestStatusEvent;
import health.ere.ps.event.StatusResponseEvent;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.model.status.Status;
import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.cards.ConnectorCardsService.CardHandleType;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.gematik.ERezeptWorkflowService;
import health.ere.ps.service.idp.BearerTokenService;
import health.ere.ps.websocket.ExceptionWithReplyToExcetion;

@ApplicationScoped
public class StatusService {

    private static Logger log = Logger.getLogger(StatusService.class.getName());

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


    public void onRequestStatus(@ObservesAsync RequestStatusEvent requestStatusEvent) {
        try {
            Status  status  = getStatus(requestStatusEvent.getRuntimeConfig());
            Session session = requestStatusEvent.getReplyTo();
            String  id      = requestStatusEvent.getId();
            // create status response event with the data
            statusResponseEvent.fireAsync(new StatusResponseEvent(status, session, id));
        } catch(Exception e) {
            log.log(Level.WARNING, "Could not get status", e);
            exceptionEvent.fireAsync(new ExceptionWithReplyToExcetion(e, requestStatusEvent.getReplyTo(), requestStatusEvent.getId()));
        }
    }

    public Status getStatus(RuntimeConfig runtimeConfig) {
        Status status = new Status();
        String connectorBaseURL = userConfig.getConnectorBaseURL();

        boolean runtimeConfigurationsIsNotNull = (runtimeConfig != null && runtimeConfig.getConfigurations() != null);
        UserConfigurations configurations = runtimeConfigurationsIsNotNull ? runtimeConfig.getConfigurations() : userConfig.getConfigurations();
        String basicAuthUsername = configurations.getBasicAuthUsername();
        String basicAuthPassword = configurations.getBasicAuthPassword();
        String clientCertificate = configurations.getClientCertificate();
        String clientCertificatePassword = configurations.getClientCertificatePassword();

        try {
            if(clientCertificate == null && new File(appConfig.getCertAuthStoreFile().get()).exists()) {
                clientCertificate = appConfig.getCertAuthStoreFile().get();
            }
            if(clientCertificatePassword == null) {
                clientCertificatePassword = appConfig.getCertAuthStoreFilePassword().get();
            }
        } catch(Exception ex) {
            log.info("Did not find client certificate in app config.");
        }

        // ConnectorReachable
        try {
            connectorServicesProvider.getSingleConnectorServicesProvider(runtimeConfig).initializeServices(true);
            GetCards parameter = new GetCards();
            parameter.setContext(connectorServicesProvider.getContextType(runtimeConfig));
            connectorServicesProvider.getEventServicePortType(runtimeConfig).getCards(parameter);
            status.setConnectorReachable(true, connectorBaseURL);
        } catch(Exception ex) {
            status.setConnectorReachable(false, connectorBaseURL+", "
                                                +clientCertificate+":"+clientCertificatePassword+", "
                                                +basicAuthUsername+":"+basicAuthPassword+", "+
                                                secretsManagerService.getSslContext());
        }

        // IdpReachable
        String discoveryUrl = "Not given";
        try {
            bearerTokenService.getIdpClient(runtimeConfig).initializeClient();
            discoveryUrl = bearerTokenService.getIdpClient(runtimeConfig).getDiscoveryDocumentUrl();
            status.setIdpReachable(true, discoveryUrl);
        } catch (Exception e) {
            status.setIdpReachable(false, discoveryUrl+" Exception: "+e.getMessage());
        }

        String bearerToken = "";
        try {
            // IdpaccesstokenObtainable
            bearerToken = bearerTokenService.requestBearerToken(runtimeConfig);
            if (bearerToken != null && bearerToken.length() > 0 )
                status.setIdpaccesstokenObtainable(true, "Bearer Token: "+bearerToken, bearerToken);
            else
                status.setIdpaccesstokenObtainable(false,"");
        } catch(Exception e) {
            status.setIdpReachable(false, discoveryUrl+" Exception: "+e.getMessage());
        }

        // SmcbAvailable
        String smcbHandle = null;
        try {
            smcbHandle = connectorCardsService.getConnectorCardHandle(CardHandleType.SMC_B, runtimeConfig);
            status.setSmcbAvailable(true, "Card Handle: "+smcbHandle);
        } catch (Exception e) {
            status.setSmcbAvailable(false, "Exception: "+e.getMessage()+" Cause: "+(e.getCause() != null ? e.getCause().getMessage() : ""));
        }

        // CautReadable
        try {
            cardCertificateReaderService.doReadCardCertificate(smcbHandle, runtimeConfig);
            status.setCautReadable(true, "");
        } catch(Exception e) {
            status.setCautReadable(false, "Exception: "+e.getMessage()+" Cause: "+(e.getCause() != null ? e.getCause().getMessage() : ""));
        }

        // EhbaAvailable
        String ehbaHandle = null;
        try {
            ehbaHandle = connectorCardsService.getConnectorCardHandle(CardHandleType.HBA, runtimeConfig);
            status.setEhbaAvailable(true, "Card Handle: "+ehbaHandle);
        } catch (Exception e) {
            status.setEhbaAvailable(false, "Exception: "+e.getMessage()+" Cause: "+(e.getCause() != null ? e.getCause().getMessage() : ""));
        }
        // ComfortsignatureAvailable
        // Connector is PTV4+
        // check if basic auth or ssl certificate is enabled
        String connectorVersion = runtimeConfig != null ? runtimeConfig.getConnectorVersion() : null;
        connectorVersion = connectorVersion == null ? userConfig.getConnectorVersion() : null;
        if ("PTV4+".equals(connectorVersion) && (basicAuthUsername != null || clientCertificate != null)) {
            status.setComfortsignatureAvailable(true, "");
        } else {
            status.setComfortsignatureAvailable(false, "");
        }
            
        // FachdienstReachable
        status.setFachdienstReachable(eRezeptWorkflowService.isERezeptServiceReachable(runtimeConfig, bearerToken), "");

        return status;
    }
    
}
