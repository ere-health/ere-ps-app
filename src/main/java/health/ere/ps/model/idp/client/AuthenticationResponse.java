package health.ere.ps.model.idp.client;

public class AuthenticationResponse {
    private String code;
    private String location;
    private String ssoToken;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSsoToken() {
        return ssoToken;
    }

    public void setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    public static AuthenticationResponseBuilder builder() {
        return new AuthenticationResponseBuilder();
    }

    public static class AuthenticationResponseBuilder {
        private AuthenticationResponse authenticationResponse;

        public AuthenticationResponseBuilder() {
            authenticationResponse = new AuthenticationResponse();
        }

        public AuthenticationResponseBuilder code(String code) {
            authenticationResponse.setCode(code);

            return this;
        }

        public AuthenticationResponseBuilder location(String location) {
            authenticationResponse.setLocation(location);

            return this;
        }

        public AuthenticationResponseBuilder ssoToken(String ssoToken) {
            authenticationResponse.setSsoToken(ssoToken);

            return this;
        }

        public AuthenticationResponse build() {
            return authenticationResponse;
        }
    }
}
