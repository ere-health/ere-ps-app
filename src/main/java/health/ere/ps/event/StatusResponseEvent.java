package health.ere.ps.event;

import java.io.Serializable;

import jakarta.websocket.Session;

public class StatusResponseEvent extends AbstractEvent implements ReplyableEvent {
    private String type = "StatusResponse";
    private Serializable payload;

    public StatusResponseEvent(Serializable status, Session replyTo, String id){
        this.payload = status;
        this.replyTo = replyTo;
        this.replyToMessageId = id;
    }

    public String getType() {
        return this.type;
    }

    public Serializable getPayload() {
        return this.payload;
    }

}
