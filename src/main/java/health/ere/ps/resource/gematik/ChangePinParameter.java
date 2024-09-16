package health.ere.ps.resource.gematik;

public class ChangePinParameter {
    public String cardHandle;
    public String pinType;

    public ChangePinParameter(String cardHandle, String pinType) {
        this.cardHandle = cardHandle;
        this.pinType = pinType;
    }
}