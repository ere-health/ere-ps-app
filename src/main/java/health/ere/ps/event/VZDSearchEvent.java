package health.ere.ps.event;


import javax.json.JsonObject;
import javax.websocket.Session;


public class VZDSearchEvent extends AbstractEvent {

    public VZDSearchEvent(JsonObject jsonObject) {
        parseRuntimeConfig(jsonObject);
    }

    public VZDSearchEvent(JsonObject object, Session replyTo, String id) {
        this(object);
        this.replyTo = replyTo;
        this.id = id;
    }
}
