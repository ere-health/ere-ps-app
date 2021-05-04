package health.ere.ps.model.idp.client.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;

import health.ere.ps.model.idp.client.token.JsonWebToken;


public class AuthenticationResponse {

    @JsonProperty(value = "Signierte Antwort auf die Authentication Challenge. JWT, welches im x5c-Attribut das "
        + "Auth-Zertifikats des Aufrufers enthält. Im Body muss als 'njwt' Bit-genau die Original-Challenge des "
        + "Servers enthalten sein.")
    private JsonWebToken signedChallenge;

    public AuthenticationResponse(JsonWebToken signedChallenge) {
        this.setSignedChallenge(signedChallenge);
    }

    public AuthenticationResponse() {
    }

    public JsonWebToken getSignedChallenge() {
        return signedChallenge;
    }

    public void setSignedChallenge(JsonWebToken signedChallenge) {
        this.signedChallenge = signedChallenge;
    }

    public static AuthenticationResponseBuilder builder() {
        return new AuthenticationResponseBuilder();
    }

    public static class AuthenticationResponseBuilder {
        AuthenticationResponse authenticationResponse;

        public AuthenticationResponseBuilder() {
            authenticationResponse = new AuthenticationResponse();
        }

        public AuthenticationResponseBuilder signedChallenge(JsonWebToken signedChallenge) {
            authenticationResponse.setSignedChallenge(signedChallenge);

            return this;
        }

        public AuthenticationResponse build() {
            return authenticationResponse;
        }
    }
}
