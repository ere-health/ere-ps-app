package health.ere.ps.model.idp.client.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import health.ere.ps.exception.idp.crypto.IdpCryptoException;

import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;

import java.security.cert.X509Certificate;
import java.util.Base64;

public class IdpRsaKeyDescriptor extends IdpKeyDescriptor {

    @JsonProperty("n")
    private String rsaModulusValue;
    @JsonProperty("e")
    private String rsaExponentValue;

    public IdpRsaKeyDescriptor(final String[] x5c, final String publicKeyUse, final String keyId,
        final String keyType,
        final String rsaModulusValue, final String rsaExponentValue) {
        super(x5c, publicKeyUse, keyId, keyType);
        this.setRsaModulusValue(rsaModulusValue);
        this.setRsaExponentValue(rsaExponentValue);
    }

    public IdpRsaKeyDescriptor() {

    }

    public static IdpKeyDescriptor constructFromX509Certificate(final X509Certificate certificate, final String keyId,
        final boolean addX5C) {
        try {
            final IdpRsaKeyDescriptor.IdpRsaKeyDescriptorBuilder descriptorBuilder = IdpRsaKeyDescriptor.builder()
                .keyId(keyId)
                .keyType(getKeyType(certificate));
            if (addX5C) {
                try {
                    descriptorBuilder.x5c(getCertArray(certificate));
                } catch (IdpCryptoException e) {
                    throw new IllegalStateException(e);
                }
            }

            final BCRSAPublicKey bcrsaPublicKey = (BCRSAPublicKey) certificate.getPublicKey();
            descriptorBuilder
                .rsaModulusValue(
                    Base64.getUrlEncoder().withoutPadding().encodeToString(bcrsaPublicKey.getModulus().toByteArray()))
                .rsaExponentValue(
                    Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(bcrsaPublicKey.getPublicExponent().toByteArray()));

            return descriptorBuilder.build();
        } catch (final ClassCastException e) {
            throw new IllegalStateException("Unknown Key-Format encountered!", e);
        }
    }

    private static IdpRsaKeyDescriptor.IdpRsaKeyDescriptorBuilder builder() {
        return new IdpRsaKeyDescriptor.IdpRsaKeyDescriptorBuilder();
    }

    public String getRsaModulusValue() {
        return rsaModulusValue;
    }

    public void setRsaModulusValue(String rsaModulusValue) {
        this.rsaModulusValue = rsaModulusValue;
    }

    public String getRsaExponentValue() {
        return rsaExponentValue;
    }

    public void setRsaExponentValue(String rsaExponentValue) {
        this.rsaExponentValue = rsaExponentValue;
    }

    static class IdpRsaKeyDescriptorBuilder {
        private IdpRsaKeyDescriptor idpRsaKeyDescriptor;

        IdpRsaKeyDescriptorBuilder() {
            idpRsaKeyDescriptor = new IdpRsaKeyDescriptor();
        }

        public IdpKeyDescriptor build() {
            return idpRsaKeyDescriptor;
        }

        public IdpRsaKeyDescriptorBuilder keyId(String keyId) {
            idpRsaKeyDescriptor.setKeyId(keyId);
            return this;
        }

        public IdpRsaKeyDescriptorBuilder keyType(String keyType) {
            idpRsaKeyDescriptor.setKeyType(keyType);
            return this;
        }

        public IdpRsaKeyDescriptorBuilder x5c(String[] x5c) {
            idpRsaKeyDescriptor.setX5c(x5c);
            return this;
        }

        public IdpRsaKeyDescriptorBuilder rsaModulusValue(String rsaExponentValue) {
            idpRsaKeyDescriptor.setRsaModulusValue(rsaExponentValue);
            return this;
        }

        public IdpRsaKeyDescriptorBuilder rsaExponentValue(String rsaExponentValue) {
            idpRsaKeyDescriptor.setRsaExponentValue(rsaExponentValue);
            return this;
        }

    }
}
