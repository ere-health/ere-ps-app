package health.ere.ps.resource.gematik;

public class UpdateERezept {

    String taskId;
    String accessCode;
    String signedBytes;

    public String getTaskId() {
        return this.taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getAccessCode() {
        return this.accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getSignedBytes() {
        return this.signedBytes;
    }

    public void setSignedBytes(String signedBytes) {
        this.signedBytes = signedBytes;
    }
}
