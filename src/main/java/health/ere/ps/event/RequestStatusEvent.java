package health.ere.ps.event;

import jakarta.json.JsonObject;
import jakarta.websocket.Session;

public class RequestStatusEvent extends AbstractEvent {

    public RequestStatusEvent(JsonObject jsonObject) {
        parseRuntimeConfig(jsonObject);
    }

    public RequestStatusEvent(JsonObject jsonObject, Session replyTo, String id) {
        this(jsonObject);
        this.replyTo = replyTo;
        this.id = id;
    }    
}
