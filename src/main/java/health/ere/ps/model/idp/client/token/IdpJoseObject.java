package health.ere.ps.model.idp.client.token;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import health.ere.ps.service.idp.crypto.CryptoLoader;
import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.model.idp.client.field.IdpScope;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static health.ere.ps.model.idp.client.field.ClaimName.AUTHENTICATION_CERTIFICATE;
import static health.ere.ps.model.idp.client.field.ClaimName.EXPIRES_AT;
import static health.ere.ps.model.idp.client.field.ClaimName.ISSUED_AT;
import static health.ere.ps.model.idp.client.field.ClaimName.SCOPE;
import static health.ere.ps.model.idp.client.field.ClaimName.X509_CERTIFICATE_CHAIN;

public abstract class IdpJoseObject {

    private final String rawString;
    private Map<String, Object> headerClaims;
    private Map<String, Object> bodyClaims;

    public IdpJoseObject(String rawString) {
        this.rawString = rawString;
    }

    public abstract Map<String, Object> extractHeaderClaims();

    public Map<String, Object> getHeaderClaims() {
        if (headerClaims == null) {
            headerClaims = extractHeaderClaims();
        }
        return headerClaims;
    }

    public abstract Map<String, Object> extractBodyClaims();

    public Map<String, Object> getBodyClaims() {
        if (bodyClaims == null) {
            bodyClaims = extractBodyClaims();
        }
        return bodyClaims;
    }

    public ZonedDateTime getExpiresAt() {
        return getDateTimeClaim(EXPIRES_AT, this::getBodyClaims)
            .orElseThrow();
    }

    public ZonedDateTime getExpiresAtBody() {
        return getBodyClaimAsZonedDateTime(EXPIRES_AT)
            .orElseThrow();
    }

    public ZonedDateTime getIssuedAt() {
        return getBodyClaimAsZonedDateTime(ISSUED_AT)
            .orElseThrow();
    }

    private Optional<ZonedDateTime> getBodyClaimAsZonedDateTime(final ClaimName claimName) {
        return getBodyClaims().entrySet().stream()
            .filter(entry -> claimName.getJoseName().equals(entry.getKey()))
            .map(Map.Entry::getValue)
            .map(TokenClaimExtraction::claimToZonedDateTime)
            .findAny();
    }

    public Set<IdpScope> getScopesBodyClaim() {
        return Optional
            .ofNullable(getBodyClaims().get(SCOPE.getJoseName()))
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .stream()
            .flatMap(value -> Stream.of(value.split(" ")))
            .map(IdpScope::fromJwtValue)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    }

    public Optional<String> getStringBodyClaim(final ClaimName claimName) {
        return Optional
            .ofNullable(getBodyClaims().get(claimName.getJoseName()))
            .filter(String.class::isInstance)
            .map(String.class::cast);
    }

    public Optional<ZonedDateTime> getBodyDateTimeClaim(final ClaimName claimName) {
        return getDateTimeClaim(claimName, this::getBodyClaims);
    }

    public Optional<ZonedDateTime> getHeaderDateTimeClaim(final ClaimName claimName) {
        return getDateTimeClaim(claimName, this::getHeaderClaims);
    }

    public Optional<ZonedDateTime> getDateTimeClaim(final ClaimName claimName,
        final Supplier<Map<String, Object>> claims) {
        return Optional
            .ofNullable(claims.get().get(claimName.getJoseName()))
            .filter(Long.class::isInstance)
            .map(Long.class::cast)
            .map(TokenClaimExtraction::claimToZonedDateTime);
    }

    public String getHeaderDecoded() {
        final String[] split = getRawString().split("\\.");
        if (split.length < 2) {
            throw new IllegalStateException("Could not retrieve Header: only found "
                + split.length + " parts!");
        }
        return StringUtils.newStringUtf8(Base64.decodeBase64(split[0]));
    }

    public String getPayloadDecoded() {
        final String[] split = getRawString().split("\\.");
        if (split.length < 2) {
            throw new IllegalStateException("Could not retrieve Body: only found "
                + split.length + " parts!");
        }
        return StringUtils.newStringUtf8(Base64.decodeBase64(split[1]));
    }

    public Optional<Object> getBodyClaim(final ClaimName claimName) {
        return Optional.ofNullable(getBodyClaims()
            .get(claimName.getJoseName()))
            .filter(Objects::nonNull);
    }

    public Optional<Object> getHeaderClaim(final ClaimName claimName) {
        return Optional.ofNullable(getHeaderClaims()
            .get(claimName.getJoseName()))
            .filter(Objects::nonNull);
    }

    public Optional<X509Certificate> getClientCertificateFromHeader() {
        return Optional.ofNullable(getHeaderClaims().get(X509_CERTIFICATE_CHAIN.getJoseName()))
            .filter(List.class::isInstance)
            .map(List.class::cast)
            .filter(list -> !list.isEmpty())
            .map(list -> list.get(0))
            .map(Object::toString)
            .map(java.util.Base64.getDecoder()::decode)
            .map(CryptoLoader::getCertificateFromPem);
    }

    public Optional<X509Certificate> getAuthenticationCertificate() {
        return getStringBodyClaim(AUTHENTICATION_CERTIFICATE)
            .map(java.util.Base64.getUrlDecoder()::decode)
            .map(CryptoLoader::getCertificateFromPem);
    }

    public Optional<JsonWebToken> getNestedJwtForClaimName(final ClaimName claimName) {
        return getStringBodyClaim(claimName)
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
            .map(JsonWebToken::new);
    }

    public String getRawString() {
        return rawString;
    }

    public static class Serializer extends JsonSerializer<IdpJoseObject> {

        @Override
        public void serialize(final IdpJoseObject value, final JsonGenerator gen, final SerializerProvider serializers)
            throws IOException {
            gen.writeString(value.getRawString());
        }
    }
}
