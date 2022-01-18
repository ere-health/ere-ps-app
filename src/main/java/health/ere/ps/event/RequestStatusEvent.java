package health.ere.ps.event;

import javax.json.JsonObject;
import javax.websocket.Session;

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
