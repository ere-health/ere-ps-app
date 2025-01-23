package health.ere.ps.event;

import jakarta.json.JsonObject;
import jakarta.websocket.Session;

public class PrefillBundleEvent extends AbstractEvent {

    String egkHandle;
    String smcbHandle;
    String hbaHandle;

    

    public PrefillBundleEvent() {

    }

    public PrefillBundleEvent(JsonObject object, Session replyTo, String id) {
        parseRuntimeConfig(object);
        setEgkHandle(object.getString("egkHandle", null));
        setSmcbHandle(object.getString("smcbHandle", null));
        setHbaHandle(object.getString("hbaHandle", null));
        this.replyTo = replyTo;
        this.id = id;
    }
    public String getEgkHandle() {
        return egkHandle;
    }

    public void setEgkHandle(String egkHandle) {
        this.egkHandle = egkHandle;
    }

    public String getSmcbHandle() {
        return smcbHandle;
    }

    public void setSmcbHandle(String smcbHandle) {
        this.smcbHandle = smcbHandle;
    }

    public String getHbaHandle() {
        return hbaHandle;
    }
    public void setHbaHandle(String hbaHandle) {
        this.hbaHandle = hbaHandle;
    }

}
