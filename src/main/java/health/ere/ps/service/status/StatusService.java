package health.ere.ps.service.status;

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
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.status.Status;
import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.gematik.ERezeptWorkflowService;
import health.ere.ps.service.idp.BearerTokenService;
import health.ere.ps.service.idp.client.IdpClient;

@ApplicationScoped
public class StatusService {

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
    IdpClient idpClient;
        
    @Inject
    ERezeptWorkflowService eRezeptWorkflowService;

    @Inject
    Event<StatusResponseEvent> statusResponseEvent;


    public void onRequestStatus(@ObservesAsync RequestStatusEvent requestStatusEvent) {
        Status  status  = getStatus(requestStatusEvent.getRuntimeConfig());
        Session session = requestStatusEvent.getReplyTo();
        String  id      = requestStatusEvent.getId();
        // create status response event with the data
        statusResponseEvent.fireAsync(new StatusResponseEvent(status, session, id));
    }

    public Status getStatus(RuntimeConfig runtimeConfig) {
        Status status = new Status();
        String connectorBaseURL = userConfig.getConnectorBaseURL();
        String clientCertificate = userConfig.getConfigurations().getClientCertificate();
        String clientCertificatePassword = userConfig.getConfigurations().getClientCertificatePassword();

        // ConnectorReachable
        try {
            GetCards parameter = new GetCards();
            parameter.setContext(connectorServicesProvider.getContextType(runtimeConfig));
            connectorServicesProvider.getEventServicePortType(runtimeConfig).getCards(parameter);
            status.setConnectorReachable(true, connectorBaseURL);
        } catch(Exception ex) {
            String basicAuthUsername = userConfig.getConfigurations().getBasicAuthUsername();
            String basicAuthPassword = userConfig.getConfigurations().getBasicAuthPassword();
            status.setConnectorReachable(false, connectorBaseURL+", "
                                                +clientCertificate+":"+clientCertificatePassword+", "
                                                +basicAuthUsername+":"+basicAuthPassword+", "+
                                                secretsManagerService.getSslContext());
        }
        // IdpReachable
        try {
            idpClient.initializeClient();
            status.setIdpReachable(true, "");
        } catch (IdpClientException | IdpException | IdpJoseException e) {
            status.setIdpReachable(false, "");
        }

        // IdpaccesstokenObtainable
        String bearerToken = bearerTokenService.requestBearerToken(runtimeConfig);
        if (bearerToken != null && bearerToken.length() > 0 )
            status.setIdpaccesstokenObtainable(true, bearerToken.substring(0, 10)+"...");
        else
            status.setIdpaccesstokenObtainable(false,"");

        // Call ERezeptServiceWorkflow isERezeptServiceReachable

        // SmcbAvailable
        // CautReadable
        // EhbaAvailable
        // ComfortsignatureAvailable
        // FachdienstReachable

        return status;
    }
    
}
