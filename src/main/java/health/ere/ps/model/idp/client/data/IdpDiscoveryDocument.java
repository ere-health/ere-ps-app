package health.ere.ps.model.idp.client.data;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class IdpDiscoveryDocument {
    private String authorizationEndpoint;
    private String authPairEndpoint;
    private String ssoEndpoint;
    private String uriPair;
    private String tokenEndpoint;
    private String uriDisc;
    private String issuer;
    private String jwksUri;
    private long exp;
    private long iat;
    private String uriPukIdpEnc;
    private String uriPukIdpSig;
    private String[] subjectTypesSupported;
    private String[] idTokenSigningAlgValuesSupported;
    private String[] responseTypesSupported;
    private String[] scopesSupported;
    private String[] responseModesSupported;
    private String[] grantTypesSupported;
    private String[] acrValuesSupported;
    private String[] tokenEndpointAuthMethodsSupported;
    private String[] codeChallengeMethodsSupported;

    public IdpDiscoveryDocument(String authorizationEndpoint, String authPairEndpoint,
                                String ssoEndpoint, String uriPair, String tokenEndpoint,
                                String uriDisc, String issuer, String jwksUri, long exp, long iat,
                                String uriPukIdpEnc, String uriPukIdpSig,
                                String[] subjectTypesSupported,
                                String[] idTokenSigningAlgValuesSupported,
                                String[] responseTypesSupported, String[] scopesSupported,
                                String[] responseModesSupported, String[] grantTypesSupported,
                                String[] acrValuesSupported,
                                String[] tokenEndpointAuthMethodsSupported,
                                String[] codeChallengeMethodsSupported) {
        this.authorizationEndpoint = authorizationEndpoint;
        this.authPairEndpoint = authPairEndpoint;
        this.ssoEndpoint = ssoEndpoint;
        this.uriPair = uriPair;
        this.tokenEndpoint = tokenEndpoint;
        this.uriDisc = uriDisc;
        this.issuer = issuer;
        this.jwksUri = jwksUri;
        this.exp = exp;
        this.iat = iat;
        this.uriPukIdpEnc = uriPukIdpEnc;
        this.uriPukIdpSig = uriPukIdpSig;
        this.subjectTypesSupported = subjectTypesSupported;
        this.idTokenSigningAlgValuesSupported = idTokenSigningAlgValuesSupported;
        this.responseTypesSupported = responseTypesSupported;
        this.scopesSupported = scopesSupported;
        this.responseModesSupported = responseModesSupported;
        this.grantTypesSupported = grantTypesSupported;
        this.acrValuesSupported = acrValuesSupported;
        this.tokenEndpointAuthMethodsSupported = tokenEndpointAuthMethodsSupported;
        this.codeChallengeMethodsSupported = codeChallengeMethodsSupported;
    }

    public IdpDiscoveryDocument() {
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public String getAuthPairEndpoint() {
        return authPairEndpoint;
    }

    public void setAuthPairEndpoint(String authPairEndpoint) {
        this.authPairEndpoint = authPairEndpoint;
    }

    public String getSsoEndpoint() {
        return ssoEndpoint;
    }

    public void setSsoEndpoint(String ssoEndpoint) {
        this.ssoEndpoint = ssoEndpoint;
    }

    public String getUriPair() {
        return uriPair;
    }

    public void setUriPair(String uriPair) {
        this.uriPair = uriPair;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getUriDisc() {
        return uriDisc;
    }

    public void setUriDisc(String uriDisc) {
        this.uriDisc = uriDisc;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getJwksUri() {
        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public long getIat() {
        return iat;
    }

    public void setIat(long iat) {
        this.iat = iat;
    }

    public String getUriPukIdpEnc() {
        return uriPukIdpEnc;
    }

    public void setUriPukIdpEnc(String uriPukIdpEnc) {
        this.uriPukIdpEnc = uriPukIdpEnc;
    }

    public String getUriPukIdpSig() {
        return uriPukIdpSig;
    }

    public void setUriPukIdpSig(String uriPukIdpSig) {
        this.uriPukIdpSig = uriPukIdpSig;
    }

    public String[] getSubjectTypesSupported() {
        return subjectTypesSupported;
    }

    public void setSubjectTypesSupported(String[] subjectTypesSupported) {
        this.subjectTypesSupported = subjectTypesSupported;
    }

    public String[] getIdTokenSigningAlgValuesSupported() {
        return idTokenSigningAlgValuesSupported;
    }

    public void setIdTokenSigningAlgValuesSupported(String[] idTokenSigningAlgValuesSupported) {
        this.idTokenSigningAlgValuesSupported = idTokenSigningAlgValuesSupported;
    }

    public String[] getResponseTypesSupported() {
        return responseTypesSupported;
    }

    public void setResponseTypesSupported(String[] responseTypesSupported) {
        this.responseTypesSupported = responseTypesSupported;
    }

    public String[] getScopesSupported() {
        return scopesSupported;
    }

    public void setScopesSupported(String[] scopesSupported) {
        this.scopesSupported = scopesSupported;
    }

    public String[] getResponseModesSupported() {
        return responseModesSupported;
    }

    public void setResponseModesSupported(String[] responseModesSupported) {
        this.responseModesSupported = responseModesSupported;
    }

    public String[] getGrantTypesSupported() {
        return grantTypesSupported;
    }

    public void setGrantTypesSupported(String[] grantTypesSupported) {
        this.grantTypesSupported = grantTypesSupported;
    }

    public String[] getAcrValuesSupported() {
        return acrValuesSupported;
    }

    public void setAcrValuesSupported(String[] acrValuesSupported) {
        this.acrValuesSupported = acrValuesSupported;
    }

    public String[] getTokenEndpointAuthMethodsSupported() {
        return tokenEndpointAuthMethodsSupported;
    }

    public void setTokenEndpointAuthMethodsSupported(String[] tokenEndpointAuthMethodsSupported) {
        this.tokenEndpointAuthMethodsSupported = tokenEndpointAuthMethodsSupported;
    }

    public String[] getCodeChallengeMethodsSupported() {
        return codeChallengeMethodsSupported;
    }

    public void setCodeChallengeMethodsSupported(String[] codeChallengeMethodsSupported) {
        this.codeChallengeMethodsSupported = codeChallengeMethodsSupported;
    }

    public static IdpDiscoveryDocumentBuilder builder() {
        return new IdpDiscoveryDocumentBuilder();
    }

    public static class IdpDiscoveryDocumentBuilder {
        private IdpDiscoveryDocument idpDiscoveryDocument;

        public IdpDiscoveryDocumentBuilder() {
            idpDiscoveryDocument = new IdpDiscoveryDocument();
        }

        public IdpDiscoveryDocumentBuilder authorizationEndpoint(String authorizationEndpoint) {
            idpDiscoveryDocument.setAuthorizationEndpoint(authorizationEndpoint);

            return this;
        }

        public IdpDiscoveryDocumentBuilder authPairEndpoint(String authPairEndpoint) {
            idpDiscoveryDocument.setAuthPairEndpoint(authPairEndpoint);

            return this;
        }

        public IdpDiscoveryDocumentBuilder ssoEndpoint(String ssoEndpoint) {
            idpDiscoveryDocument.setSsoEndpoint(ssoEndpoint);

            return this;
        }

        public IdpDiscoveryDocumentBuilder uriPair(String uriPair) {
            idpDiscoveryDocument.setUriPair(uriPair);

            return this;
        }

        public IdpDiscoveryDocumentBuilder tokenEndpoint(String tokenEndpoint) {
            idpDiscoveryDocument.setTokenEndpoint(tokenEndpoint);

            return this;
        }

        public IdpDiscoveryDocumentBuilder uriDisc(String uriDisc) {
            idpDiscoveryDocument.setUriDisc(uriDisc);

            return this;
        }

        public IdpDiscoveryDocumentBuilder issuer(String issuer) {
            idpDiscoveryDocument.setIssuer(issuer);

            return this;
        }

        public IdpDiscoveryDocumentBuilder jwksUri(String issuer) {
            idpDiscoveryDocument.setJwksUri(issuer);

            return this;
        }

        public IdpDiscoveryDocumentBuilder exp(long exp) {
            idpDiscoveryDocument.setExp(exp);

            return this;
        }

        public IdpDiscoveryDocumentBuilder iat(long iat) {
            idpDiscoveryDocument.setIat(iat);

            return this;
        }

        public IdpDiscoveryDocumentBuilder uriPukIdpEnc(String uriPukIdpEnc) {
            idpDiscoveryDocument.setUriPukIdpEnc(uriPukIdpEnc);

            return this;
        }

        public IdpDiscoveryDocumentBuilder uriPukIdpSig(String uriPukIdpSig) {
            idpDiscoveryDocument.setUriPukIdpSig(uriPukIdpSig);

            return this;
        }

        public IdpDiscoveryDocumentBuilder subjectTypesSupported(String[] subjectTypesSupported) {
            idpDiscoveryDocument.setSubjectTypesSupported(subjectTypesSupported);

            return this;
        }

        public IdpDiscoveryDocumentBuilder idTokenSigningAlgValuesSupported(
                String[] idTokenSigningAlgValuesSupported) {
            idpDiscoveryDocument.setIdTokenSigningAlgValuesSupported(
                    idTokenSigningAlgValuesSupported);

            return this;
        }

        public IdpDiscoveryDocumentBuilder responseTypesSupported(
                String[] responseTypesSupported) {
            idpDiscoveryDocument.setResponseTypesSupported(responseTypesSupported);

            return this;
        }

        public IdpDiscoveryDocumentBuilder scopesSupported(String[] scopesSupported) {
            idpDiscoveryDocument.setScopesSupported(scopesSupported);

            return this;
        }

        public IdpDiscoveryDocumentBuilder responseModesSupported(String[] responseModesSupported) {
            idpDiscoveryDocument.setResponseModesSupported(responseModesSupported);

            return this;
        }

        public IdpDiscoveryDocumentBuilder grantTypesSupported(String[] grantTypesSupported) {
            idpDiscoveryDocument.setGrantTypesSupported(grantTypesSupported);

            return this;
        }

        public IdpDiscoveryDocumentBuilder acrValuesSupported(String[] acrValuesSupported) {
            idpDiscoveryDocument.setAcrValuesSupported(acrValuesSupported);

            return this;
        }

        public IdpDiscoveryDocumentBuilder tokenEndpointAuthMethodsSupported(
                String[] tokenEndpointAuthMethodsSupported) {
            idpDiscoveryDocument.setTokenEndpointAuthMethodsSupported(
                    tokenEndpointAuthMethodsSupported);

            return this;
        }

        public IdpDiscoveryDocumentBuilder codeChallengeMethodsSupported(
                String[] codeChallengeMethodsSupported) {
            idpDiscoveryDocument.setCodeChallengeMethodsSupported(codeChallengeMethodsSupported);

            return this;
        }

        public IdpDiscoveryDocument build() {
            return idpDiscoveryDocument;
        }
    }
}
