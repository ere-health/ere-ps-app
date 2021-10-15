package health.ere.ps.event;


import javax.json.JsonObject;
import javax.websocket.Session;


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
