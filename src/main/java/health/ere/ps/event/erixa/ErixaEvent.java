package health.ere.ps.event.erixa;


import javax.json.JsonObject;


public class ErixaEvent {

    public final String processType;
    public final JsonObject payload;

    public ErixaEvent(JsonObject jsonObject) {
        payload = jsonObject.getJsonObject("payload");
        processType = payload.getString("processType");
    }
}
