package health.ere.ps.model.idp.client;

import health.ere.ps.model.idp.client.token.IdpJwe;
import health.ere.ps.model.idp.client.token.JsonWebToken;

public class AuthenticationRequest {
    private String authenticationEndpointUrl;
    private IdpJwe signedChallenge;
    private String ssoToken;
    private JsonWebToken challengeToken;

    public String getAuthenticationEndpointUrl() {
        return authenticationEndpointUrl;
    }

    public void setAuthenticationEndpointUrl(String authenticationEndpointUrl) {
        this.authenticationEndpointUrl = authenticationEndpointUrl;
    }

    public IdpJwe getSignedChallenge() {
        return signedChallenge;
    }

    public void setSignedChallenge(IdpJwe signedChallenge) {
        this.signedChallenge = signedChallenge;
    }

    public String getSsoToken() {
        return ssoToken;
    }

    public void setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    public JsonWebToken getChallengeToken() {
        return challengeToken;
    }

    public void setChallengeToken(JsonWebToken challengeToken) {
        this.challengeToken = challengeToken;
    }

    public static AuthenticationRequestBuilder builder() {
        return new AuthenticationRequestBuilder();
    }

    public static class AuthenticationRequestBuilder {
        private AuthenticationRequest authenticationRequest;

        public AuthenticationRequestBuilder() {
            authenticationRequest = new AuthenticationRequest();
        }

        public AuthenticationRequestBuilder authenticationEndpointUrl(
                String authenticationEndpointUrl) {
            authenticationRequest.setAuthenticationEndpointUrl(authenticationEndpointUrl);

            return this;
        }

        public AuthenticationRequestBuilder signedChallenge(IdpJwe signedChallenge) {
            authenticationRequest.setSignedChallenge(signedChallenge);

            return this;
        }

        public AuthenticationRequestBuilder ssoToken(String ssoToken) {
            authenticationRequest.setSsoToken(ssoToken);

            return this;
        }

        public AuthenticationRequestBuilder challengeToken(JsonWebToken challengeToken) {
            authenticationRequest.setChallengeToken(challengeToken);

            return this;
        }

        public AuthenticationRequest build() {
            return authenticationRequest;
        }
    }
}
