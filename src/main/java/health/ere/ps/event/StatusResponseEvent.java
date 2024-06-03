package health.ere.ps.event;

import jakarta.websocket.Session;

import java.io.Serializable;

public class StatusResponseEvent extends AbstractEvent implements ReplyableEvent {

    private final Serializable payload;

    public StatusResponseEvent(Serializable status, Session replyTo, String id){
        this.payload = status;
        this.replyTo = replyTo;
        this.replyToMessageId = id;
    }

    public String getType() {
        return "StatusResponse";
    }

    public Serializable getPayload() {
        return this.payload;
    }
}
