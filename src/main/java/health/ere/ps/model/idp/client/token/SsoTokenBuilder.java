package health.ere.ps.model.idp.client.token;

import java.security.Key;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.model.idp.client.authentication.IdpJwtProcessor;
import health.ere.ps.model.idp.client.authentication.JwtBuilder;
import health.ere.ps.model.idp.client.brainPoolExtension.BrainpoolAlgorithmSuiteIdentifiers;
import health.ere.ps.model.idp.client.data.IdpKeyDescriptor;
import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.service.idp.crypto.X509ClaimExtraction;

public class SsoTokenBuilder {

    private IdpJwtProcessor jwtProcessor;
    private String issuerUrl;
    private Key tokenEncryptionKey;

    public IdpJwe buildSsoToken(final X509Certificate certificate, final ZonedDateTime issuingTime)
            throws IdpJoseException, IdpCryptoException {
        final Map<String, Object> bodyClaimsMap = new HashMap<>();
        final Map<String, Object> headerClaimsMap = new HashMap<>();
        headerClaimsMap.put(ClaimName.ALGORITHM.getJoseName(), BrainpoolAlgorithmSuiteIdentifiers.BRAINPOOL256_USING_SHA256);
        bodyClaimsMap.put(ClaimName.CONFIRMATION.getJoseName(), IdpKeyDescriptor.constructFromX509Certificate(certificate));
        headerClaimsMap.put(ClaimName.TYPE.getJoseName(), "JWT");
        bodyClaimsMap.put(ClaimName.ISSUER.getJoseName(), getIssuerUrl());
        bodyClaimsMap.put(ClaimName.ISSUED_AT.getJoseName(), issuingTime.toEpochSecond());
        bodyClaimsMap.put(ClaimName.AUTH_TIME.getJoseName(), issuingTime.toEpochSecond());

        bodyClaimsMap.putAll(X509ClaimExtraction.extractClaimsFromCertificate(certificate));
        return getJwtProcessor().buildJwt(new JwtBuilder()
            .addAllHeaderClaims(headerClaimsMap)
            .addAllBodyClaims(bodyClaimsMap)
            .expiresAt(issuingTime.plusHours(12)))
            .encrypt(getTokenEncryptionKey());
    }

    public IdpJwtProcessor getJwtProcessor() {
        return jwtProcessor;
    }

    public void setJwtProcessor(IdpJwtProcessor jwtProcessor) {
        this.jwtProcessor = jwtProcessor;
    }

    public String getIssuerUrl() {
        return issuerUrl;
    }

    public void setIssuerUrl(String issuerUrl) {
        this.issuerUrl = issuerUrl;
    }

    public Key getTokenEncryptionKey() {
        return tokenEncryptionKey;
    }

    public void setTokenEncryptionKey(Key tokenEncryptionKey) {
        this.tokenEncryptionKey = tokenEncryptionKey;
    }
}
