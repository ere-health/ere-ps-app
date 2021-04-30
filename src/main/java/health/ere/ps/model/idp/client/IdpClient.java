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

import health.ere.ps.model.idp.client.authentication.AuthenticationChallenge;
import health.ere.ps.model.idp.client.authentication.AuthenticationResponseBuilder;
import health.ere.ps.model.idp.client.authentication.UriUtils;
import health.ere.ps.model.idp.client.brainPoolExtension.BrainpoolAlgorithmSuiteIdentifiers;
import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.model.idp.client.field.CodeChallengeMethod;
import health.ere.ps.model.idp.client.field.IdpScope;
import health.ere.ps.model.idp.client.token.IdpJwe;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.idp.client.AuthenticatorClient;
import health.ere.ps.service.idp.crypto.KeyAnalysis;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.MultipartBody;

import static org.jose4j.jws.AlgorithmIdentifiers.RSA_PSS_USING_SHA256;

public class IdpClient implements IIdpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdpClient.class);
    private static final Consumer NOOP_CONSUMER = o -> {
    };

    private String clientId;
    private String redirectUrl;
    private String discoveryDocumentUrl;
    private boolean shouldVerifyState;
    private Set<IdpScope> scopes = Set.of(IdpScope.OPENID, IdpScope.EREZEPT);
    private Function<GetRequest, GetRequest> beforeAuthorizationMapper = Function.identity();
    private Consumer<HttpResponse<AuthenticationChallenge>> afterAuthorizationCallback = NOOP_CONSUMER;
    private Function<MultipartBody, MultipartBody> beforeAuthenticationMapper = Function.identity();
    private Consumer<HttpResponse<String>> afterAuthenticationCallback = NOOP_CONSUMER;
    private Function<MultipartBody, MultipartBody> beforeTokenMapper = Function.identity();
    private Consumer<HttpResponse<JsonNode>> afterTokenCallback = NOOP_CONSUMER;
    private AuthenticatorClient authenticatorClient = new AuthenticatorClient();
    private CodeChallengeMethod codeChallengeMethod = CodeChallengeMethod.S256;
    private Function<AuthorizationResponse, AuthorizationResponse> authorizationResponseMapper = Function.identity();
    private Function<AuthenticationResponse, AuthenticationResponse> authenticationResponseMapper = Function.identity();
    private DiscoveryDocumentResponse discoveryDocumentResponse;

    public IdpClient(String clientId, String redirectUrl, String discoveryDocumentUrl,
                     boolean shouldVerifyState, Set<IdpScope> scopes,
                     Function<GetRequest, GetRequest> beforeAuthorizationMapper,
                     Consumer<HttpResponse<AuthenticationChallenge>> afterAuthorizationCallback,
                     Function<MultipartBody, MultipartBody> beforeAuthenticationMapper,
                     Consumer<HttpResponse<String>> afterAuthenticationCallback,
                     Function<MultipartBody, MultipartBody> beforeTokenMapper,
                     Consumer<HttpResponse<JsonNode>> afterTokenCallback,
                     AuthenticatorClient authenticatorClient,
                     CodeChallengeMethod codeChallengeMethod,
                     Function<AuthorizationResponse,
                     AuthorizationResponse> authorizationResponseMapper,
                     Function<AuthenticationResponse,
                     AuthenticationResponse> authenticationResponseMapper,
                     DiscoveryDocumentResponse discoveryDocumentResponse) {
        this.clientId = clientId;
        this.redirectUrl = redirectUrl;
        this.discoveryDocumentUrl = discoveryDocumentUrl;
        this.shouldVerifyState = shouldVerifyState;
        this.scopes = scopes;
        this.beforeAuthorizationMapper = beforeAuthorizationMapper;
        this.afterAuthorizationCallback = afterAuthorizationCallback;
        this.beforeAuthenticationMapper = beforeAuthenticationMapper;
        this.afterAuthenticationCallback = afterAuthenticationCallback;
        this.beforeTokenMapper = beforeTokenMapper;
        this.afterTokenCallback = afterTokenCallback;
        this.authenticatorClient = authenticatorClient;
        this.codeChallengeMethod = codeChallengeMethod;
        this.authorizationResponseMapper = authorizationResponseMapper;
        this.authenticationResponseMapper = authenticationResponseMapper;
        this.discoveryDocumentResponse = discoveryDocumentResponse;
    }

    public IdpClient() {
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("IdpClient{");
        sb.append("clientId='").append(clientId).append('\'');
        sb.append(", redirectUrl='").append(redirectUrl).append('\'');
        sb.append(", discoveryDocumentUrl='").append(discoveryDocumentUrl).append('\'');
        sb.append(", shouldVerifyState=").append(shouldVerifyState);
        sb.append(", scopes=").append(scopes);
        sb.append(", beforeAuthorizationMapper=").append(beforeAuthorizationMapper);
        sb.append(", afterAuthorizationCallback=").append(afterAuthorizationCallback);
        sb.append(", beforeAuthenticationMapper=").append(beforeAuthenticationMapper);
        sb.append(", afterAuthenticationCallback=").append(afterAuthenticationCallback);
        sb.append(", beforeTokenMapper=").append(beforeTokenMapper);
        sb.append(", afterTokenCallback=").append(afterTokenCallback);
        sb.append(", authenticatorClient=").append(authenticatorClient);
        sb.append(", codeChallengeMethod=").append(codeChallengeMethod);
        sb.append(", authorizationResponseMapper=").append(authorizationResponseMapper);
        sb.append(", authenticationResponseMapper=").append(authenticationResponseMapper);
        sb.append(", discoveryDocumentResponse=").append(discoveryDocumentResponse);
        sb.append('}');
        return sb.toString();
    }

    private String signServerChallenge(final String challengeToSign, final X509Certificate certificate,
                                       final Function<Pair<String, String>, String> contentSigner) {
        final JwtClaims claims = new JwtClaims();
        claims.setClaim(ClaimName.NESTED_JWT.getJoseName(), challengeToSign);
        final JsonWebSignature jsonWebSignature = new JsonWebSignature();
        jsonWebSignature.setPayload(claims.toJson());
        jsonWebSignature.setHeader("typ", "JWT");
        jsonWebSignature.setHeader("cty", "NJWT");
        if (KeyAnalysis.isEcKey(certificate.getPublicKey())) {
            jsonWebSignature.setAlgorithmHeaderValue(
                    BrainpoolAlgorithmSuiteIdentifiers.BRAINPOOL256_USING_SHA256);
        } else {
            jsonWebSignature.setAlgorithmHeaderValue(RSA_PSS_USING_SHA256);
        }
        return new JsonWebToken(
            contentSigner.apply(Pair.of(
                jsonWebSignature.getHeaders().getEncodedHeader(),
                jsonWebSignature.getEncodedPayload())))
            .encrypt(getDiscoveryDocumentResponse().getIdpEnc())
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
            getDiscoveryDocumentResponse().getAuthorizationEndpoint());
        final AuthorizationResponse authorizationResponse = getAuthorizationResponseMapper().apply(
            getAuthenticatorClient()
                .doAuthorizationRequest(AuthorizationRequest.builder()
                        .clientId(getClientId())
                        .link(getDiscoveryDocumentResponse().getAuthorizationEndpoint())
                        .codeChallenge(ClientUtilities.generateCodeChallenge(codeVerifier))
                        .codeChallengeMethod(getCodeChallengeMethod())
                        .redirectUri(getRedirectUrl())
                        .state(state)
                        .scopes(getScopes())
                        .nonce(nonce)
                        .build(),
                        getBeforeAuthorizationMapper(),
                        getAfterAuthorizationCallback()));

        // Authentication
        LOGGER.debug("Performing Authentication with remote-URL '{}'",
            getDiscoveryDocumentResponse().getAuthorizationEndpoint());
        final AuthenticationResponse authenticationResponse = getAuthenticationResponseMapper().apply(
            getAuthenticatorClient()
                .performAuthentication(AuthenticationRequest.builder()
                        .authenticationEndpointUrl(
                            getDiscoveryDocumentResponse().getAuthorizationEndpoint())
                        .signedChallenge(new IdpJwe(
                            signServerChallenge(
                                authorizationResponse.getAuthenticationChallenge().getChallenge().getRawString(),
                                certificate, contentSigner)))
                        .build(),
                        getBeforeAuthenticationMapper(),
                        getAfterAuthenticationCallback()));
        if (isShouldVerifyState()) {
            final String stringInTokenUrl = UriUtils
                .extractParameterValue(authenticationResponse.getLocation(), "state");
            if (!state.equals(stringInTokenUrl)) {
                throw new IdpClientRuntimeException("state-parameter unexpected changed");
            }
        }

        // get Token
        LOGGER.debug("Performing getToken with remote-URL '{}'",
                getDiscoveryDocumentResponse().getTokenEndpoint());
        return getAuthenticatorClient().retrieveAccessToken(TokenRequest.builder()
                .tokenUrl(getDiscoveryDocumentResponse().getTokenEndpoint())
                .clientId(getClientId())
                .code(authenticationResponse.getCode())
                .ssoToken(authenticationResponse.getSsoToken())
                .redirectUrl(getRedirectUrl())
                .codeVerifier(codeVerifier)
                .idpEnc(getDiscoveryDocumentResponse().getIdpEnc())
                .build(),
                getBeforeTokenMapper(),
                getAfterTokenCallback());
    }

    public IdpTokenResult loginWithSsoToken(final IdpJwe ssoToken) {
        assertThatClientIsInitialized();

        final String codeVerifier = ClientUtilities.generateCodeVerifier();
        final String nonce = RandomStringUtils.randomAlphanumeric(20);

        // Authorization
        final String state = RandomStringUtils.randomAlphanumeric(20);
        LOGGER.debug("Performing Authorization with remote-URL '{}'",
            getDiscoveryDocumentResponse().getAuthorizationEndpoint());
        final AuthorizationResponse authorizationResponse = getAuthorizationResponseMapper().apply(
            getAuthenticatorClient()
                .doAuthorizationRequest(AuthorizationRequest.builder()
                        .clientId(getClientId())
                        .link(getDiscoveryDocumentResponse().getAuthorizationEndpoint())
                        .codeChallenge(ClientUtilities.generateCodeChallenge(codeVerifier))
                        .codeChallengeMethod(getCodeChallengeMethod())
                        .redirectUri(getRedirectUrl())
                        .state(state)
                        .scopes(getScopes())
                        .nonce(nonce)
                        .build(),
                        getBeforeAuthorizationMapper(),
                        getAfterAuthorizationCallback()));

        // Authentication
        final String ssoChallengeEndpoint = getDiscoveryDocumentResponse().getAuthorizationEndpoint().replace(
            IdpConstants.BASIC_AUTHORIZATION_ENDPOINT, IdpConstants.SSO_ENDPOINT);
        LOGGER.debug("Performing Sso-Authentication with remote-URL '{}'", ssoChallengeEndpoint);
        final AuthenticationResponse authenticationResponse = getAuthenticationResponseMapper().apply(
            getAuthenticatorClient()
                .performAuthenticationWithSsoToken(AuthenticationRequest.builder()
                        .authenticationEndpointUrl(ssoChallengeEndpoint)
                        .ssoToken(ssoToken.getRawString())
                        .challengeToken(authorizationResponse.getAuthenticationChallenge().getChallenge())
                        .build(),
                        getBeforeAuthenticationMapper(),
                        getAfterAuthenticationCallback()));
        if (isShouldVerifyState()) {
            final String stringInTokenUrl = UriUtils
                .extractParameterValue(authenticationResponse.getLocation(), "state");
            if (!state.equals(stringInTokenUrl)) {
                throw new IdpClientRuntimeException("state-parameter unexpected changed");
            }
        }

        // get Token
        LOGGER.debug("Performing getToken with remote-URL '{}'",
                getDiscoveryDocumentResponse().getTokenEndpoint());
        return getAuthenticatorClient().retrieveAccessToken(TokenRequest.builder()
                .tokenUrl(getDiscoveryDocumentResponse().getTokenEndpoint())
                .clientId(getClientId())
                .code(authenticationResponse.getCode())
                .ssoToken(ssoToken.getRawString())
                .redirectUrl(getRedirectUrl())
                .codeVerifier(codeVerifier)
                .idpEnc(getDiscoveryDocumentResponse().getIdpEnc())
                .build(),
                getBeforeTokenMapper(),
                getAfterTokenCallback());
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
            .encrypt(getDiscoveryDocumentResponse().getIdpEnc());
    }

    private void assertThatClientIsInitialized() {
        LOGGER.debug("Verifying IDP-Client initialization...");
        if (getDiscoveryDocumentResponse() == null ||
            StringUtils.isEmpty(getDiscoveryDocumentResponse().getAuthorizationEndpoint()) ||
            StringUtils.isEmpty(getDiscoveryDocumentResponse().getTokenEndpoint())) {
            throw new IdpClientRuntimeException(
                "IDP-Client not initialized correctly! Call .initialize() before performing an actual operation.");
        }
    }

    @Override
    public IdpClient initialize() {
        LOGGER.info("Initializing using url '{}'", getDiscoveryDocumentUrl());
        setDiscoveryDocumentResponse(getAuthenticatorClient()
            .retrieveDiscoveryDocument(getDiscoveryDocumentUrl()));
        return this;
    }

    public void verifyAuthTokenToken(final IdpTokenResult authToken) {
        authToken.getAccessToken()
            .verify(getDiscoveryDocumentResponse().getIdpSig().getPublicKey());
    }

    public void setBeforeAuthorizationCallback(final Consumer<GetRequest> callback) {
        setBeforeAuthorizationMapper(toNoopIdentity(callback));
    }

    public void setBeforeAuthenticationCallback(final Consumer<MultipartBody> callback) {
        setBeforeAuthenticationMapper(toNoopIdentity(callback));
    }

    public void setBeforeTokenCallback(final Consumer<MultipartBody> callback) {
        setBeforeTokenMapper(toNoopIdentity(callback));
    }

    public <T> Function<T, T> toNoopIdentity(final Consumer<T> callback) {
        return t -> {
            callback.accept(t);
            return t;
        };
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getDiscoveryDocumentUrl() {
        return discoveryDocumentUrl;
    }

    public void setDiscoveryDocumentUrl(String discoveryDocumentUrl) {
        this.discoveryDocumentUrl = discoveryDocumentUrl;
    }

    public boolean isShouldVerifyState() {
        return shouldVerifyState;
    }

    public void setShouldVerifyState(boolean shouldVerifyState) {
        this.shouldVerifyState = shouldVerifyState;
    }

    public Set<IdpScope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<IdpScope> scopes) {
        this.scopes = scopes;
    }

    public Function<GetRequest, GetRequest> getBeforeAuthorizationMapper() {
        return beforeAuthorizationMapper;
    }

    public void setBeforeAuthorizationMapper(Function<GetRequest,
            GetRequest> beforeAuthorizationMapper) {
        this.beforeAuthorizationMapper = beforeAuthorizationMapper;
    }

    public Consumer<HttpResponse<AuthenticationChallenge>> getAfterAuthorizationCallback() {
        return afterAuthorizationCallback;
    }

    public void setAfterAuthorizationCallback(
            Consumer<HttpResponse<AuthenticationChallenge>> afterAuthorizationCallback) {
        this.afterAuthorizationCallback = afterAuthorizationCallback;
    }

    public Function<MultipartBody, MultipartBody> getBeforeAuthenticationMapper() {
        return beforeAuthenticationMapper;
    }

    public void setBeforeAuthenticationMapper(Function<MultipartBody,
            MultipartBody> beforeAuthenticationMapper) {
        this.beforeAuthenticationMapper = beforeAuthenticationMapper;
    }

    public Consumer<HttpResponse<String>> getAfterAuthenticationCallback() {
        return afterAuthenticationCallback;
    }

    public void setAfterAuthenticationCallback(
            Consumer<HttpResponse<String>> afterAuthenticationCallback) {
        this.afterAuthenticationCallback = afterAuthenticationCallback;
    }

    public Function<MultipartBody, MultipartBody> getBeforeTokenMapper() {
        return beforeTokenMapper;
    }

    public void setBeforeTokenMapper(Function<MultipartBody, MultipartBody> beforeTokenMapper) {
        this.beforeTokenMapper = beforeTokenMapper;
    }

    public Consumer<HttpResponse<JsonNode>> getAfterTokenCallback() {
        return afterTokenCallback;
    }

    public void setAfterTokenCallback(Consumer<HttpResponse<JsonNode>> afterTokenCallback) {
        this.afterTokenCallback = afterTokenCallback;
    }

    public AuthenticatorClient getAuthenticatorClient() {
        return authenticatorClient;
    }

    public void setAuthenticatorClient(AuthenticatorClient authenticatorClient) {
        this.authenticatorClient = authenticatorClient;
    }

    public CodeChallengeMethod getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public void setCodeChallengeMethod(CodeChallengeMethod codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
    }

    public Function<AuthorizationResponse, AuthorizationResponse> getAuthorizationResponseMapper() {
        return authorizationResponseMapper;
    }

    public void setAuthorizationResponseMapper(Function<AuthorizationResponse,
            AuthorizationResponse> authorizationResponseMapper) {
        this.authorizationResponseMapper = authorizationResponseMapper;
    }

    public Function<AuthenticationResponse, AuthenticationResponse> getAuthenticationResponseMapper() {
        return authenticationResponseMapper;
    }

    public void setAuthenticationResponseMapper(Function<AuthenticationResponse,
            AuthenticationResponse> authenticationResponseMapper) {
        this.authenticationResponseMapper = authenticationResponseMapper;
    }

    public DiscoveryDocumentResponse getDiscoveryDocumentResponse() {
        return discoveryDocumentResponse;
    }

    public void setDiscoveryDocumentResponse(DiscoveryDocumentResponse discoveryDocumentResponse) {
        this.discoveryDocumentResponse = discoveryDocumentResponse;
    }

    public static IdpClientBuilder builder() {
        return new IdpClientBuilder();
    }

    public static class IdpClientBuilder {
        private IdpClient idpClient;

        public IdpClientBuilder() {
            idpClient = new IdpClient();
        }

        public IdpClientBuilder clientId(String clientId) {
            idpClient.setClientId(clientId);

            return this;
        }

        public IdpClientBuilder redirectUrl(String redirectUrl) {
            idpClient.setRedirectUrl(redirectUrl);

            return this;
        }

        public IdpClientBuilder discoveryDocumentUrl(String discoveryDocumentUrl) {
            idpClient.setDiscoveryDocumentUrl(discoveryDocumentUrl);

            return this;
        }

        public IdpClientBuilder shouldVerifyState(boolean shouldVerifyState) {
            idpClient.setShouldVerifyState(shouldVerifyState);

            return this;
        }

        public IdpClientBuilder scopes(Set<IdpScope> scopes) {
            idpClient.setScopes(scopes);

            return this;
        }

        public IdpClientBuilder beforeAuthorizationMapper(Function<GetRequest, GetRequest> beforeAuthorizationMapper) {
            idpClient.setBeforeAuthorizationMapper(beforeAuthorizationMapper);

            return this;
        }

        public IdpClientBuilder afterAuthorizationCallback(
                Consumer<HttpResponse<AuthenticationChallenge>> afterAuthorizationCallback) {
            idpClient.setAfterAuthorizationCallback(afterAuthorizationCallback);

            return this;
        }

        public IdpClientBuilder beforeAuthenticationMapper(
                Function<MultipartBody, MultipartBody> beforeAuthenticationMapper) {
            idpClient.setBeforeAuthenticationMapper(beforeAuthenticationMapper);

            return this;
        }

        public IdpClientBuilder afterAuthenticationCallback(
                Consumer<HttpResponse<String>> afterAuthenticationCallback) {
            idpClient.setAfterAuthenticationCallback(afterAuthenticationCallback);

            return this;
        }

        public IdpClientBuilder beforeTokenMapper(
                Function<MultipartBody, MultipartBody> beforeTokenMapper) {
            idpClient.setBeforeTokenMapper(beforeTokenMapper);

            return this;
        }

        public IdpClientBuilder afterTokenCallback(
                Consumer<HttpResponse<JsonNode>> afterTokenCallback) {
            idpClient.setAfterTokenCallback(afterTokenCallback);

            return this;
        }

        public IdpClientBuilder authenticatorClient(AuthenticatorClient authenticatorClient) {
            idpClient.setAuthenticatorClient(authenticatorClient);

            return this;
        }

        public IdpClientBuilder codeChallengeMethod(CodeChallengeMethod codeChallengeMethod) {
            idpClient.setCodeChallengeMethod(codeChallengeMethod);

            return this;
        }

        public IdpClientBuilder authorizationResponseMapper(
                Function<AuthorizationResponse, AuthorizationResponse> authorizationResponseMapper) {
            idpClient.setAuthorizationResponseMapper(authorizationResponseMapper);

            return this;
        }

        public IdpClientBuilder authenticationResponseMapper(
                Function<AuthenticationResponse, AuthenticationResponse> authenticationResponseMapper) {
            idpClient.setAuthenticationResponseMapper(authenticationResponseMapper);

            return this;
        }

        public IdpClientBuilder discoveryDocumentResponse(
                DiscoveryDocumentResponse discoveryDocumentResponse) {
            idpClient.setDiscoveryDocumentResponse(discoveryDocumentResponse);

            return this;
        }

        public IdpClient build() {
            return idpClient;
        }
    }
}
