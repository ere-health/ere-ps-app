package health.ere.ps.event;

import javax.websocket.Session;

import health.ere.ps.model.status.Status;

public class StatusResponseEvent extends AbstractEvent {
    private Status status;

    public StatusResponseEvent(Status status, Session replyTo, String id){
        this.status = status;
        this.replyTo = replyTo;
        this.replyToMessageId = id;
    }
    public Status getStatus() {
        return this.status;
    }

}
