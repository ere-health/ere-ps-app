package health.ere.ps.model.idp.client.data;

import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.model.idp.client.field.IdpScope;

import java.util.Map;

public class UserConsentDescriptionTexts {
    private Map<IdpScope, String> scopes;
    private Map<ClaimName, String> claims;

    public UserConsentDescriptionTexts(Map<IdpScope, String> scopes, Map<ClaimName, String> claims) {
        this.setScopes(scopes);
        this.setClaims(claims);
    }

    public UserConsentDescriptionTexts() {
    }

    public static UserConsentDescriptionTextsBuilder builder() {
        return new UserConsentDescriptionTextsBuilder();
    }

    public Map<IdpScope, String> getScopes() {
        return scopes;
    }

    public void setScopes(Map<IdpScope, String> scopes) {
        this.scopes = scopes;
    }

    public Map<ClaimName, String> getClaims() {
        return claims;
    }

    public void setClaims(Map<ClaimName, String> claims) {
        this.claims = claims;
    }

    public static class UserConsentDescriptionTextsBuilder {
        private UserConsentDescriptionTexts userConsentDescriptionTexts;

        public UserConsentDescriptionTextsBuilder() {
            userConsentDescriptionTexts = new UserConsentDescriptionTexts();
        }

        public UserConsentDescriptionTextsBuilder scopes(Map<IdpScope, String> scopes) {
            userConsentDescriptionTexts.setScopes(scopes);
            return this;
        }

        public UserConsentDescriptionTextsBuilder claims(Map<ClaimName, String> claims) {
            userConsentDescriptionTexts.setClaims(claims);
            return this;
        }

        public UserConsentDescriptionTexts build() {
            return userConsentDescriptionTexts;
        }
    }
}
