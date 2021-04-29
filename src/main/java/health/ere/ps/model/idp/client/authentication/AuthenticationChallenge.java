package health.ere.ps.model.idp.client.authentication;

import health.ere.ps.model.idp.client.data.UserConsent;
import health.ere.ps.model.idp.client.token.JsonWebToken;


public class AuthenticationChallenge {
    private JsonWebToken challenge;
    private UserConsent userConsent;

    public AuthenticationChallenge(JsonWebToken challenge, UserConsent userConsent) {
        this.setChallenge(challenge);
        this.setUserConsent(userConsent);
    }

    public AuthenticationChallenge() {
    }

    public JsonWebToken getChallenge() {
        return challenge;
    }

    public void setChallenge(JsonWebToken challenge) {
        this.challenge = challenge;
    }

    public UserConsent getUserConsent() {
        return userConsent;
    }

    public void setUserConsent(UserConsent userConsent) {
        this.userConsent = userConsent;
    }

    public static AuthenticationChallengeBuilder builder() {
        return new AuthenticationChallengeBuilder();
    }

    public static class AuthenticationChallengeBuilder {
        private AuthenticationChallenge authenticationChallenge;

        public AuthenticationChallengeBuilder() {
            authenticationChallenge = new AuthenticationChallenge();
        }

        public AuthenticationChallengeBuilder challenge(JsonWebToken challenge) {
            authenticationChallenge.setChallenge(challenge);
            return this;
        }

        public AuthenticationChallengeBuilder userConsent(UserConsent userConsent) {
            authenticationChallenge.setUserConsent(userConsent);
            return this;
        }

        public AuthenticationChallenge build() {
            return authenticationChallenge;
        }
    }
}
