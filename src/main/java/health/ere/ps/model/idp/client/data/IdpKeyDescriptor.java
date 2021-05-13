package health.ere.ps.model.idp.client.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import health.ere.ps.model.idp.client.brainPoolExtension.BrainpoolCurves;
import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.exception.idp.IdpJoseException;

import org.jose4j.json.internal.json_simple.JSONAware;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import static health.ere.ps.service.idp.crypto.KeyAnalysis.isEcKey;

public class IdpKeyDescriptor implements JSONAware {

    @JsonInclude(Include.NON_NULL)
    private String[] x5c;
    @JsonInclude(Include.NON_NULL)
    @JsonProperty("use")
    private String publicKeyUse;
    @JsonProperty("kid")
    private String keyId;
    @JsonProperty("kty")
    private String keyType;

    {
        BrainpoolCurves.init();
    }

    public IdpKeyDescriptor(String[] x5c, String publicKeyUse, String keyId, String keyType) {
        this.setX5c(x5c);
        this.setPublicKeyUse(publicKeyUse);
        this.setKeyId(keyId);
        this.setKeyType(keyType);
    }

    public IdpKeyDescriptor() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdpKeyDescriptor)) return false;
        IdpKeyDescriptor that = (IdpKeyDescriptor) o;
        return Arrays.equals(getX5c(), that.getX5c()) &&
                getPublicKeyUse().equals(that.getPublicKeyUse()) &&
                Objects.equals(getKeyId(), that.getKeyId()) &&
                Objects.equals(getKeyType(), that.getKeyType());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getPublicKeyUse(), getKeyId(), getKeyType());
        result = 31 * result + Arrays.hashCode(getX5c());
        return result;
    }

    public static String[] getCertArray(final X509Certificate certificate) {
        try {
            return new String[]{
                Base64.getEncoder().encodeToString(
                    certificate.getEncoded())};
        } catch (final CertificateEncodingException e) {
            throw new IdpCryptoException("Error while retrieving key information", e);
        }
    }

    public static IdpKeyDescriptor constructFromX509Certificate(final X509Certificate certificate) {
        return constructFromX509Certificate(certificate, Optional.empty(), true);
    }

    public static IdpKeyDescriptor constructFromX509Certificate(final X509Certificate certificate,
        final Optional<String> keyId, final boolean addX5C) {
        if (isEcKey(certificate.getPublicKey())) {
            return IdpEccKeyDescriptor.constructFromX509Certificate(certificate,
                keyId.orElse(certificate.getSerialNumber().toString()), addX5C);
        } else {
            return IdpRsaKeyDescriptor.constructFromX509Certificate(certificate,
                keyId.orElse(certificate.getSerialNumber().toString()), addX5C);
        }
    }

    public static String getKeyType(final X509Certificate certificate) {
        if (isEcKey(certificate.getPublicKey())) {
            return EllipticCurveJsonWebKey.KEY_TYPE;
        } else {
            return RsaJsonWebKey.KEY_TYPE;
        }
    }

    @Override
    public String toJSONString() {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(Include.NON_NULL);
            return objectMapper
                .writeValueAsString(this);
        } catch (final JsonProcessingException e) {
            throw new IdpJoseException("Error during Claim serialization", e);
        }
    }

    public String[] getX5c() {
        return x5c;
    }

    public void setX5c(String[] x5c) {
        this.x5c = x5c;
    }

    public String getPublicKeyUse() {
        return publicKeyUse;
    }

    public void setPublicKeyUse(String publicKeyUse) {
        this.publicKeyUse = publicKeyUse;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }
}
