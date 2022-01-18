package health.ere.ps.event;

import javax.json.JsonObject;
import javax.websocket.Session;

public class PrefillBundleEvent extends AbstractEvent {

    public PrefillBundleEvent() {

    }

    public PrefillBundleEvent(JsonObject object, Session replyTo, String id) {
        parseRuntimeConfig(object);
        this.replyTo = replyTo;
        this.id = id;
    }

}
