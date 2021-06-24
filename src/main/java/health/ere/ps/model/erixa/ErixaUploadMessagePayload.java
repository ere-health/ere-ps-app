package health.ere.ps.model.erixa;

import javax.json.JsonObject;

public class ErixaUploadMessagePayload {

    private String document;
    private String bundle;

    public ErixaUploadMessagePayload() {
    }


    public ErixaUploadMessagePayload(JsonObject payload) {
        document = payload.getString("document");
        bundle = payload.getString("bundle");
    }

    public String getDocument() {
        return document;
    }

    public String getBundle() {
        return bundle;
    }
}
