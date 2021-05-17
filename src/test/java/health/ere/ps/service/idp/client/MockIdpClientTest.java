package health.ere.ps.service.idp.client;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import health.ere.ps.exception.idp.IdpJwtExpiredException;
import health.ere.ps.exception.idp.IdpJwtSignatureInvalidException;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.client.brainPoolExtension.BrainpoolAlgorithmSuiteIdentifiers;
import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.idp.tests.Afo;
import health.ere.ps.model.idp.crypto.PkiKeyResolver;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(PkiKeyResolver.class)
public class MockIdpClientTest {

    private static final String URI_IDP_SERVER = "https://idp.zentral.idp.splitdns.ti-dienste.de";
    private static final String CLIENT_ID_E_REZEPT_APP = "eRezeptApp";
    private MockIdpClient mockIdpClient;
    private PkiIdentity serverIdentity;
    private PkiIdentity rsaClientIdentity;

    @BeforeEach
    public void startup(
            @PkiKeyResolver.Filename("ecc") final PkiIdentity serverIdentity,
            @PkiKeyResolver.Filename("C_CH_AUT_R2048") final PkiIdentity rsaClientIdentity) {
        this.serverIdentity = serverIdentity;
        this.rsaClientIdentity = rsaClientIdentity;

        mockIdpClient = MockIdpClient.builder()
                .serverIdentity(serverIdentity)
                .uriIdpServer(URI_IDP_SERVER)
                .clientId(CLIENT_ID_E_REZEPT_APP)
                .build();

        mockIdpClient.initialize();
    }

    @Test
    public void testLogin() {
        Assertions.assertDoesNotThrow(() -> mockIdpClient.login(rsaClientIdentity)
                .getAccessToken()
                .verify(mockIdpClient.getServerIdentity()
                        .getCertificate()
                        .getPublicKey()));
    }

    @Test
    public void verifyToken() {
        final IdpTokenResult authToken = mockIdpClient.login(rsaClientIdentity);
        authToken.getAccessToken().verify(mockIdpClient.getServerIdentity().getCertificate().getPublicKey());
    }

    @Test
    public void invalidSignatureTokens_verifyShouldFail() {
        final IdpTokenResult authToken = MockIdpClient.builder()
                .serverIdentity(serverIdentity)
                .produceTokensWithInvalidSignature(true)
                .clientId(CLIENT_ID_E_REZEPT_APP)
                .build()
                .initialize()
                .login(rsaClientIdentity);

        assertThrows(IdpJwtSignatureInvalidException.class, () -> authToken.getAccessToken()
                .verify(mockIdpClient.getServerIdentity().getCertificate().getPublicKey()));
    }

    @Test
    public void loginWithoutInitialize_shouldGiveInitializationError() {
        final MockIdpClient idpClient = MockIdpClient.builder()
                .serverIdentity(serverIdentity)
                .build();

        Throwable exception = assertThrows(NullPointerException.class,
                () -> idpClient.login(rsaClientIdentity));

        assertTrue(StringUtils.defaultString(exception.getMessage()).contains("initialize()"));
    }

    @Test
    public void expiredTokens_verifyShouldFail() {
        final IdpTokenResult authToken = MockIdpClient.builder()
                .serverIdentity(serverIdentity)
                .produceOnlyExpiredTokens(true)
                .clientId(CLIENT_ID_E_REZEPT_APP)
                .build()
                .initialize()
                .login(rsaClientIdentity);

        assertThrows(IdpJwtExpiredException.class, () -> authToken.getAccessToken().verify(
                mockIdpClient.getServerIdentity().getCertificate().getPublicKey()));
    }

    @Test
    public void verifyTokenWithEcClientCertificate(
            @PkiKeyResolver.Filename("certs/833621999741600_c.hci.aut-apo-ecc.p12") final PkiIdentity eccClientIdentity) {
       assertDoesNotThrow(() -> mockIdpClient.login(eccClientIdentity)
                .getAccessToken()
                .verify(mockIdpClient.getServerIdentity()
                        .getCertificate()
                        .getPublicKey()));
    }

    @Disabled
    @Test
    public void verifyServerSignatureEcc() {
        assertTrue(mockIdpClient.login(rsaClientIdentity)
                .getAccessToken()
                .getHeaderClaims().entrySet().stream().anyMatch(entry -> entry.getKey() ==
                                ClaimName.ALGORITHM.getJoseName() && entry.getValue() ==
                        BrainpoolAlgorithmSuiteIdentifiers.BRAINPOOL256_USING_SHA256));
    }

    @Disabled
    @Test
    public void verifyServerSignatureRsa(@PkiKeyResolver.Filename("rsa") final PkiIdentity rsaIdentity) {
        mockIdpClient = MockIdpClient.builder()
                .serverIdentity(rsaIdentity)
                .uriIdpServer(URI_IDP_SERVER)
                .clientId(CLIENT_ID_E_REZEPT_APP)
                .build();
        mockIdpClient.initialize();

        assertTrue(mockIdpClient.login(rsaClientIdentity)
                .getAccessToken()
                .getHeaderClaims().entrySet().stream().anyMatch(entry ->
                entry.getKey() == ClaimName.ALGORITHM.getJoseName() &&
                        entry.getValue() == "PS256"));
    }

    @Disabled
    @Test
    public void resignTokenWithNewBodyClaim_ShouldContainNewClaim() {
        final JsonWebToken jwt = mockIdpClient.login(rsaClientIdentity)
                .getAccessToken();

        final Map<String, Object> bodyClaims = jwt.getBodyClaims();
        bodyClaims.put("foo", "bar");

        final JsonWebToken resignedAccessToken = mockIdpClient.resignToken(
                jwt.getHeaderClaims(),
                bodyClaims,
                jwt.getExpiresAtBody());

        assertTrue(resignedAccessToken.getBodyClaims().entrySet().stream().anyMatch(entry ->
                entry.getKey() == "foo" && entry.getValue() == "bar"));
    }

    @Disabled
    @Test
    public void resignTokenWithNewHeaderClaim_ShouldContainNewHeaderClaim() {
        final JsonWebToken jwt = mockIdpClient.login(rsaClientIdentity)
                .getAccessToken();

        final Map<String, Object> jwtHeaderClaims = jwt.getHeaderClaims();
        final Map<String, Object> jwtBodyClaims = jwt.getBodyClaims();

        jwtHeaderClaims.put("foo", "bar");

        final JsonWebToken resignedAccessToken = mockIdpClient.resignToken(
                jwtHeaderClaims,
                jwtBodyClaims,
                jwt.getExpiresAtBody());

        assertTrue(resignedAccessToken.getHeaderClaims().entrySet().stream().anyMatch(entry ->
                entry.getKey() == "foo" && entry.getValue() == "bar"));
    }

    @Test
    @Afo("A_20297-01")
    public void verifyAccessTokenIssClaim() {
        final JsonWebToken jwt = mockIdpClient.login(rsaClientIdentity).getAccessToken();
        final Map<String, Object> bodyClaims = jwt.getBodyClaims();

        assertEquals(bodyClaims.get(ClaimName.ISSUER.getJoseName()), URI_IDP_SERVER);
    }
}
