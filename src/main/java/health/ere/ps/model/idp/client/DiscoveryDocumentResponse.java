package health.ere.ps.model.idp.client;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class DiscoveryDocumentResponse {
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private X509Certificate idpSig;
    private PublicKey idpEnc;
    private X509Certificate discSig;

    public DiscoveryDocumentResponse(String authorizationEndpoint, String tokenEndpoint,
                                     X509Certificate idpSig, PublicKey idpEnc,
                                     X509Certificate discSig) {
        this.authorizationEndpoint = authorizationEndpoint;
        this.tokenEndpoint = tokenEndpoint;
        this.idpSig = idpSig;
        this.idpEnc = idpEnc;
        this.discSig = discSig;
    }

    public DiscoveryDocumentResponse() {
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public X509Certificate getIdpSig() {
        return idpSig;
    }

    public void setIdpSig(X509Certificate idpSig) {
        this.idpSig = idpSig;
    }

    public PublicKey getIdpEnc() {
        return idpEnc;
    }

    public void setIdpEnc(PublicKey idpEnc) {
        this.idpEnc = idpEnc;
    }

    public X509Certificate getDiscSig() {
        return discSig;
    }

    public void setDiscSig(X509Certificate discSig) {
        this.discSig = discSig;
    }

    public static DiscoveryDocumentResponseBuilder builder() {
        return new DiscoveryDocumentResponseBuilder();
    }

    public static class DiscoveryDocumentResponseBuilder {
        private DiscoveryDocumentResponse discoveryDocumentResponse;

        public DiscoveryDocumentResponseBuilder() {
            discoveryDocumentResponse = new DiscoveryDocumentResponse();
        }

        public DiscoveryDocumentResponseBuilder authorizationEndpoint(
                String authorizationEndpoint) {
            discoveryDocumentResponse.setAuthorizationEndpoint(authorizationEndpoint);

            return this;
        }

        public DiscoveryDocumentResponseBuilder tokenEndpoint(String tokenEndpoint) {
            discoveryDocumentResponse.setTokenEndpoint(tokenEndpoint);

            return this;
        }

        public DiscoveryDocumentResponseBuilder idpSig(X509Certificate idpSig) {
            discoveryDocumentResponse.setIdpSig(idpSig);

            return this;
        }

        public DiscoveryDocumentResponseBuilder idpEnc(PublicKey idpEnc) {
            discoveryDocumentResponse.setIdpEnc(idpEnc);

            return this;
        }

        public DiscoveryDocumentResponseBuilder discSig(X509Certificate discSig) {
            discoveryDocumentResponse.setDiscSig(discSig);

            return this;
        }

        public DiscoveryDocumentResponse build() {
            return discoveryDocumentResponse;
        }
    }
}
