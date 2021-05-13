package health.ere.ps.model.idp.client.token;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jose4j.jwt.NumericDate;

import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.IdpConstants;
import health.ere.ps.model.idp.client.authentication.IdpJwtProcessor;
import health.ere.ps.model.idp.client.authentication.JwtBuilder;
import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.service.idp.crypto.Nonce;

public class IdTokenBuilder {

    private static final Set<String> requiredClaims = Stream.of(ClaimName.PROFESSION_OID,
            ClaimName.GIVEN_NAME, ClaimName.FAMILY_NAME,
            ClaimName.ORGANIZATION_NAME, ClaimName.ID_NUMBER, ClaimName.AUTHENTICATION_CLASS_REFERENCE,
            ClaimName.CLIENT_ID, ClaimName.SCOPE, ClaimName.AUTH_TIME)
        .map(ClaimName::getJoseName)
        .collect(Collectors.toSet());
    private static final List<ClaimName> CLAIMS_TO_TAKE_FROM_AUTHENTICATION_TOKEN = List
        .of(ClaimName.GIVEN_NAME, ClaimName.FAMILY_NAME, ClaimName.ORGANIZATION_NAME,
                ClaimName.PROFESSION_OID, ClaimName.ID_NUMBER, ClaimName.AUTH_TIME, ClaimName.NONCE);

    private IdpJwtProcessor jwtProcessor;
    private String issuerUrl;
    private String serverSubjectSalt;

    public JsonWebToken buildIdToken(final String clientId, final JsonWebToken authenticationToken,
        final byte[] accesTokenHash) {
        final Map<String, Object> claimsMap = new HashMap<>();
        final ZonedDateTime now = ZonedDateTime.now();
        final String atHashValue = Base64.getUrlEncoder().withoutPadding().encodeToString(
            ArrayUtils.subarray(accesTokenHash, 0, 16));

        claimsMap.put(ClaimName.ISSUER.getJoseName(), getIssuerUrl());
        claimsMap.put(ClaimName.SUBJECT.getJoseName(), clientId);
        claimsMap.put(ClaimName.AUDIENCE.getJoseName(), clientId);
        claimsMap.put(ClaimName.ISSUED_AT.getJoseName(), now.toEpochSecond());

        CLAIMS_TO_TAKE_FROM_AUTHENTICATION_TOKEN.stream()
            .map(claimName -> Pair.of(claimName, authenticationToken.getBodyClaim(claimName)))
            .filter(pair -> pair.getValue().isPresent())
            .forEach(pair -> claimsMap.put(pair.getKey().getJoseName(), pair.getValue().get()));

        claimsMap.put(ClaimName.AUTHORIZED_PARTY.getJoseName(),
            authenticationToken.getBodyClaim(ClaimName.CLIENT_ID)
                .orElseThrow(() -> new IdpJoseException("Missing '" + ClaimName.AUTHORIZED_PARTY.getJoseName() + "' claim!")));
        claimsMap.put(ClaimName.AUTHENTICATION_METHODS_REFERENCE.getJoseName(), getAmrString());
        claimsMap.put(ClaimName.AUTHENTICATION_CLASS_REFERENCE.getJoseName(), IdpConstants.EIDAS_LOA_HIGH);
        claimsMap.put(ClaimName.ACCESS_TOKEN_HASH.getJoseName(), atHashValue);
        claimsMap.put(ClaimName.SUBJECT.getJoseName(),
            TokenBuilderUtil.buildSubjectClaim(
                IdpConstants.AUDIENCE,
                authenticationToken.getStringBodyClaim(ClaimName.ID_NUMBER)
                    .orElseThrow(() -> new IdpJoseException("Missing '" + ClaimName.ID_NUMBER.getJoseName() + "' claim!")),
                    getServerSubjectSalt()));
        claimsMap.put(ClaimName.JWT_ID.getJoseName(), new Nonce().getNonceAsHex(IdpConstants.JTI_LENGTH));
        claimsMap.put(ClaimName.EXPIRES_AT.getJoseName(), NumericDate.fromSeconds(now.plusMinutes(5).toEpochSecond()).getValue());

        final Map<String, Object> headerClaims = new HashMap<>();
        headerClaims.put(ClaimName.TYPE.getJoseName(), "JWT");

        return getJwtProcessor().buildJwt(new JwtBuilder()
            .addAllBodyClaims(claimsMap)
            .addAllHeaderClaims(headerClaims));
    }

    private String[] getAmrString() {
        return new String[]{"mfa", "sc", "pin"};
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

    public String getServerSubjectSalt() {
        return serverSubjectSalt;
    }

    public void setServerSubjectSalt(String serverSubjectSalt) {
        this.serverSubjectSalt = serverSubjectSalt;
    }
}
