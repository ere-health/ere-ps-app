package health.ere.ps.model.idp.client;

import health.ere.ps.model.idp.client.authentication.AuthenticationChallenge;
import health.ere.ps.model.idp.client.token.JsonWebToken;

public class ImpfnachweisAuthorizationResponse extends AuthorizationResponse {
    private String challenge;

    private String location;

    public ImpfnachweisAuthorizationResponse(String challenge, String location) {
        this.challenge = challenge;
        this.location = location;
    }

    public String getChallenge() {
        return this.challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public AuthenticationChallenge getAuthenticationChallenge() {
        AuthenticationChallenge authenticationChallenge = new AuthenticationChallenge();
        JsonWebToken challengeJwt = new JsonWebToken("") {
            public String getRawString() {
                return challenge;
            }
        };
        authenticationChallenge.setChallenge(challengeJwt);
        
        return authenticationChallenge;
    }

}
