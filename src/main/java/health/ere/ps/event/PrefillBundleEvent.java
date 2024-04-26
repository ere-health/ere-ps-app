package health.ere.ps.event;

import jakarta.json.JsonObject;
import jakarta.websocket.Session;

public class PrefillBundleEvent extends AbstractEvent {

    public PrefillBundleEvent() {

    }

    public PrefillBundleEvent(JsonObject object, Session replyTo, String id) {
        parseRuntimeConfig(object);
        this.replyTo = replyTo;
        this.id = id;
    }

}
