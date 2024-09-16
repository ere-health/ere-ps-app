package health.ere.ps.event;

import jakarta.json.JsonObject;
import jakarta.websocket.Session;

public class PrefillBundleEvent extends AbstractEvent {

    String egkHandle;

    public PrefillBundleEvent() {

    }

    public PrefillBundleEvent(JsonObject object, Session replyTo, String id) {
        parseRuntimeConfig(object);
        setEgkHandle(object.getString("egkHandle", null));
        this.replyTo = replyTo;
        this.id = id;
    }

    public String getEgkHandle() {
        return egkHandle;
    }

    public void setEgkHandle(String egkHandle) {
        this.egkHandle = egkHandle;
    }

}
