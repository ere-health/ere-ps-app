package health.ere.ps.model.idp.client;

import health.ere.ps.model.idp.client.field.CodeChallengeMethod;
import health.ere.ps.model.idp.client.field.IdpScope;

import java.util.Set;

public class AuthorizationRequest {

    private String link;
    private String clientId;
    private String codeChallenge;
    private CodeChallengeMethod codeChallengeMethod;
    private String redirectUri;
    private String state;
    private Set<IdpScope> scopes;
    private String nonce;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public void setCodeChallenge(String codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    public CodeChallengeMethod getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public void setCodeChallengeMethod(CodeChallengeMethod codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Set<IdpScope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<IdpScope> scopes) {
        this.scopes = scopes;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public static AuthorizationRequestBuilder builder() {
        return new AuthorizationRequestBuilder();
    }

    public static class AuthorizationRequestBuilder {
        private AuthorizationRequest authorizationRequest;

        public AuthorizationRequestBuilder() {
            authorizationRequest = new AuthorizationRequest();
        }

        public AuthorizationRequestBuilder link(String link) {
            authorizationRequest.setLink(link);

            return this;
        }

        public AuthorizationRequestBuilder clientId(String clientId) {
            authorizationRequest.setClientId(clientId);

            return this;
        }

        public AuthorizationRequestBuilder codeChallenge(String codeChallenge) {
            authorizationRequest.setCodeChallenge(codeChallenge);

            return this;
        }

        public AuthorizationRequestBuilder codeChallengeMethod(
                CodeChallengeMethod codeChallengeMethod) {
            authorizationRequest.setCodeChallengeMethod(codeChallengeMethod);

            return this;
        }

        public AuthorizationRequestBuilder redirectUri(String redirectUri) {
            authorizationRequest.setRedirectUri(redirectUri);

            return this;
        }

        public AuthorizationRequestBuilder state(String state) {
            authorizationRequest.setState(state);

            return this;
        }

        public AuthorizationRequestBuilder scopes(Set<IdpScope> scopes) {
            authorizationRequest.setScopes(scopes);

            return this;
        }

        public AuthorizationRequestBuilder nonce(String nonce) {
            authorizationRequest.setNonce(nonce);

            return this;
        }

        public AuthorizationRequest build() {
            return authorizationRequest;
        }

    }
}

