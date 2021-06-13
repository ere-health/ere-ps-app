package health.ere.ps.event;

public class RequestBearerTokenFromIdpEvent {
    private String bearerToken;

    public String getBearerToken() {
        return this.bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

}
