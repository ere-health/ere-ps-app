package health.ere.ps.model.idp.client.authentication;

import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.exception.idp.ChallengeExpiredException;
import health.ere.ps.exception.idp.ChallengeSignatureInvalidException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.exception.idp.NoNestedJwtFoundException;
import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.model.idp.client.token.JsonWebToken;

import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

public class AuthenticationChallengeVerifier {

    private PkiIdentity serverIdentity;

    public AuthenticationChallengeVerifier(PkiIdentity serverIdentity) {
        this.serverIdentity = serverIdentity;
    }

    public AuthenticationChallengeVerifier() {
    }

    public void verifyResponseAndThrowExceptionIfFail(final JsonWebToken authenticationResponse) {
        final X509Certificate clientCertificate = extractClientCertificateFromChallenge(authenticationResponse)
            .orElseThrow(
                () -> new IdpJoseException("Could not extract client certificate from challenge response header"));

        performClientSignatureValidation(clientCertificate, authenticationResponse.getRawString());
        performServerSignatureValidationOfNjwt(authenticationResponse);
    }

    public void verifyResponseWithCertAndThrowExceptionIfFail(final X509Certificate authCert,
        final JsonWebToken authenticationResponse) {
        performClientSignatureValidation(authCert, authenticationResponse.getRawString());
    }

    public void verifyResponseWithPublicKeyAndThrowExceptionIfFail(final PublicKey publicKey,
        final JsonWebToken authenticationResponse) {
        performClientSignatureValidationWithKey(publicKey, authenticationResponse.getRawString());
    }

    private void performClientSignatureValidation(final X509Certificate clientCertificate,
        final String authResponse) {
        final JwtConsumer serverJwtConsumer = new JwtConsumerBuilder()
            .setVerificationKey(clientCertificate.getPublicKey())
            .setSkipDefaultAudienceValidation()
            .build();
        try {
            serverJwtConsumer.process(authResponse);
        } catch (final Exception e) {
            throw new ChallengeSignatureInvalidException(e);
        }
    }

    private void performClientSignatureValidationWithKey(final PublicKey publicKey,
        final String authResponse) {
        final JwtConsumer serverJwtConsumer = new JwtConsumerBuilder()
            .setVerificationKey(publicKey)
            .build();
        try {
            serverJwtConsumer.process(authResponse);
        } catch (final InvalidJwtException e) {
            throw new ChallengeSignatureInvalidException(e);
        }
    }

    private void performServerSignatureValidationOfNjwt(final JsonWebToken authenticationResponse) {
        final JsonWebToken serverChallenge = authenticationResponse.getBodyClaim(ClaimName.NESTED_JWT)
            .map(njwt -> new JsonWebToken(njwt.toString()))
            .orElseThrow(NoNestedJwtFoundException::new);

        if (serverChallenge.getExpiresAt().isBefore(ZonedDateTime.now())
            || serverChallenge.getExpiresAtBody().isBefore(ZonedDateTime.now())) {
            throw new ChallengeExpiredException();
        }
        try {
            serverChallenge.verify(getServerIdentity().getCertificate().getPublicKey());
        } catch (final Exception e) {
            throw new ChallengeSignatureInvalidException();
        }
    }

    public Optional<X509Certificate> extractClientCertificateFromChallenge(final JsonWebToken authenticationResponse) {
        return authenticationResponse.getClientCertificateFromHeader();
    }

    public Map<String, Object> extractClaimsFromSignedChallenge(final AuthenticationResponse authenticationResponse) {
        return authenticationResponse.getSignedChallenge().getBodyClaims();
    }

    public PkiIdentity getServerIdentity() {
        return serverIdentity;
    }

    public void setServerIdentity(PkiIdentity serverIdentity) {
        this.serverIdentity = serverIdentity;
    }

    public AuthenticationChallengeVerifierBuilder builder() {
        return new AuthenticationChallengeVerifierBuilder();
    }

    public static class AuthenticationChallengeVerifierBuilder {
        private AuthenticationChallengeVerifier authenticationChallengeVerifier;

        public AuthenticationChallengeVerifierBuilder() {
            authenticationChallengeVerifier = new AuthenticationChallengeVerifier();
        }

        public AuthenticationChallengeVerifierBuilder serverIdentity(PkiIdentity serverIdentity) {
            authenticationChallengeVerifier.setServerIdentity(serverIdentity);

            return this;
        }

        public AuthenticationChallengeVerifier build() {
            return authenticationChallengeVerifier;
        }
    }
}
