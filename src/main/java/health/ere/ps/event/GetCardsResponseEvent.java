package health.ere.ps.event;


import javax.websocket.Session;

import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;


public class GetCardsResponseEvent extends AbstractEvent {

    GetCardsResponse getCardsResponse;

    public GetCardsResponseEvent(GetCardsResponse getCardsResponse, Session replyTo, String id) {
        this.getCardsResponse = getCardsResponse;
        this.replyTo = replyTo;
        this.replyToMessageId = id;
    }

    public GetCardsResponse getGetCardsResponse() {
        return this.getCardsResponse;
    }

    public void setGetCardsResponse(GetCardsResponse getCardsResponse) {
        this.getCardsResponse = getCardsResponse;
    }
}
