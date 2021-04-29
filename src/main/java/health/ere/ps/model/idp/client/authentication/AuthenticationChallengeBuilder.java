package health.ere.ps.model.idp.client.authentication;

import health.ere.ps.model.idp.client.IdpConstants;
import health.ere.ps.service.idp.crypto.Nonce;
import health.ere.ps.model.idp.client.data.UserConsent;
import health.ere.ps.model.idp.client.data.UserConsentConfiguration;
import health.ere.ps.model.idp.client.field.IdpScope;
import health.ere.ps.model.idp.client.token.JsonWebToken;

import org.apache.commons.lang3.tuple.Pair;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static health.ere.ps.model.idp.client.field.ClaimName.CLIENT_ID;
import static health.ere.ps.model.idp.client.field.ClaimName.CODE_CHALLENGE;
import static health.ere.ps.model.idp.client.field.ClaimName.CODE_CHALLENGE_METHOD;
import static health.ere.ps.model.idp.client.field.ClaimName.EXPIRES_AT;
import static health.ere.ps.model.idp.client.field.ClaimName.ISSUED_AT;
import static health.ere.ps.model.idp.client.field.ClaimName.ISSUER;
import static health.ere.ps.model.idp.client.field.ClaimName.JWT_ID;
import static health.ere.ps.model.idp.client.field.ClaimName.NONCE;
import static health.ere.ps.model.idp.client.field.ClaimName.REDIRECT_URI;
import static health.ere.ps.model.idp.client.field.ClaimName.RESPONSE_TYPE;
import static health.ere.ps.model.idp.client.field.ClaimName.SCOPE;
import static health.ere.ps.model.idp.client.field.ClaimName.SERVER_NONCE;
import static health.ere.ps.model.idp.client.field.ClaimName.STATE;
import static health.ere.ps.model.idp.client.field.ClaimName.TOKEN_TYPE;
import static health.ere.ps.model.idp.client.field.ClaimName.TYPE;

public class AuthenticationChallengeBuilder {
    private static final long CHALLENGE_TOKEN_VALIDITY_IN_MINUTES = 3;
    private static final int NONCE_BYTE_AMOUNT = 32;
    private IdpJwtProcessor serverSigner;
    private String uriIdpServer;
    private UserConsentConfiguration userConsentConfiguration;

    public AuthenticationChallengeBuilder(IdpJwtProcessor serverSigner, String uriIdpServer, UserConsentConfiguration userConsentConfiguration) {
        this.serverSigner = serverSigner;
        this.uriIdpServer = uriIdpServer;
        this.userConsentConfiguration = userConsentConfiguration;
    }

    public AuthenticationChallengeBuilder() {
    }

    public AuthenticationChallenge buildAuthenticationChallenge(final String clientId, final String state,
                                                                final String redirect, final String code, final String scope, final String nonce) {
        final Map<String, Object> claims = new HashMap<>();
        claims.put(ISSUER.getJoseName(), getUriIdpServer());

        final ZonedDateTime now = ZonedDateTime.now();
        claims.put(EXPIRES_AT.getJoseName(), now.plusMinutes(CHALLENGE_TOKEN_VALIDITY_IN_MINUTES).toEpochSecond());
        claims.put(ISSUED_AT.getJoseName(), now.toEpochSecond());
        claims.put(RESPONSE_TYPE.getJoseName(), "code");
        claims.put(SCOPE.getJoseName(), scope);
        claims.put(CLIENT_ID.getJoseName(), clientId);
        claims.put(STATE.getJoseName(), state);
        claims.put(REDIRECT_URI.getJoseName(), redirect);
        claims.put(CODE_CHALLENGE_METHOD.getJoseName(), "S256");
        claims.put(CODE_CHALLENGE.getJoseName(), code);
        claims.put(TOKEN_TYPE.getJoseName(), "challenge");
        if (nonce != null) {
            claims.put(NONCE.getJoseName(), nonce);
        }
        claims.put(SERVER_NONCE.getJoseName(), new Nonce().getNonceAsBase64UrlEncodedString(NONCE_BYTE_AMOUNT));
        claims.put(JWT_ID.getJoseName(), new Nonce().getNonceAsHex(IdpConstants.JTI_LENGTH));

        final Map<String, Object> headerClaims = new HashMap<>();
        headerClaims.put(TYPE.getJoseName(), "JWT");

        final UserConsent userConsent = getUserConsent(scope);
        return AuthenticationChallenge.builder()
            .challenge(buildJwt(claims, headerClaims))
            .userConsent(userConsent)
            .build();
    }

    private UserConsent getUserConsent(final String scopes) {
        final List<IdpScope> requestedScopes = Stream.of(scopes.split(" "))
            .map(IdpScope::fromJwtValue)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
        final Map<String, String> scopeMap = requestedScopes.stream()
            .map(s -> Pair
                .of(s.getJwtValue(), getUserConsentConfiguration().getDescriptionTexts().getScopes().get(s)))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        final Map<String, String> clientMap = requestedScopes.stream()
            .filter(id -> getUserConsentConfiguration().getClaimsToBeIncluded().containsKey(id))
            .map(id -> getUserConsentConfiguration().getClaimsToBeIncluded().get(id))
            .flatMap(List::stream)
            .distinct()
            .map(s -> Pair
                .of(s.getJoseName(), getUserConsentConfiguration().getDescriptionTexts().getClaims().get(s)))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        return UserConsent.builder()
            .requestedScopes(scopeMap)
            .requestedClaims(clientMap)
            .build();
    }

    private JsonWebToken buildJwt(final Map<String, Object> bodyClaims, final Map<String, Object> headerClaims) {
        return getServerSigner().buildJwt(new JwtBuilder()
            .addAllBodyClaims(bodyClaims)
            .addAllHeaderClaims(headerClaims));
    }

    public IdpJwtProcessor getServerSigner() {
        return serverSigner;
    }

    public void setServerSigner(IdpJwtProcessor serverSigner) {
        this.serverSigner = serverSigner;
    }

    public String getUriIdpServer() {
        return uriIdpServer;
    }

    public void setUriIdpServer(String uriIdpServer) {
        this.uriIdpServer = uriIdpServer;
    }

    public UserConsentConfiguration getUserConsentConfiguration() {
        return userConsentConfiguration;
    }

    public void setUserConsentConfiguration(UserConsentConfiguration userConsentConfiguration) {
        this.userConsentConfiguration = userConsentConfiguration;
    }

    public static AuthenticationChallengeBuilderBuilder builder() {
        return new AuthenticationChallengeBuilderBuilder();
    }

    public static class AuthenticationChallengeBuilderBuilder {
        private AuthenticationChallengeBuilder authenticationChallengeBuilder;

        public AuthenticationChallengeBuilderBuilder() {
            authenticationChallengeBuilder = new AuthenticationChallengeBuilder();
        }

        public AuthenticationChallengeBuilderBuilder serverSigner(IdpJwtProcessor serverSigner) {
            authenticationChallengeBuilder.setServerSigner(serverSigner);

            return this;
        }

        public AuthenticationChallengeBuilderBuilder uriIdpServer(String uriIdpServer) {
            authenticationChallengeBuilder.setUriIdpServer(uriIdpServer);

            return this;
        }

        public AuthenticationChallengeBuilderBuilder userConsentConfiguration(
                UserConsentConfiguration userConsentConfiguration) {
            authenticationChallengeBuilder.setUserConsentConfiguration(userConsentConfiguration);

            return this;
        }

        public AuthenticationChallengeBuilder build() {
            return authenticationChallengeBuilder;
        }
    }
}
