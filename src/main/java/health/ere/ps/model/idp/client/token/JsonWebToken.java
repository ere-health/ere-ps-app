package health.ere.ps.model.idp.client.token;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.exception.idp.IdpJwtExpiredException;
import health.ere.ps.exception.idp.IdpJwtSignatureInvalidException;
import health.ere.ps.model.idp.client.field.ClaimName;
import org.jose4j.jwt.consumer.ErrorCodes;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import java.io.IOException;
import java.security.Key;
import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

@JsonSerialize(using = IdpJoseObject.Serializer.class)
@JsonDeserialize(using = JsonWebToken.Deserializer.class)
public class JsonWebToken extends IdpJoseObject {

    public JsonWebToken(final String rawString) {
        super(rawString);
    }

    public void verify(final PublicKey publicKey) throws IdpJoseException {
        final JwtConsumer jwtConsumer = new JwtConsumerBuilder()
            .setVerificationKey(publicKey)
            .setSkipDefaultAudienceValidation()
            .build();

        try {
            jwtConsumer.process(getRawString());
        } catch (final InvalidJwtException e) {
            if (e.getErrorDetails().stream()
                .anyMatch(error -> error.getErrorCode() == ErrorCodes.EXPIRED)) {
                throw new IdpJwtExpiredException(e);
            }
            if (e.getErrorDetails().stream()
                .anyMatch(error -> error.getErrorCode() == ErrorCodes.SIGNATURE_INVALID)) {
                throw new IdpJwtSignatureInvalidException(e);
            }
            throw new IdpJoseException("Invalid JWT encountered", e);
        }
    }

    public IdpJwe encrypt(final Key key) throws IdpJoseException {
        return IdpJwe.createWithPayloadAndExpiryAndEncryptWithKey("{\"njwt\":\"" + getRawString() + "\"}",
            findExpClaimInNestedJwts(), key, "NJWT");
    }

    public Optional<ZonedDateTime> findExpClaimInNestedJwts() {
        final Optional<ZonedDateTime> expClaim = getBodyDateTimeClaim(ClaimName.EXPIRES_AT);
        if (expClaim.isPresent()) {
            return expClaim;
        } else {
            final Optional<Object> njwtClaim = getBodyClaim(ClaimName.NESTED_JWT);
            if (njwtClaim.isPresent()) {
                try {
                    return new JsonWebToken(njwtClaim.get().toString())
                        .findExpClaimInNestedJwts();
                } catch (final Exception e) {
                    return Optional.empty();
                }
            }
            return Optional.empty();
        }
    }

    @Override
    public Map<String, Object> extractHeaderClaims() {
        try {
            return TokenClaimExtraction.extractClaimsFromJwtHeader(getRawString());
        } catch (IdpJoseException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Map<String, Object> extractBodyClaims() {
        try {
            return TokenClaimExtraction.extractClaimsFromJwtBody(getRawString());
        } catch (IdpJoseException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class Deserializer extends JsonDeserializer<IdpJoseObject> {

        @Override
        public IdpJoseObject deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
            return new JsonWebToken(ctxt.readValue(p, String.class));
        }
    }
}
