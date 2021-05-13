package health.ere.ps.model.idp.client.data;

import java.util.Map;

public class UserConsent {
    private Map<String, String> requestedScopes;
    private Map<String, String> requestedClaims;

    public UserConsent(Map<String, String> requestedScopes, Map<String, String> requestedClaims) {
        this.requestedScopes = requestedScopes;
        this.requestedClaims = requestedClaims;
    }

    public UserConsent() {
    }

    public Map<String, String> getRequestedScopes() {
        return requestedScopes;
    }

    public void setRequestedScopes(Map<String, String> requestedScopes) {
        this.requestedScopes = requestedScopes;
    }

    public Map<String, String> getRequestedClaims() {
        return requestedClaims;
    }

    public void setRequestedClaims(Map<String, String> requestedClaims) {
        this.requestedClaims = requestedClaims;
    }

    public static UserConsentBuilder builder() {
        return new UserConsentBuilder();
    }

    public static class UserConsentBuilder {
        private UserConsent userConsent;

        public UserConsentBuilder() {
            userConsent = new UserConsent();
        }

        public UserConsentBuilder requestedScopes(Map<String, String> requestedScopes) {
            userConsent.setRequestedScopes(requestedScopes);

            return this;
        }

        public UserConsentBuilder requestedClaims(Map<String, String> requestedClaims) {
            userConsent.setRequestedClaims(requestedClaims);

            return this;
        }

        public UserConsent build() {
            return userConsent;
        }
    }
}
