package health.ere.ps.model.idp.client.token;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.jose4j.json.JsonUtil;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.lang.JoseException;

import java.io.IOException;
import java.security.Key;
import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.field.ClaimName;

@JsonSerialize(using = IdpJoseObject.Serializer.class)
@JsonDeserialize(using = IdpJwe.Deserializer.class)
public class IdpJwe extends IdpJoseObject {

    private Key decryptionKey;

    public IdpJwe(final String rawString) {
        super(rawString);
    }

    public static IdpJwe createWithPayloadAndEncryptWithKey(final String payload, final Key key,
        final String contentType) {
        return createWithPayloadAndExpiryAndEncryptWithKey(payload, Optional.empty(), key, contentType);
    }

    public static IdpJwe createWithPayloadAndExpiryAndEncryptWithKey(final String payload,
        final Optional<ZonedDateTime> expiryOptional, final Key key, final String contentType) {
        final JsonWebEncryption jwe = new JsonWebEncryption();

        jwe.setPlaintext(payload);
        if (key instanceof PublicKey) {
            jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.ECDH_ES);
        } else {
            jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.DIRECT);
        }
        jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_256_GCM);
        jwe.setKey(key);
        expiryOptional
            .map(TokenClaimExtraction::zonedDateTimeToClaim)
            .ifPresent(expValue -> jwe.setHeader(ClaimName.EXPIRES_AT.getJoseName(), expValue));
        jwe.setHeader(ClaimName.CONTENT_TYPE.getJoseName(), contentType);

        try {
            return new IdpJwe(jwe.getCompactSerialization());
        } catch (final JoseException e) {
            throw new IllegalStateException("Error during token encryption", e);
        }
    }

    public JsonWebToken decryptNestedJwt(final Key key) {
        setDecryptionKey(key);
        try {
            return new JsonWebToken(getStringBodyClaim(ClaimName.NESTED_JWT)
                .orElseThrow(() -> new IdpJoseException("Could not find njwt")));
        } catch (IdpJoseException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public ZonedDateTime getExpiresAt() {
        return getDateTimeClaim(ClaimName.EXPIRES_AT, this::getHeaderClaims)
            .orElseThrow();
    }

    public String decryptJweAndReturnPayloadString(final Key key) throws IdpJoseException {
        final JsonWebEncryption receiverJwe = new JsonWebEncryption();

        receiverJwe.setAlgorithmConstraints(
            new AlgorithmConstraints(ConstraintType.PERMIT,
                KeyManagementAlgorithmIdentifiers.DIRECT,
                KeyManagementAlgorithmIdentifiers.ECDH_ES));
        receiverJwe.setContentEncryptionAlgorithmConstraints(
            new AlgorithmConstraints(ConstraintType.PERMIT,
                ContentEncryptionAlgorithmIdentifiers.AES_256_GCM));

        try {
            receiverJwe.setCompactSerialization(getRawString());
            receiverJwe.setKey(key);

            return receiverJwe.getPlaintextString();
        } catch (final JoseException e) {
            throw new IdpJoseException("Error during decryption", e);
        }
    }

    @Override
    public Map<String, Object> extractHeaderClaims() {
        final JsonWebEncryption jwe = new JsonWebEncryption();
        try {
            jwe.setCompactSerialization(getRawString());
            return JsonUtil.parseJson(jwe.getHeaders().getFullHeaderAsJsonString());
        } catch (final JoseException e) {
            throw new IllegalStateException(e);
        }
    }

    public IdpJwe setDecryptionKey(final Key decryptionKey) {
        this.decryptionKey = decryptionKey;
        return this;
    }

    public Key getDecryptionKey() {
        return decryptionKey;
    }

    @Override
    public Map<String, Object> extractBodyClaims() {
        Objects.requireNonNull(getDecryptionKey(), "Body-claim extraction requires non-null decryption key");
        try {
            return JsonUtil.parseJson(decryptJweAndReturnPayloadString(getDecryptionKey()));
        } catch (final JoseException | IdpJoseException e) {
            throw new IllegalStateException("Exception occurred during body-claim extraction", e);
        }
    }

    public static class Deserializer extends JsonDeserializer<IdpJoseObject> {

        @Override
        public IdpJoseObject deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
            return new IdpJwe(ctxt.readValue(p, String.class));
        }
    }
}
