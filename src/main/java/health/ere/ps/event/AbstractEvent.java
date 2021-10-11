package health.ere.ps.event;

import javax.json.bind.annotation.JsonbTransient;
import javax.websocket.Session;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractEvent {
    
    @JsonbTransient
    @JsonIgnore
    protected String id;

    @JsonbTransient
    @JsonIgnore
    protected Session replyTo;

    @JsonbTransient
    @JsonIgnore
    protected String replyToMessageId;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }
    

    public Session getReplyTo() {
        return this.replyTo;
    }

    public void setReplyTo(Session replyTo) {
        this.replyTo = replyTo;
    }
    public String getReplyToMessageId() {
        return this.replyToMessageId;
    }

    public void setReplyToMessageId(String replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }
}
