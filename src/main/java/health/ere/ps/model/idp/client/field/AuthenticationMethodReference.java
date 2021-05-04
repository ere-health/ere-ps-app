package health.ere.ps.model.idp.client.field;

public enum AuthenticationMethodReference {

    MULTI_FACTOR_AUTHENTICATON("mfa", false),
    SMARTCARD("sc", false),
    PIN("pin", false),
    HARDWARE_KEY("hwk", true),
    FACE_UNLOCK("face", true),
    FINGERPRINT("fpt", true),
    PASSWORD("pwd", true);

    private String description;
    private boolean isAlternativeAuthentication;

    AuthenticationMethodReference(String description, boolean isAlternativeAuthentication) {
        this.description = description;
        this.isAlternativeAuthentication = isAlternativeAuthentication;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAlternativeAuthentication() {
        return isAlternativeAuthentication;
    }
}
