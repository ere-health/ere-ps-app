package health.ere.ps.model.idp.client;

import health.ere.ps.model.idp.client.token.IdpJwe;
import health.ere.ps.model.idp.client.token.JsonWebToken;

import java.time.LocalDateTime;

public class IdpTokenResult {
    private JsonWebToken accessToken;
    private JsonWebToken idToken;
    private int expiresIn;
    private String tokenType;
    private IdpJwe ssoToken;
    private LocalDateTime validUntil;

    public IdpTokenResult(JsonWebToken accessToken, JsonWebToken idToken, int expiresIn,
                          String tokenType, IdpJwe ssoToken, LocalDateTime validUntil) {
        this.accessToken = accessToken;
        this.idToken = idToken;
        this.expiresIn = expiresIn;
        this.tokenType = tokenType;
        this.ssoToken = ssoToken;
        this.validUntil = validUntil;
    }

    public IdpTokenResult() {
    }

    public JsonWebToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(JsonWebToken accessToken) {
        this.accessToken = accessToken;
    }

    public JsonWebToken getIdToken() {
        return idToken;
    }

    public void setIdToken(JsonWebToken idToken) {
        this.idToken = idToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public IdpJwe getSsoToken() {
        return ssoToken;
    }

    public void setSsoToken(IdpJwe ssoToken) {
        this.ssoToken = ssoToken;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public static IdpTokenResultBuilder builder() {
        return new IdpTokenResultBuilder();
    }

    public static class IdpTokenResultBuilder {
        private IdpTokenResult idpTokenResult;

        public IdpTokenResultBuilder() {
            idpTokenResult = new IdpTokenResult();
        }

        public IdpTokenResultBuilder accessToken(JsonWebToken accessToken) {
            idpTokenResult.setAccessToken(accessToken);

            return this;
        }

        public IdpTokenResultBuilder idToken(JsonWebToken idToken) {
            idpTokenResult.setIdToken(idToken);

            return this;
        }

        public IdpTokenResultBuilder expiresIn(int expiresIn) {
            idpTokenResult.setExpiresIn(expiresIn);

            return this;
        }

        public IdpTokenResultBuilder tokenType(String tokenType) {
            idpTokenResult.setTokenType(tokenType);

            return this;
        }

        public IdpTokenResultBuilder ssoToken(IdpJwe ssoToken) {
            idpTokenResult.setSsoToken(ssoToken);

            return this;
        }

        public IdpTokenResultBuilder validUntil(LocalDateTime validUntil) {
            idpTokenResult.setValidUntil(validUntil);

            return this;
        }

        public IdpTokenResult build() {
            return idpTokenResult;
        }
    }
}
