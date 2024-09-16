package health.ere.ps.event;


import jakarta.json.JsonObject;
import jakarta.websocket.Session;


public class DeactivateComfortSignatureEvent extends AbstractEvent {

    public DeactivateComfortSignatureEvent(JsonObject jsonObject) {
        parseRuntimeConfig(jsonObject);
    }

    public DeactivateComfortSignatureEvent(JsonObject jsonObject, Session replyTo, String id) {
        this(jsonObject);
        this.replyTo = replyTo;
        this.id = id;
    }
}
