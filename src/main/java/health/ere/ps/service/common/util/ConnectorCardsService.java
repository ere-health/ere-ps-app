package health.ere.ps.service.common.util;

import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.v7.GetCards;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortType;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import health.ere.ps.config.AppConfig;


@ApplicationScoped
public class ConnectorCardsService {

    @Inject
    AppConfig appConfig;

    private ContextType contextType;
    private SignatureServicePortType signatureService;
    private EventServicePortType eventService;

    @PostConstruct
    void init() {
        contextType = new ContextType();
        contextType.setMandantId(appConfig.getSignatureServiceContextMandantId());
        contextType.setClientSystemId(appConfig.getSignatureServiceContextClientSystemId());
        contextType.setWorkplaceId(appConfig.getSignatureServiceContextWorkplaceId());
        contextType.setUserId(appConfig.getSignatureServiceContextUserId());
    }

    public GetCardsResponse getConnectorCards()
            throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        GetCards parameter = new GetCards();
        parameter.setContext(contextType);
        return eventService.getCards(parameter);
    }
}
