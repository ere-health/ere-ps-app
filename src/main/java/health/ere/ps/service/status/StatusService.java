package health.ere.ps.service.status;

import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import de.gematik.ws.conn.eventservice.v7.GetCards;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.event.RequestStatusEvent;
import health.ere.ps.event.StatusResponseEvent;
import health.ere.ps.model.status.Status;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.gematik.ERezeptWorkflowService;
import health.ere.ps.service.idp.BearerTokenService;

public class StatusService {

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;

    @Inject
    BearerTokenService bearerTokenService;

    @Inject
    ERezeptWorkflowService eRezeptWorkflowService;

    @Inject
    Event<StatusResponseEvent> statusResponseEvent;

    public Status getStatus(RuntimeConfig runtimeConfig) {
        Status status = new Status();

        try {
            GetCards parameter = new GetCards();
            parameter.setContext(connectorServicesProvider.getContextType(runtimeConfig));
            connectorServicesProvider.getEventServicePortType(runtimeConfig).getCards(parameter);
            status.setConnectorReachable(true);
        } catch(Exception ex) {
            status.setConnectorReachable(false);
        }

        // Call Bearer Service and check if it returns an access Code
        // bearerTokenService.requestBearerToken(runtimeConfig);

        // Call ERezeptServiceWorkflow isERezeptServiceReachable

        return status;
    }

    public void onRequestStatus(@ObservesAsync RequestStatusEvent requestStatusEvent) {
        // TODO: Extract runtime config from status request
        // call status from this service
        // create status response event with the data
        statusResponseEvent.fire(new StatusResponseEvent());
    }
    
}
