package health.ere.ps.service.idp.client;

import org.apache.commons.codec.digest.DigestUtils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.crypto.spec.SecretKeySpec;

import health.ere.ps.model.idp.client.IdpConstants;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.client.authentication.AuthenticationChallenge;
import health.ere.ps.model.idp.client.authentication.AuthenticationChallengeBuilder;
import health.ere.ps.service.idp.client.authentication.AuthenticationChallengeVerifier;
import health.ere.ps.model.idp.client.authentication.AuthenticationResponse;
import health.ere.ps.model.idp.client.authentication.AuthenticationResponseBuilder;
import health.ere.ps.model.idp.client.authentication.AuthenticationTokenBuilder;
import health.ere.ps.model.idp.client.authentication.IdpJwtProcessor;
import health.ere.ps.model.idp.client.authentication.JwtBuilder;
import health.ere.ps.model.idp.client.data.UserConsentConfiguration;
import health.ere.ps.model.idp.client.data.UserConsentDescriptionTexts;
import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.model.idp.client.field.IdpScope;
import health.ere.ps.model.idp.client.token.AccessTokenBuilder;
import health.ere.ps.model.idp.client.token.IdpJwe;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import health.ere.ps.model.idp.crypto.PkiIdentity;

public class MockIdpClient implements IIdpClient {
    private PkiIdentity serverIdentity;
    private String clientId;
    private boolean produceTokensWithInvalidSignature;
    private boolean produceOnlyExpiredTokens;
    private final String uriIdpServer = IdpConstants.DEFAULT_SERVER_URL;
    private final String serverSubSalt = "someArbitrarySubSaltValue";
    private AccessTokenBuilder accessTokenBuilder;
    private AuthenticationResponseBuilder authenticationResponseBuilder;
    private AuthenticationTokenBuilder authenticationTokenBuilder;
    private AuthenticationChallengeBuilder authenticationChallengeBuilder;
    private IdpJwtProcessor jwtProcessor;
    private SecretKeySpec encryptionKey;

    public MockIdpClient(PkiIdentity serverIdentity, String clientId,
                         boolean produceTokensWithInvalidSignature,
                         boolean produceOnlyExpiredTokens,
                         AccessTokenBuilder accessTokenBuilder,
                         AuthenticationResponseBuilder authenticationResponseBuilder,
                         AuthenticationTokenBuilder authenticationTokenBuilder,
                         AuthenticationChallengeBuilder authenticationChallengeBuilder,
                         IdpJwtProcessor jwtProcessor, SecretKeySpec encryptionKey) {
        this.setServerIdentity(serverIdentity);
        this.setClientId(clientId);
        this.setProduceTokensWithInvalidSignature(produceTokensWithInvalidSignature);
        this.setProduceOnlyExpiredTokens(produceOnlyExpiredTokens);
        this.setAccessTokenBuilder(accessTokenBuilder);
        this.setAuthenticationResponseBuilder(authenticationResponseBuilder);
        this.setAuthenticationTokenBuilder(authenticationTokenBuilder);
        this.setAuthenticationChallengeBuilder(authenticationChallengeBuilder);
        this.setJwtProcessor(jwtProcessor);
        this.setEncryptionKey(encryptionKey);
    }

    public MockIdpClient() {
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MockIdpClient{");
        sb.append("serverIdentity=").append(serverIdentity);
        sb.append(", clientId='").append(clientId).append('\'');
        sb.append(", produceTokensWithInvalidSignature=").append(produceTokensWithInvalidSignature);
        sb.append(", produceOnlyExpiredTokens=").append(produceOnlyExpiredTokens);
        sb.append(", uriIdpServer='").append(uriIdpServer).append('\'');
        sb.append(", serverSubSalt='").append(serverSubSalt).append('\'');
        sb.append(", accessTokenBuilder=").append(accessTokenBuilder);
        sb.append(", authenticationResponseBuilder=").append(authenticationResponseBuilder);
        sb.append(", authenticationTokenBuilder=").append(authenticationTokenBuilder);
        sb.append(", authenticationChallengeBuilder=").append(authenticationChallengeBuilder);
        sb.append(", jwtProcessor=").append(jwtProcessor);
        sb.append(", encryptionKey=").append(encryptionKey);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MockIdpClient)) return false;
        MockIdpClient that = (MockIdpClient) o;
        return isProduceTokensWithInvalidSignature() == that.isProduceTokensWithInvalidSignature() &&
                isProduceOnlyExpiredTokens() == that.isProduceOnlyExpiredTokens() &&
                getServerIdentity().equals(that.getServerIdentity()) &&
                getClientId().equals(that.getClientId()) &&
                getUriIdpServer().equals(that.getUriIdpServer()) &&
                getServerSubSalt().equals(that.getServerSubSalt()) &&
                getAccessTokenBuilder().equals(that.getAccessTokenBuilder()) &&
                getAuthenticationResponseBuilder().equals(that.getAuthenticationResponseBuilder()) &&
                getAuthenticationTokenBuilder().equals(that.getAuthenticationTokenBuilder()) &&
                getAuthenticationChallengeBuilder().equals(that.getAuthenticationChallengeBuilder()) &&
                getJwtProcessor().equals(that.getJwtProcessor()) &&
                getEncryptionKey().equals(that.getEncryptionKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerIdentity(), getClientId(), isProduceTokensWithInvalidSignature(),
                isProduceOnlyExpiredTokens(), getUriIdpServer(), getServerSubSalt(),
                getAccessTokenBuilder(), getAuthenticationResponseBuilder(),
                getAuthenticationTokenBuilder(), getAuthenticationChallengeBuilder(),
                getJwtProcessor(), getEncryptionKey());
    }

    @Override
    public IdpTokenResult login(final PkiIdentity clientIdentity) {
        assertThatMockIdClientIsInitialized();

        return IdpTokenResult.builder()
            .accessToken(buildAccessToken(clientIdentity))
            .validUntil(LocalDateTime.now().plusMinutes(5))
            .build();
    }

    private JsonWebToken buildAccessToken(final PkiIdentity clientIdentity) {
        final AuthenticationChallenge challenge = getAuthenticationChallengeBuilder()
            .buildAuthenticationChallenge(getClientId(), "placeholderValue", "foo", "foo",
                IdpScope.OPENID.getJwtValue() + " " + IdpScope.EREZEPT.getJwtValue(), "nonceValue");
        final AuthenticationResponse authenticationResponse = getAuthenticationResponseBuilder()
            .buildResponseForChallenge(challenge, clientIdentity);
        final IdpJwe authenticationToken = getAuthenticationTokenBuilder()
            .buildAuthenticationToken(clientIdentity.getCertificate(),
                authenticationResponse.getSignedChallenge().getBodyClaim(ClaimName.NESTED_JWT)
                    .map(Objects::toString)
                    .map(JsonWebToken::new)
                    .map(JsonWebToken::getBodyClaims)
                    .orElseThrow(),
                ZonedDateTime.now());

        JsonWebToken accessToken = getAccessTokenBuilder().buildAccessToken(
            authenticationToken.decryptNestedJwt(getEncryptionKey()));

        if (isProduceOnlyExpiredTokens()) {
            accessToken = resignToken(accessToken.getHeaderClaims(),
                accessToken.getBodyClaims(),
                ZonedDateTime.now().minusMinutes(10));
        }

        if (isProduceTokensWithInvalidSignature()) {
            final List<String> strings = Arrays.asList(accessToken.getRawString().split("\\."));
            strings.set(2, strings.get(2) + "mvK");
            accessToken = new JsonWebToken(strings.stream()
                .collect(Collectors.joining(".")));
        }

        return accessToken;
    }

    public JsonWebToken resignToken(
        final Map<String, Object> headerClaims,
        final Map<String, Object> bodyClaims,
        final ZonedDateTime expiresAt) {
        Objects.requireNonNull(getJwtProcessor(), "jwtProcessor is null. Did you call initialize()?");
        return getJwtProcessor().buildJwt(new JwtBuilder()
            .addAllBodyClaims(bodyClaims)
            .addAllHeaderClaims(headerClaims)
            .expiresAt(expiresAt));
    }

    @Override
    public MockIdpClient initialize() {
        getServerIdentity().setKeyId(Optional.of("puk_idp_sig"));
        getServerIdentity().setUse(Optional.of("sig"));
        setJwtProcessor(new IdpJwtProcessor(getServerIdentity()));
        setAccessTokenBuilder(new AccessTokenBuilder(getJwtProcessor(), getUriIdpServer(), getServerSubSalt()));
        setAuthenticationChallengeBuilder(AuthenticationChallengeBuilder.builder()
            .serverSigner(new IdpJwtProcessor(getServerIdentity()))
            .uriIdpServer(getUriIdpServer())
            .userConsentConfiguration(UserConsentConfiguration.builder()
                .claimsToBeIncluded(Map.of(IdpScope.OPENID, List.of(),
                    IdpScope.EREZEPT, List.of(),
                    IdpScope.PAIRING, List.of()))
                .descriptionTexts(UserConsentDescriptionTexts.builder()
                    .claims(Collections.emptyMap())
                    .scopes(Map.of(IdpScope.OPENID, "openid",
                        IdpScope.PAIRING, "pairing",
                        IdpScope.EREZEPT, "erezept"))
                    .build())
                .build())
            .build());
        setAuthenticationResponseBuilder(new AuthenticationResponseBuilder());
        setEncryptionKey(new SecretKeySpec(DigestUtils.sha256("fdsa"), "AES"));
        setAuthenticationTokenBuilder(AuthenticationTokenBuilder.builder()
            .jwtProcessor(getJwtProcessor())
            .authenticationChallengeVerifier(new AuthenticationChallengeVerifier(getServerIdentity()))
            .encryptionKey(getEncryptionKey())
            .build());
        return this;
    }

    private void assertThatMockIdClientIsInitialized() {
        Objects.requireNonNull(getAccessTokenBuilder(), "accessTokenBuilder is null. Did you call initialize()?");
        Objects.requireNonNull(getAuthenticationTokenBuilder(),
            "authenticationTokenBuilder is null. Did you call initialize()?");
        Objects.requireNonNull(getClientId(), "clientId is null. You have to set it!");
    }

    public PkiIdentity getServerIdentity() {
        return serverIdentity;
    }

    public String getClientId() {
        return clientId;
    }

    public boolean isProduceTokensWithInvalidSignature() {
        return produceTokensWithInvalidSignature;
    }

    public boolean isProduceOnlyExpiredTokens() {
        return produceOnlyExpiredTokens;
    }

    public String getUriIdpServer() {
        return uriIdpServer;
    }

    public String getServerSubSalt() {
        return serverSubSalt;
    }

    public AccessTokenBuilder getAccessTokenBuilder() {
        return accessTokenBuilder;
    }

    public AuthenticationResponseBuilder getAuthenticationResponseBuilder() {
        return authenticationResponseBuilder;
    }

    public AuthenticationTokenBuilder getAuthenticationTokenBuilder() {
        return authenticationTokenBuilder;
    }

    public AuthenticationChallengeBuilder getAuthenticationChallengeBuilder() {
        return authenticationChallengeBuilder;
    }

    public IdpJwtProcessor getJwtProcessor() {
        return jwtProcessor;
    }

    public SecretKeySpec getEncryptionKey() {
        return encryptionKey;
    }

    public static MockIdpClientBuilder builder() {
        return new MockIdpClientBuilder();
    }

    public void setServerIdentity(PkiIdentity serverIdentity) {
        this.serverIdentity = serverIdentity;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setProduceTokensWithInvalidSignature(boolean produceTokensWithInvalidSignature) {
        this.produceTokensWithInvalidSignature = produceTokensWithInvalidSignature;
    }

    public void setProduceOnlyExpiredTokens(boolean produceOnlyExpiredTokens) {
        this.produceOnlyExpiredTokens = produceOnlyExpiredTokens;
    }

    public void setAccessTokenBuilder(AccessTokenBuilder accessTokenBuilder) {
        this.accessTokenBuilder = accessTokenBuilder;
    }

    public void setAuthenticationResponseBuilder(AuthenticationResponseBuilder authenticationResponseBuilder) {
        this.authenticationResponseBuilder = authenticationResponseBuilder;
    }

    public void setAuthenticationTokenBuilder(AuthenticationTokenBuilder authenticationTokenBuilder) {
        this.authenticationTokenBuilder = authenticationTokenBuilder;
    }

    public void setAuthenticationChallengeBuilder(AuthenticationChallengeBuilder authenticationChallengeBuilder) {
        this.authenticationChallengeBuilder = authenticationChallengeBuilder;
    }

    public void setJwtProcessor(IdpJwtProcessor jwtProcessor) {
        this.jwtProcessor = jwtProcessor;
    }

    public void setEncryptionKey(SecretKeySpec encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public static class MockIdpClientBuilder {
        private MockIdpClient mockIdpClient;

        public MockIdpClientBuilder() {
            mockIdpClient = new MockIdpClient();
        }

        public MockIdpClientBuilder serverIdentity(PkiIdentity serverIdentity) {
            mockIdpClient.setServerIdentity(serverIdentity);

            return this;
        }

        public MockIdpClientBuilder clientId(String clientId) {
            mockIdpClient.setClientId(clientId);

            return this;
        }

        public MockIdpClientBuilder produceTokensWithInvalidSignature(
                boolean produceTokensWithInvalidSignature) {
            mockIdpClient.setProduceTokensWithInvalidSignature(produceTokensWithInvalidSignature);

            return this;
        }

        public MockIdpClientBuilder produceOnlyExpiredTokens(boolean produceOnlyExpiredTokens) {
            mockIdpClient.setProduceOnlyExpiredTokens(produceOnlyExpiredTokens);

            return this;
        }

        public MockIdpClientBuilder accessTokenBuilder(AccessTokenBuilder accessTokenBuilder) {
            mockIdpClient.setAccessTokenBuilder(accessTokenBuilder);

            return this;
        }

        public MockIdpClientBuilder authenticationResponseBuilder(
                AuthenticationResponseBuilder authenticationResponseBuilder) {
            mockIdpClient.setAuthenticationResponseBuilder(authenticationResponseBuilder);

            return this;
        }

        public MockIdpClientBuilder authenticationTokenBuilder(
                AuthenticationTokenBuilder authenticationTokenBuilder) {
            mockIdpClient.setAuthenticationTokenBuilder(authenticationTokenBuilder);

            return this;
        }

        public MockIdpClientBuilder authenticationChallengeBuilder(
                AuthenticationChallengeBuilder authenticationChallengeBuilder) {
            mockIdpClient.setAuthenticationChallengeBuilder(authenticationChallengeBuilder);

            return this;
        }

        public MockIdpClientBuilder jwtProcessor(IdpJwtProcessor jwtProcessor) {
            mockIdpClient.setJwtProcessor(jwtProcessor);

            return this;
        }

        public MockIdpClientBuilder encryptionKey(SecretKeySpec encryptionKey) {
            mockIdpClient.setEncryptionKey(encryptionKey);

            return this;
        }

        public MockIdpClient build() {
            return mockIdpClient;
        }
    }
}
