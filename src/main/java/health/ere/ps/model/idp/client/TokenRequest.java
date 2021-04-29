package health.ere.ps.model.idp.client;

import java.security.PublicKey;

public class TokenRequest {
    private String tokenUrl;
    private String clientId;
    private String code;

    public TokenRequest(String tokenUrl, String clientId, String code, String codeVerifier,
                        String redirectUrl, String ssoToken, PublicKey idpEnc) {
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.code = code;
        this.codeVerifier = codeVerifier;
        this.redirectUrl = redirectUrl;
        this.ssoToken = ssoToken;
        this.idpEnc = idpEnc;
    }

    public TokenRequest() {}

    private String codeVerifier;
    private String redirectUrl;
    private String ssoToken;
    private PublicKey idpEnc;

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeVerifier() {
        return codeVerifier;
    }

    public void setCodeVerifier(String codeVerifier) {
        this.codeVerifier = codeVerifier;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getSsoToken() {
        return ssoToken;
    }

    public void setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    public PublicKey getIdpEnc() {
        return idpEnc;
    }

    public void setIdpEnc(PublicKey idpEnc) {
        this.idpEnc = idpEnc;
    }
}
