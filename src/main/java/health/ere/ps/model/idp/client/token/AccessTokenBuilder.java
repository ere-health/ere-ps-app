
package health.ere.ps.model.idp.client.token;

import org.apache.commons.lang3.tuple.Pair;
import org.jose4j.jwt.NumericDate;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.exception.idp.RequiredClaimException;
import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.model.idp.client.IdpConstants;
import health.ere.ps.model.idp.client.authentication.IdpJwtProcessor;
import health.ere.ps.model.idp.client.authentication.JwtBuilder;
import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.service.idp.crypto.Nonce;

public class AccessTokenBuilder {

    private static final List<ClaimName> CLAIMS_TO_TAKE_FROM_AUTHENTICATION_TOKEN = List
        .of(ClaimName.PROFESSION_OID, ClaimName.GIVEN_NAME, ClaimName.FAMILY_NAME,
            ClaimName.ORGANIZATION_NAME, ClaimName.ID_NUMBER, ClaimName.CLIENT_ID,
                ClaimName.SCOPE, ClaimName.AUTH_TIME);
    private IdpJwtProcessor jwtProcessor;
    private String issuerUrl;
    private String serverSubjectSalt;

    public AccessTokenBuilder(IdpJwtProcessor jwtProcessor, String issuerUrl, String serverSubjectSalt) {
        this.jwtProcessor = jwtProcessor;
        this.issuerUrl = issuerUrl;
        this.serverSubjectSalt = serverSubjectSalt;
    }

    public JsonWebToken buildAccessToken(final JsonWebToken authenticationToken) {
        final ZonedDateTime now = ZonedDateTime.now();
        final Map<String, Object> claimsMap = new HashMap<>();

        CLAIMS_TO_TAKE_FROM_AUTHENTICATION_TOKEN.stream()
            .map(claimName -> Pair.of(claimName, authenticationToken.getBodyClaim(claimName)))
            .filter(pair -> pair.getValue().isPresent())
            .forEach(pair -> claimsMap.put(pair.getKey().getJoseName(), pair.getValue().get()));
        claimsMap.put(ClaimName.ISSUED_AT.getJoseName(), now.toEpochSecond());
        claimsMap.put(ClaimName.ISSUER.getJoseName(), issuerUrl);
        claimsMap.put(ClaimName.AUDIENCE.getJoseName(), IdpConstants.AUDIENCE);
        claimsMap.put(ClaimName.AUTHENTICATION_CLASS_REFERENCE.getJoseName(),
                IdpConstants.EIDAS_LOA_HIGH);
        try {
            claimsMap.put(ClaimName.SUBJECT.getJoseName(),
                TokenBuilderUtil.buildSubjectClaim(
                    IdpConstants.AUDIENCE,
                    authenticationToken.getStringBodyClaim(ClaimName.ID_NUMBER)
                        .orElseThrow(() -> new RequiredClaimException("Missing '" + ClaimName.ID_NUMBER.getJoseName() + "' claim!")),
                    serverSubjectSalt));
        } catch (RequiredClaimException e) {
            throw new IllegalStateException(e);
        }
        try {
            claimsMap.put(ClaimName.AUTHORIZED_PARTY.getJoseName(), authenticationToken.getBodyClaim(ClaimName.CLIENT_ID)
                .orElseThrow(() -> new RequiredClaimException("Unable to obtain " + ClaimName.CLIENT_ID.getJoseName() + "!")));
        } catch (RequiredClaimException e) {
            throw new IllegalStateException(e);
        }
        claimsMap.put(ClaimName.JWT_ID.getJoseName(), new Nonce().getNonceAsHex(IdpConstants.JTI_LENGTH));
        claimsMap.put(ClaimName.AUTHENTICATION_METHODS_REFERENCE.getJoseName(),
            authenticationToken.getBodyClaim(ClaimName.AUTHENTICATION_METHODS_REFERENCE)
                .orElse(getAmrString()));
        claimsMap.put(ClaimName.EXPIRES_AT.getJoseName(), NumericDate.fromSeconds(now.plusMinutes(5).toEpochSecond()).getValue());

        final Map<String, Object> headerClaimsMap = new HashMap<>();
        headerClaimsMap.put(ClaimName.TYPE.getJoseName(), "at+JWT");

        try {
            return jwtProcessor.buildJwt(new JwtBuilder()
                .replaceAllBodyClaims(claimsMap)
                .replaceAllHeaderClaims(headerClaimsMap));
        } catch (IdpJoseException | IdpCryptoException e) {
            throw new IllegalStateException(e);
        }
    }

    private String[] getAmrString() {
        return new String[]{"mfa", "sc", "pin"};
    }
}
