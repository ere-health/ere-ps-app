package health.ere.ps.model.idp.client.authentication;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.Key;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.IdpConstants;
import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.model.idp.client.token.IdpJwe;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import health.ere.ps.service.idp.crypto.CryptoLoader;
import health.ere.ps.service.idp.crypto.Nonce;
import health.ere.ps.service.idp.crypto.X509ClaimExtraction;

public class AuthenticationTokenBuilder {
    private IdpJwtProcessor jwtProcessor;
    private Key encryptionKey;
    private AuthenticationChallengeVerifier authenticationChallengeVerifier;

    public AuthenticationTokenBuilder(IdpJwtProcessor jwtProcessor, Key encryptionKey,
                                      AuthenticationChallengeVerifier authenticationChallengeVerifier) {
        this.jwtProcessor = jwtProcessor;
        this.encryptionKey = encryptionKey;
        this.authenticationChallengeVerifier = authenticationChallengeVerifier;
    }

    public AuthenticationTokenBuilder() {
    }

    public IdpJwe buildAuthenticationToken(
        final X509Certificate clientCertificate,
        final Map<String, Object> serverChallengeClaims,
        final ZonedDateTime issueingTime) {
        final Map<String, Object> claimsMap = X509ClaimExtraction.extractClaimsFromCertificate(clientCertificate);

        claimsMap.put(ClaimName.CLIENT_ID.getJoseName(),
                serverChallengeClaims.get(ClaimName.CLIENT_ID.getJoseName()));
        claimsMap.put(ClaimName.REDIRECT_URI.getJoseName(), serverChallengeClaims.get(ClaimName.REDIRECT_URI.getJoseName()));
        claimsMap.put(ClaimName.NONCE.getJoseName(), serverChallengeClaims.get(ClaimName.NONCE.getJoseName()));
        claimsMap.put(ClaimName.CODE_CHALLENGE.getJoseName(), serverChallengeClaims.get(ClaimName.CODE_CHALLENGE.getJoseName()));
        claimsMap
            .put(ClaimName.CODE_CHALLENGE_METHOD.getJoseName(), serverChallengeClaims.get(ClaimName.CODE_CHALLENGE_METHOD.getJoseName()));
        claimsMap.put(ClaimName.ISSUER.getJoseName(), serverChallengeClaims.get(ClaimName.ISSUER.getJoseName()));
        claimsMap.put(ClaimName.RESPONSE_TYPE.getJoseName(), serverChallengeClaims.get(ClaimName.RESPONSE_TYPE.getJoseName()));
        claimsMap.put(ClaimName.STATE.getJoseName(), serverChallengeClaims.get(ClaimName.STATE.getJoseName()));
        claimsMap.put(ClaimName.SCOPE.getJoseName(), serverChallengeClaims.get(ClaimName.SCOPE.getJoseName()));
        claimsMap.put(ClaimName.ISSUED_AT.getJoseName(), issueingTime.toEpochSecond());
        claimsMap.put(ClaimName.TOKEN_TYPE.getJoseName(), "code");
        claimsMap.put(ClaimName.AUTH_TIME.getJoseName(), issueingTime.toEpochSecond());
        claimsMap.put(ClaimName.SERVER_NONCE.getJoseName(), RandomStringUtils.randomAlphanumeric(20));
        claimsMap.put(ClaimName.JWT_ID.getJoseName(), new Nonce().getNonceAsHex(IdpConstants.JTI_LENGTH));

        final Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(ClaimName.TYPE.getJoseName(), "JWT");

        return getJwtProcessor().buildJwt(new JwtBuilder()
            .addAllBodyClaims(claimsMap)
            .addAllHeaderClaims(headerMap)
            .expiresAt(issueingTime.plusMinutes(1)))
            .encrypt(getEncryptionKey());
    }

    public IdpJwe buildAuthenticationTokenFromSsoToken(final JsonWebToken ssoToken,
                                                       final JsonWebToken challengeToken, final ZonedDateTime issueingTime) {
        final X509Certificate confirmationCertificate = extractConfirmationCertificate(ssoToken);

        final Map<String, Object> claimsMap = new HashMap<>();

        claimsMap.putAll(X509ClaimExtraction.extractClaimsFromCertificate(confirmationCertificate));

        claimsMap.put(ClaimName.CODE_CHALLENGE.getJoseName(),
                extractClaimFromChallengeToken(challengeToken, ClaimName.CODE_CHALLENGE));
        claimsMap.put(ClaimName.CODE_CHALLENGE_METHOD.getJoseName(),
            extractClaimFromChallengeToken(challengeToken, ClaimName.CODE_CHALLENGE_METHOD));
        claimsMap.put(ClaimName.NONCE.getJoseName(),
                extractClaimFromChallengeToken(challengeToken, ClaimName.NONCE));
        claimsMap.put(ClaimName.CLIENT_ID.getJoseName(),
                extractClaimFromChallengeToken(challengeToken, ClaimName.CLIENT_ID));
        claimsMap.put(ClaimName.REDIRECT_URI.getJoseName(),
                extractClaimFromChallengeToken(challengeToken, ClaimName.REDIRECT_URI));
        claimsMap.put(ClaimName.SCOPE.getJoseName(),
                extractClaimFromChallengeToken(challengeToken, ClaimName.SCOPE));
        claimsMap.put(ClaimName.ISSUED_AT.getJoseName(), issueingTime.toEpochSecond());
        claimsMap.put(ClaimName.STATE.getJoseName(),
                extractClaimFromChallengeToken(challengeToken, ClaimName.STATE));
        claimsMap.put(ClaimName.RESPONSE_TYPE.getJoseName(),
                extractClaimFromChallengeToken(challengeToken, ClaimName.RESPONSE_TYPE));
        claimsMap.put(ClaimName.TOKEN_TYPE.getJoseName(), "code");
        claimsMap.put(ClaimName.AUTH_TIME.getJoseName(), ZonedDateTime.now().toEpochSecond());
        claimsMap.put(ClaimName.SERVER_NONCE.getJoseName(),
                RandomStringUtils.randomAlphanumeric(20));
        claimsMap.put(ClaimName.ISSUER.getJoseName(),
                extractClaimFromChallengeToken(challengeToken, ClaimName.ISSUER));
        claimsMap.put(ClaimName.JWT_ID.getJoseName(),
                new Nonce().getNonceAsHex(IdpConstants.JTI_LENGTH));

        final Map<String, Object> headerClaims = new HashMap<>(ssoToken.getHeaderClaims());
        headerClaims.put(ClaimName.TYPE.getJoseName(), "JWT");

        return getJwtProcessor().buildJwt(new JwtBuilder()
            .replaceAllHeaderClaims(headerClaims)
            .replaceAllBodyClaims(claimsMap)
            .expiresAt(ZonedDateTime.now().plusHours(1)))
            .encrypt(getEncryptionKey());
    }

    private Object extractClaimFromChallengeToken(final JsonWebToken challengeToken, final ClaimName claimName) {
        return challengeToken.getBodyClaim(claimName)
            .orElseThrow(() -> new IdpJoseException("Unexpected structure in Challenge-Token"));
    }

    private X509Certificate extractConfirmationCertificate(final JsonWebToken ssoToken) {
        final String certString = ssoToken.getBodyClaim(ClaimName.CONFIRMATION)

            .filter(Map.class::isInstance)
            .map(Map.class::cast)
            .map(map -> map.get(ClaimName.X509_CERTIFICATE_CHAIN.getJoseName()))

            .filter(List.class::isInstance)
            .map(List.class::cast)
            .filter(list -> !list.isEmpty())
            .map(list -> list.get(0))

            .map(Object::toString)

            .orElseThrow(() -> new IdpJoseException(
                "Unsupported cnf-Structure found: Could not extract confirmed Certificate!"));

        final byte[] decode = Base64.getDecoder().decode(certString);

        return CryptoLoader.getCertificateFromPem(decode);
    }

    public IdpJwtProcessor getJwtProcessor() {
        return jwtProcessor;
    }

    public void setJwtProcessor(IdpJwtProcessor jwtProcessor) {
        this.jwtProcessor = jwtProcessor;
    }

    public Key getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(Key encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public AuthenticationChallengeVerifier getAuthenticationChallengeVerifier() {
        return authenticationChallengeVerifier;
    }

    public void setAuthenticationChallengeVerifier(AuthenticationChallengeVerifier authenticationChallengeVerifier) {
        this.authenticationChallengeVerifier = authenticationChallengeVerifier;
    }

    public static AuthenticationTokenBuilderBuilder builder() {
        return new AuthenticationTokenBuilderBuilder();
    }

    public static class AuthenticationTokenBuilderBuilder {
        private AuthenticationTokenBuilder authenticationTokenBuilder;

        public AuthenticationTokenBuilderBuilder() {
            authenticationTokenBuilder = new AuthenticationTokenBuilder();
        }

        public AuthenticationTokenBuilderBuilder jwtProcessor(IdpJwtProcessor jwtProcessor) {
            authenticationTokenBuilder.setJwtProcessor(jwtProcessor);

            return this;
        }

        public AuthenticationTokenBuilderBuilder encryptionKey(Key encryptionKey) {
            authenticationTokenBuilder.setEncryptionKey(encryptionKey);

            return this;
        }

        public AuthenticationTokenBuilderBuilder authenticationChallengeVerifier(
                AuthenticationChallengeVerifier authenticationChallengeVerifier) {
            authenticationTokenBuilder.setAuthenticationChallengeVerifier(
                    authenticationChallengeVerifier);

            return this;
        }

        public AuthenticationTokenBuilder build() {
            return authenticationTokenBuilder;
        }
    }
}
