package health.ere.ps.model.idp.client.authentication;

import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.model.idp.client.token.JsonWebToken;

import org.apache.commons.lang3.Validate;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static health.ere.ps.model.idp.client.brainPoolExtension.BrainpoolAlgorithmSuiteIdentifiers.BRAINPOOL256_USING_SHA256;

public class IdpJwtProcessor {

    private X509Certificate certificate;
    private String algorithm;
    private Optional<String> keyId;
    private PrivateKey privateKey;

    public IdpJwtProcessor(final PkiIdentity identity) throws IdpCryptoException {
        this(identity.getCertificate());
        privateKey = identity.getPrivateKey();
        keyId = identity.getKeyId();
    }

    public IdpJwtProcessor(final X509Certificate certificate) throws IdpCryptoException {
        Validate.notNull(certificate);
        this.certificate = certificate;
        if (certificate.getPublicKey() instanceof ECPublicKey) {
            algorithm = BRAINPOOL256_USING_SHA256;
        } else if (certificate.getPublicKey() instanceof RSAPublicKey) {
            algorithm = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
        } else {
            throw new IdpCryptoException(
                "Could not identify Public-Key: " + certificate.getPublicKey().getClass().toString());
        }
    }

    public JsonWebToken buildJwt(final JwtBuilder jwtBuilder) throws IdpJoseException, IdpCryptoException {
        Validate.notNull(jwtBuilder);
        Objects.requireNonNull(privateKey, "No private key supplied, cancelling JWT signing");
        Objects.requireNonNull(jwtBuilder, "No Descriptor supplied, cancelling JWT signing");
        keyId.ifPresent(keyIdValue -> jwtBuilder.addHeaderClaim(ClaimName.KEY_ID, keyIdValue));
        return jwtBuilder
            .setSignerKey(privateKey)
            .setCertificate(certificate)
            .buildJwt();
    }

    public JsonWebToken buildJws(final String payload, final Map<String, Object> headerClaims,
        final boolean includeSignerCertificateInHeader) throws IdpJoseException {
        Validate.notNull(payload);
        Validate.notNull(headerClaims);

        final JsonWebSignature jws = new JsonWebSignature();

        jws.setPayload(payload);
        jws.setKey(privateKey);
        jws.setAlgorithmHeaderValue(algorithm);

        headerClaims.keySet().forEach(key -> jws.setHeader(key, headerClaims.get(key)));

        keyId.ifPresent(keyIdValue -> jws.setHeader(ClaimName.KEY_ID.getJoseName(), keyIdValue));

        if (includeSignerCertificateInHeader) {
            jws.setCertificateChainHeaderValue(certificate);
        }

        try {
            return new JsonWebToken(jws.getCompactSerialization());
        } catch (final JoseException e) {
            throw new IdpJoseException(e);
        }
    }

    public void verifyAndThrowExceptionIfFail(final JsonWebToken jwt) throws IdpJoseException {
        Validate.notNull(jwt);
        jwt.verify(certificate.getPublicKey());
    }

    public String getHeaderDecoded(final JsonWebToken jwt) {
        Validate.notNull(jwt);
        return jwt.getHeaderDecoded();
    }

    public String getPayloadDecoded(final JsonWebToken jwt) {
        Validate.notNull(jwt);
        return jwt.getPayloadDecoded();
    }
}
