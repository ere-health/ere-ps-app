package health.ere.ps.event;


import jakarta.json.JsonObject;
import jakarta.websocket.Session;


public class ActivateComfortSignatureEvent extends AbstractEvent {

    public ActivateComfortSignatureEvent(JsonObject jsonObject) {
        parseRuntimeConfig(jsonObject);
    }

    public ActivateComfortSignatureEvent(JsonObject object, Session replyTo, String id) {
        this(object);
        this.replyTo = replyTo;
        this.id = id;
    }
}
