package health.ere.ps.model.idp.client.token;

import org.jose4j.json.JsonUtil;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;

import health.ere.ps.exception.idp.IdpJoseException;

public class TokenClaimExtraction {

    private TokenClaimExtraction() {

    }

    /**
     * @param token jwt as string
     * @return Claims as a map of key value strings
     * @desc Implements the extraction of claims from json web tokens
     */
    public static Map<String, Object> extractClaimsFromJwtBody(final String token) throws IdpJoseException {
        final JwtConsumer jwtConsumer = new JwtConsumerBuilder()
            .setSkipSignatureVerification()
            .setSkipDefaultAudienceValidation()
            .setSkipAllValidators()
            .build();

        try {
            return jwtConsumer.process(token).getJwtClaims().getClaimsMap();
        } catch (final InvalidJwtException e) {
            throw new IdpJoseException(e);
        }
    }

    public static Map<String, Object> extractClaimsFromJwtHeader(final String token) throws IdpJoseException {
        final JsonWebSignature jsonWebSignature = new JsonWebSignature();
        try {
            jsonWebSignature.setCompactSerialization(token);
            return JsonUtil.parseJson(jsonWebSignature.getHeaders().getFullHeaderAsJsonString());
        } catch (final JoseException e) {
            throw new IdpJoseException(e);
        }
    }

    public static ZonedDateTime claimToZonedDateTime(final Long claim) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(claim * 1000), ZoneOffset.UTC);
    }

    public static long zonedDateTimeToClaim(final ZonedDateTime dateTime) {
        return dateTime.toEpochSecond();
    }

    public static ZonedDateTime claimToZonedDateTime(final Object claim) {
        Objects.requireNonNull(claim);

        if (claim instanceof String) {
            return claimToZonedDateTime(Long.parseLong((String) claim));
        } else if (claim instanceof Long) {
            return claimToZonedDateTime((Long) claim);
        } else if (claim instanceof Integer) {
            return claimToZonedDateTime(Integer.toUnsignedLong((Integer) claim));
        } else {
            throw new IllegalArgumentException("Couldn't convert claim: " + claim.getClass().getSimpleName());
        }
    }
}
