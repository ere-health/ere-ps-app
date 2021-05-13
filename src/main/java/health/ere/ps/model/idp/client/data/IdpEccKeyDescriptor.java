package health.ere.ps.model.idp.client.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import health.ere.ps.exception.idp.crypto.IdpCryptoException;

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECPoint;

import java.security.cert.X509Certificate;
import java.util.Base64;

public class IdpEccKeyDescriptor extends IdpKeyDescriptor {

    @JsonProperty("crv")
    private String eccCurveName;
    @JsonProperty("x")
    private String eccPointXValue;
    @JsonProperty("y")
    private String eccPointYValue;

    public IdpEccKeyDescriptor(final String[] x5c, final String publicKeyUse, final String keyId,
        final String keyType,
        final String eccCurveName, final String eccPointXValue, final String eccPointYValue) {
        super(x5c, publicKeyUse, keyId, keyType);
        this.setEccCurveName(eccCurveName);
        this.setEccPointXValue(eccPointXValue);
        this.setEccPointYValue(eccPointYValue);
    }

    public IdpEccKeyDescriptor() {

    }

    public static IdpKeyDescriptor constructFromX509Certificate(final X509Certificate certificate, final String keyId,
        final boolean addX5C) {
        try {
            final IdpEccKeyDescriptor.IdpEccKeyDescriptorBuilder descriptorBuilder = IdpEccKeyDescriptor.builder()
                .keyId(keyId)
                .keyType(getKeyType(certificate));
            if (addX5C) {
                descriptorBuilder.x5c(getCertArray(certificate));
            }

            final BCECPublicKey bcecPublicKey = (BCECPublicKey) (certificate.getPublicKey());
            if (!((ECNamedCurveParameterSpec) bcecPublicKey.getParameters()).getName().equals("brainpoolP256r1")) {
                throw new IdpCryptoException(
                    "Unknown Key-Format encountered: '" + ((ECNamedCurveParameterSpec) bcecPublicKey.getParameters())
                        .getName() + "'!");
            }

            final ECPoint generator = bcecPublicKey.getQ();
            descriptorBuilder
                .eccCurveName("BP-256")
                .eccPointXValue(
                    Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(generator.getAffineXCoord().toBigInteger().toByteArray()))
                .eccPointYValue(
                    Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(generator.getAffineYCoord().toBigInteger().toByteArray()));

            return descriptorBuilder.build();
        } catch (final ClassCastException e) {
            throw new IdpCryptoException("Unknown Key-Format encountered!", e);
        }
    }

    public String getEccCurveName() {
        return eccCurveName;
    }

    public void setEccCurveName(String eccCurveName) {
        this.eccCurveName = eccCurveName;
    }

    public String getEccPointXValue() {
        return eccPointXValue;
    }

    public void setEccPointXValue(String eccPointXValue) {
        this.eccPointXValue = eccPointXValue;
    }

    public String getEccPointYValue() {
        return eccPointYValue;
    }

    public void setEccPointYValue(String eccPointYValue) {
        this.eccPointYValue = eccPointYValue;
    }

    public static IdpEccKeyDescriptorBuilder builder() {
        return new IdpEccKeyDescriptorBuilder();
    }

    static class IdpEccKeyDescriptorBuilder {
        private IdpEccKeyDescriptor idpEccKeyDescriptor;

        public IdpEccKeyDescriptorBuilder() {
            this.idpEccKeyDescriptor = new IdpEccKeyDescriptor();
        }

        public IdpEccKeyDescriptorBuilder keyId(String keyId) {
            idpEccKeyDescriptor.setKeyId(keyId);
            return this;
        }

        public IdpEccKeyDescriptorBuilder keyType(String keyType) {
            idpEccKeyDescriptor.setKeyType(keyType);
            return this;
        }

        public IdpEccKeyDescriptorBuilder x5c(String[] x5c) {
            idpEccKeyDescriptor.setX5c(x5c);
            return this;
        }

        public IdpEccKeyDescriptorBuilder eccPointXValue(String eccPointXValue) {
            idpEccKeyDescriptor.setEccPointXValue(eccPointXValue);
            return this;
        }

        public IdpEccKeyDescriptorBuilder eccPointYValue(String eccPointYValue) {
            idpEccKeyDescriptor.setEccPointYValue(eccPointYValue);
            return this;
        }
        
        public IdpEccKeyDescriptorBuilder eccCurveName(String eccCurveName) {
            idpEccKeyDescriptor.setEccCurveName(eccCurveName);
            return this;
        }

        public IdpKeyDescriptor build() {
            return idpEccKeyDescriptor;
        }
    }
}
