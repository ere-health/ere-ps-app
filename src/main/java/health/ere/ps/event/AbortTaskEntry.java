package health.ere.ps.event;

import jakarta.json.JsonObject;

public class AbortTaskEntry {
    private String accessCode;
    private String id;

    public AbortTaskEntry() {

    }

    public AbortTaskEntry(JsonObject jsonObject) {
        this.accessCode = jsonObject.getString("accessCode");
        this.id = jsonObject.getString("id");
    }

    public String getAccessCode() {
        return this.accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
