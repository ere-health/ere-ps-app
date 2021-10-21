package health.ere.ps.event;

import javax.json.JsonObject;
import javax.json.bind.annotation.JsonbTransient;
import javax.websocket.Session;

import com.fasterxml.jackson.annotation.JsonIgnore;

import health.ere.ps.config.RuntimeConfig;

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

    @JsonbTransient
    @JsonIgnore
    protected RuntimeConfig runtimeConfig;

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

    public RuntimeConfig getRuntimeConfig() {
        return this.runtimeConfig;
    }

    public void setRuntimeConfig(RuntimeConfig runtimeConfig) {
        this.runtimeConfig = runtimeConfig;
    }

    public void parseRuntimeConfig(JsonObject object) {
        if(object != null && object.getJsonObject("runtimeConfig") != null) {
            this.runtimeConfig = new RuntimeConfig(object);
        }
    }
}
