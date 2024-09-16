package health.ere.ps.event.erixa;


import jakarta.json.JsonObject;
import jakarta.websocket.Session;

import health.ere.ps.event.AbstractEvent;


public class ErixaEvent extends AbstractEvent {

    public final String processType;
    public final JsonObject payload;

    public ErixaEvent(JsonObject jsonObject) {
        payload = jsonObject.getJsonObject("payload");
        processType = jsonObject.getString("processType");
    }

    public ErixaEvent(JsonObject jsonObject, Session replyTo, String replyToMessageId) {
        this(jsonObject);
        this.replyTo = replyTo;
        this.replyToMessageId = replyToMessageId;
    }
}
