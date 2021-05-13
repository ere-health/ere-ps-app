package health.ere.ps.model.idp.client;

import health.ere.ps.model.idp.client.authentication.AuthenticationChallenge;

public class AuthorizationResponse {
    private AuthenticationChallenge authenticationChallenge;

    public AuthenticationChallenge getAuthenticationChallenge() {
        return authenticationChallenge;
    }

    public void setAuthenticationChallenge(AuthenticationChallenge authenticationChallenge) {
        this.authenticationChallenge = authenticationChallenge;
    }

    public static AuthorizationResponseBuilder builder() {
        return new AuthorizationResponseBuilder();
    }

    public static class AuthorizationResponseBuilder {
        private AuthorizationResponse authorizationResponse;

        public AuthorizationResponseBuilder() {
            authorizationResponse = new AuthorizationResponse();
        }

        public AuthorizationResponseBuilder authenticationChallenge(
                AuthenticationChallenge authenticationChallenge) {
            authorizationResponse.setAuthenticationChallenge(authenticationChallenge);
            return this;
        }

        public AuthorizationResponse build() {
            return authorizationResponse;
        }
    }
}
