package health.ere.ps.model.idp.crypto;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Optional;

public class PkiIdentity {
    private X509Certificate certificate;
    private PrivateKey privateKey;
    private Optional<String> keyId;
    private Optional<String> use;

    public PkiIdentity(X509Certificate certificate, PrivateKey privateKey, Optional<String> keyId,
                       Optional<String> use) {
        this.setCertificate(certificate);
        this.setPrivateKey(privateKey);
        this.setKeyId(keyId);
        this.setUse(use);
    }

    public PkiIdentity() {
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public Optional<String> getKeyId() {
        return keyId;
    }

    public void setKeyId(Optional<String> keyId) {
        this.keyId = keyId;
    }

    public Optional<String> getUse() {
        return use;
    }

    public void setUse(Optional<String> use) {
        this.use = use;
    }
}
