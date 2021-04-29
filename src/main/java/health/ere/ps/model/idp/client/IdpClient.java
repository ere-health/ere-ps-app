package health.ere.ps.model.idp.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.MultipartBody;

import static org.jose4j.jws.AlgorithmIdentifiers.RSA_PSS_USING_SHA256;

@Data
@ToString
@AllArgsConstructor
@Builder(toBuilder = true)
public class IdpClient implements de.gematik.idp.client.IIdpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdpClient.class);
    private static final Consumer NOOP_CONSUMER = o -> {
    };

    private final String clientId;
    private final String redirectUrl;
    private final String discoveryDocumentUrl;
    private final boolean shouldVerifyState;
    @Builder.Default
    private Set<IdpScope> scopes = Set.of(IdpScope.OPENID, IdpScope.EREZEPT);
    @Builder.Default
    private Function<GetRequest, GetRequest> beforeAuthorizationMapper = Function.identity();
    @Builder.Default
    private Consumer<HttpResponse<AuthenticationChallenge>> afterAuthorizationCallback = NOOP_CONSUMER;
    @Builder.Default
    private Function<MultipartBody, MultipartBody> beforeAuthenticationMapper = Function.identity();
    @Builder.Default
    private Consumer<HttpResponse<String>> afterAuthenticationCallback = NOOP_CONSUMER;
    @Builder.Default
    private Function<MultipartBody, MultipartBody> beforeTokenMapper = Function.identity();
    @Builder.Default
    private Consumer<HttpResponse<JsonNode>> afterTokenCallback = NOOP_CONSUMER;
    @Builder.Default
    private AuthenticatorClient authenticatorClient = new AuthenticatorClient();
    @Builder.Default
    private CodeChallengeMethod codeChallengeMethod = CodeChallengeMethod.S256;
    @Builder.Default
    private Function<AuthorizationResponse, AuthorizationResponse> authorizationResponseMapper = Function.identity();
    @Builder.Default
    private Function<AuthenticationResponse, AuthenticationResponse> authenticationResponseMapper = Function.identity();
    private DiscoveryDocumentResponse discoveryDocumentResponse;

    private String signServerChallenge(final String challengeToSign, final X509Certificate certificate,
        final Function<Pair<String, String>, String> contentSigner) {
        final JwtClaims claims = new JwtClaims();
        claims.setClaim(ClaimName.NESTED_JWT.getJoseName(), challengeToSign);
        final JsonWebSignature jsonWebSignature = new JsonWebSignature();
        jsonWebSignature.setPayload(claims.toJson());
        jsonWebSignature.setHeader("typ", "JWT");
        jsonWebSignature.setHeader("cty", "NJWT");
        if (isEcKey(certificate.getPublicKey())) {
            jsonWebSignature.setAlgorithmHeaderValue(BRAINPOOL256_USING_SHA256);
        } else {
            jsonWebSignature.setAlgorithmHeaderValue(RSA_PSS_USING_SHA256);
        }
        return new JsonWebToken(
            contentSigner.apply(Pair.of(
                jsonWebSignature.getHeaders().getEncodedHeader(),
                jsonWebSignature.getEncodedPayload())))
            .encrypt(discoveryDocumentResponse.getIdpEnc())
            .getRawString();
    }

    @Override
    public IdpTokenResult login(final PkiIdentity idpIdentity) {
        assertThatIdpIdentityIsValid(idpIdentity);
        return login(idpIdentity.getCertificate(),
            jwtPair -> {
                final JsonWebSignature jws = new JsonWebSignature();
                jws.setPayload(new String(Base64.getUrlDecoder().decode(jwtPair.getRight())));
                Optional.ofNullable(jwtPair.getLeft())
                    .map(b64Header -> new String(Base64.getUrlDecoder().decode(b64Header)))
                    .map(JsonParser::parseString)
                    .map(JsonElement::getAsJsonObject)
                    .map(JsonObject::entrySet)
                    .stream()
                    .flatMap(Set::stream)
                    .forEach(entry -> jws.setHeader(entry.getKey(),
                        entry.getValue().getAsString()));

                jws.setCertificateChainHeaderValue(idpIdentity.getCertificate());
                jws.setKey(idpIdentity.getPrivateKey());
                try {
                    return jws.getCompactSerialization();
                } catch (final JoseException e) {
                    throw new IdpClientRuntimeException("Error during encryption", e);
                }
            });
    }

    public IdpTokenResult login(final X509Certificate certificate,
        final Function<Pair<String, String>, String> contentSigner) {
        assertThatClientIsInitialized();

        final String codeVerifier = ClientUtilities.generateCodeVerifier();
        final String nonce = RandomStringUtils.randomAlphanumeric(20);

        // Authorization
        final String state = RandomStringUtils.randomAlphanumeric(20);
        LOGGER.debug("Performing Authorization with remote-URL '{}'",
            discoveryDocumentResponse.getAuthorizationEndpoint());
        final AuthorizationResponse authorizationResponse = authorizationResponseMapper.apply(
            authenticatorClient
                .doAuthorizationRequest(AuthorizationRequest.builder()
                        .clientId(clientId)
                        .link(discoveryDocumentResponse.getAuthorizationEndpoint())
                        .codeChallenge(ClientUtilities.generateCodeChallenge(codeVerifier))
                        .codeChallengeMethod(codeChallengeMethod)
                        .redirectUri(redirectUrl)
                        .state(state)
                        .scopes(scopes)
                        .nonce(nonce)
                        .build(),
                    beforeAuthorizationMapper,
                    afterAuthorizationCallback));

        // Authentication
        LOGGER.debug("Performing Authentication with remote-URL '{}'",
            discoveryDocumentResponse.getAuthorizationEndpoint());
        final AuthenticationResponse authenticationResponse = authenticationResponseMapper.apply(
            authenticatorClient
                .performAuthentication(AuthenticationRequest.builder()
                        .authenticationEndpointUrl(
                            discoveryDocumentResponse.getAuthorizationEndpoint())
                        .signedChallenge(new IdpJwe(
                            signServerChallenge(
                                authorizationResponse.getAuthenticationChallenge().getChallenge().getRawString(),
                                certificate, contentSigner)))
                        .build(),
                    beforeAuthenticationMapper,
                    afterAuthenticationCallback));
        if (shouldVerifyState) {
            final String stringInTokenUrl = UriUtils
                .extractParameterValue(authenticationResponse.getLocation(), "state");
            if (!state.equals(stringInTokenUrl)) {
                throw new IdpClientRuntimeException("state-parameter unexpected changed");
            }
        }

        // get Token
        LOGGER.debug("Performing getToken with remote-URL '{}'", discoveryDocumentResponse.getTokenEndpoint());
        return authenticatorClient.retrieveAccessToken(TokenRequest.builder()
                .tokenUrl(discoveryDocumentResponse.getTokenEndpoint())
                .clientId(clientId)
                .code(authenticationResponse.getCode())
                .ssoToken(authenticationResponse.getSsoToken())
                .redirectUrl(redirectUrl)
                .codeVerifier(codeVerifier)
                .idpEnc(discoveryDocumentResponse.getIdpEnc())
                .build(),
            beforeTokenMapper,
            afterTokenCallback);
    }

    public IdpTokenResult loginWithSsoToken(final IdpJwe ssoToken) {
        assertThatClientIsInitialized();

        final String codeVerifier = ClientUtilities.generateCodeVerifier();
        final String nonce = RandomStringUtils.randomAlphanumeric(20);

        // Authorization
        final String state = RandomStringUtils.randomAlphanumeric(20);
        LOGGER.debug("Performing Authorization with remote-URL '{}'",
            discoveryDocumentResponse.getAuthorizationEndpoint());
        final AuthorizationResponse authorizationResponse = authorizationResponseMapper.apply(
            authenticatorClient
                .doAuthorizationRequest(AuthorizationRequest.builder()
                        .clientId(clientId)
                        .link(discoveryDocumentResponse.getAuthorizationEndpoint())
                        .codeChallenge(ClientUtilities.generateCodeChallenge(codeVerifier))
                        .codeChallengeMethod(codeChallengeMethod)
                        .redirectUri(redirectUrl)
                        .state(state)
                        .scopes(scopes)
                        .nonce(nonce)
                        .build(),
                    beforeAuthorizationMapper,
                    afterAuthorizationCallback));

        // Authentication
        final String ssoChallengeEndpoint = discoveryDocumentResponse.getAuthorizationEndpoint().replace(
            IdpConstants.BASIC_AUTHORIZATION_ENDPOINT, IdpConstants.SSO_ENDPOINT);
        LOGGER.debug("Performing Sso-Authentication with remote-URL '{}'", ssoChallengeEndpoint);
        final AuthenticationResponse authenticationResponse = authenticationResponseMapper.apply(
            authenticatorClient
                .performAuthenticationWithSsoToken(AuthenticationRequest.builder()
                        .authenticationEndpointUrl(ssoChallengeEndpoint)
                        .ssoToken(ssoToken.getRawString())
                        .challengeToken(authorizationResponse.getAuthenticationChallenge().getChallenge())
                        .build(),
                    beforeAuthenticationMapper,
                    afterAuthenticationCallback));
        if (shouldVerifyState) {
            final String stringInTokenUrl = UriUtils
                .extractParameterValue(authenticationResponse.getLocation(), "state");
            if (!state.equals(stringInTokenUrl)) {
                throw new IdpClientRuntimeException("state-parameter unexpected changed");
            }
        }

        // get Token
        LOGGER.debug("Performing getToken with remote-URL '{}'", discoveryDocumentResponse.getTokenEndpoint());
        return authenticatorClient.retrieveAccessToken(TokenRequest.builder()
                .tokenUrl(discoveryDocumentResponse.getTokenEndpoint())
                .clientId(clientId)
                .code(authenticationResponse.getCode())
                .ssoToken(ssoToken.getRawString())
                .redirectUrl(redirectUrl)
                .codeVerifier(codeVerifier)
                .idpEnc(discoveryDocumentResponse.getIdpEnc())
                .build(),
            beforeTokenMapper,
            afterTokenCallback);
    }

    private void assertThatIdpIdentityIsValid(final PkiIdentity idpIdentity) {
        Objects.requireNonNull(idpIdentity);
        Objects.requireNonNull(idpIdentity.getCertificate());
        Objects.requireNonNull(idpIdentity.getPrivateKey());
    }

    private IdpJwe signChallenge(
        final AuthenticationChallenge authenticationChallenge,
        final PkiIdentity idpIdentity) {
        return AuthenticationResponseBuilder.builder().build()
            .buildResponseForChallenge(authenticationChallenge, idpIdentity)
            .getSignedChallenge()
            .encrypt(discoveryDocumentResponse.getIdpEnc());
    }

    private void assertThatClientIsInitialized() {
        LOGGER.debug("Verifying IDP-Client initialization...");
        if (discoveryDocumentResponse == null ||
            StringUtils.isEmpty(discoveryDocumentResponse.getAuthorizationEndpoint()) ||
            StringUtils.isEmpty(discoveryDocumentResponse.getTokenEndpoint())) {
            throw new IdpClientRuntimeException(
                "IDP-Client not initialized correctly! Call .initialize() before performing an actual operation.");
        }
    }

    @Override
    public IdpClient initialize() {
        LOGGER.info("Initializing using url '{}'", discoveryDocumentUrl);
        discoveryDocumentResponse = authenticatorClient
            .retrieveDiscoveryDocument(discoveryDocumentUrl);
        return this;
    }

    public void verifyAuthTokenToken(final IdpTokenResult authToken) {
        authToken.getAccessToken()
            .verify(discoveryDocumentResponse.getIdpSig().getPublicKey());
    }

    public void setBeforeAuthorizationCallback(final Consumer<GetRequest> callback) {
        beforeAuthorizationMapper = toNoopIdentity(callback);
    }

    public void setBeforeAuthenticationCallback(final Consumer<MultipartBody> callback) {
        beforeAuthenticationMapper = toNoopIdentity(callback);
    }

    public void setBeforeTokenCallback(final Consumer<MultipartBody> callback) {
        beforeTokenMapper = toNoopIdentity(callback);
    }

    public <T> Function<T, T> toNoopIdentity(final Consumer<T> callback) {
        return t -> {
            callback.accept(t);
            return t;
        };
    }
}
