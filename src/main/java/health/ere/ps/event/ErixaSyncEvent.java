package health.ere.ps.event;

import javax.json.JsonObject;

public class ErixaSyncEvent {

    public final String document;
    public final String patient;

    public ErixaSyncEvent(JsonObject data) {
        document = data.getString("document");
        patient = data.getString("patient");
    }
}
