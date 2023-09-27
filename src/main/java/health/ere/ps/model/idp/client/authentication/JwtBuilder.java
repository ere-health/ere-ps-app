package health.ere.ps.model.idp.client.authentication;

import static health.ere.ps.model.idp.client.brainPoolExtension.BrainpoolAlgorithmSuiteIdentifiers.BRAINPOOL256_USING_SHA256;

import java.security.Key;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jose4j.jca.ProviderContext;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;

import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import health.ere.ps.model.idp.crypto.PkiIdentity;

public class JwtBuilder {
    private final Map<String, Object> headerClaims = new HashMap<>();
    private final Map<String, Object> bodyClaims = new HashMap<>();
    private Key signerKey;
    private X509Certificate certificate;
    private boolean includeSignerCertificateInHeader = false;

    public JwtBuilder(Key signerKey, X509Certificate certificate, boolean includeSignerCertificateInHeader) {
        this.signerKey = signerKey;
        this.certificate = certificate;
        this.includeSignerCertificateInHeader = includeSignerCertificateInHeader;
    }

    public JwtBuilder() {
    }

    public JwtBuilder replaceAllBodyClaims(final Map<String, Object> additionalClaims) {
        bodyClaims.clear();
        bodyClaims.putAll(additionalClaims);
        return this;
    }

    public JwtBuilder addAllBodyClaims(final Map<String, Object> additionalClaims) {
        bodyClaims.putAll(additionalClaims);
        return this;
    }

    public JwtBuilder replaceAllHeaderClaims(final Map<String, Object> additionalClaims) {
        headerClaims.clear();
        headerClaims.putAll(additionalClaims);
        return this;
    }

    public JwtBuilder addAllHeaderClaims(final Map<String, Object> additionalClaims) {
        headerClaims.putAll(additionalClaims);
        return this;
    }

    public JwtBuilder addHeaderClaim(final ClaimName key, final Object value) {
        headerClaims.put(key.getJoseName(), value);
        return this;
    }

    public JwtBuilder addBodyClaim(final ClaimName key, final Object value) {
        bodyClaims.put(key.getJoseName(), value);
        return this;
    }

    public JwtBuilder expiresAt(final ZonedDateTime exp) {
        final NumericDate expDate = NumericDate.fromSeconds(exp.toEpochSecond());
        bodyClaims.put(ClaimName.EXPIRES_AT.getJoseName(), expDate.getValue());
        return this;
    }

    public JwtBuilder setSignerKey(final Key key) {
        signerKey = key;
        return this;
    }

    public JwtBuilder setCertificate(final X509Certificate certificate) {
        this.certificate = certificate;
        return this;
    }

    public JwtBuilder setIdentity(final PkiIdentity pkiIdentity) {
        certificate = pkiIdentity.getCertificate();
        signerKey = pkiIdentity.getPrivateKey();
        return this;
    }

    public JsonWebToken buildJwt() throws IdpCryptoException, IdpJoseException {
        Objects.requireNonNull(signerKey, "No private key supplied, cancelling JWT signing");

        final JwtClaims claims = new JwtClaims();
        bodyClaims.forEach((key, value) -> claims.setClaim(key, value));

        final JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(signerKey);
        jws.setAlgorithmHeaderValue(determineAlgorithm());

        headerClaims.keySet().forEach(key -> jws.setHeader(key, headerClaims.get(key)));

        if (includeSignerCertificateInHeader) {
            if (certificate == null) {
                throw new IdpJoseException("Could not include x5c-header: certificate not set");
            }
            jws.setCertificateChainHeaderValue(certificate);
        }

        try {
            ProviderContext providerCtx = new ProviderContext();
            providerCtx.getSuppliedKeyProviderContext().setSignatureProvider("BC");
            jws.setProviderContext(providerCtx);
            return new JsonWebToken(jws.getCompactSerialization());
        } catch (final JoseException e) {
            throw new IdpJoseException(e);
        }
    }

    private String determineAlgorithm() throws IdpCryptoException {
        if (signerKey instanceof ECPrivateKey) {
            return BRAINPOOL256_USING_SHA256;
        } else if (signerKey instanceof RSAPrivateKey) {
            return AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
        } else {
            throw new IdpCryptoException("Could not identify Signer-Key: " + signerKey.getClass().toString());
        }
    }

    public Map<String, Object> getClaims() {
        return bodyClaims;
    }

    public JwtBuilder includeSignerCertificateInHeader(final boolean shouldInclude) {
        includeSignerCertificateInHeader = shouldInclude;
        return this;
    }
}
