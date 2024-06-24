package health.ere.ps.service.idp.client;

import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.AuthorizationRequest;
import health.ere.ps.model.idp.client.AuthorizationResponse;
import health.ere.ps.model.idp.client.DiscoveryDocumentResponse;
import health.ere.ps.model.idp.client.field.CodeChallengeMethod;
import health.ere.ps.model.idp.client.field.IdpScope;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import health.ere.ps.profile.TitusTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
class AuthenticatorClientTest {

    @Inject
    AuthenticatorClient authenticatorClient;

    private static final Logger logger = Logger.getLogger(AuthenticatorClientTest.class.getName());

    @Inject
    AppConfig appConfig;

    @ConfigProperty(name = "idp.auth.request.redirect.url")
    String idpAuthRequestRedirectUrl;

    @Test
    @Tag("titus")
    void test_Successful_Authorization_Request() throws IdpClientException, IdpException {
        AuthenticatorClient authenticatorClient = new AuthenticatorClient();

        AuthorizationResponse authorizationResponse =
                authenticatorClient.doAuthorizationRequest(AuthorizationRequest.builder()
                .clientId(appConfig.getIdpClientId())
                .link(appConfig.getIdpAuthRequestURL())
                .codeChallenge(ClientUtilities.generateCodeChallenge(
                        ClientUtilities.generateCodeVerifier()))
                .codeChallengeMethod(CodeChallengeMethod.S256)
                .redirectUri(appConfig.getIdpAuthRequestRedirectURL())
                .state(RandomStringUtils.randomAlphanumeric(20))
                .scopes(java.util.Set.of(IdpScope.OPENID, IdpScope.EREZEPT))
                .nonce(RandomStringUtils.randomAlphanumeric(20))
                .build());

        assertNotNull(authorizationResponse.getAuthenticationChallenge(),
                "Auth Challenge Present");

        assertNotNull(authorizationResponse.getAuthenticationChallenge().getUserConsent(),
                "User Consent Present");

        assertTrue(
                MapUtils.isNotEmpty(authorizationResponse.getAuthenticationChallenge()
                        .getUserConsent().getRequestedClaims()),
                "User consent requested claims map is present");

        assertTrue(
                MapUtils.isNotEmpty(authorizationResponse.getAuthenticationChallenge()
                        .getUserConsent().getRequestedScopes()),
                "User consent scopes map is present");

        assertNotNull(authorizationResponse.getAuthenticationChallenge().getChallenge(),
                "Challenge Response Present");

        logger.info("User consent scopes: " +
                authorizationResponse.getAuthenticationChallenge()
                        .getUserConsent().getRequestedScopes());
        logger.info("User consent claims: " +
                authorizationResponse.getAuthenticationChallenge()
                        .getUserConsent().getRequestedClaims());
        logger.info("Auth challenge: " +
                authorizationResponse.getAuthenticationChallenge()
                        .getChallenge());
    }

    @Test
    void test_Successful_Retrieval_Of_Discovery_Document_Using_Idp_Http_Client()
            throws IdpClientException {
        IdpHttpClientService idpHttpClientService =
                authenticatorClient.getIdpHttpClientInstanceByUrl(
                        appConfig.getIdpBaseURL() + IdpHttpClientService.DISCOVERY_DOCUMENT_URI);

        try (Response response = idpHttpClientService.doGenericGetRequest()) {
            String jsonString = response.readEntity(String.class);
            JsonWebToken jsonWebToken = new JsonWebToken(jsonString);

            logger.info("Status = " + response.getStatus());
            response.getHeaders().entrySet().stream().forEach(
                    (entry -> logger.info(entry.getKey() + " = " + entry.getValue())));
            logger.info("Body = " + jsonWebToken.getPayloadDecoded());

            assertNotNull(jsonString);
        }
    }

    @Test
    void test_Successful_Retrieval_Of_Discovery_Document_Using_Auth_Client()
            throws IdpClientException, IdpException, IdpJoseException {
        DiscoveryDocumentResponse discoveryDocumentResponse =
                authenticatorClient.retrieveDiscoveryDocument(
                        appConfig.getIdpBaseURL() + IdpHttpClientService.DISCOVERY_DOCUMENT_URI);

        assertNotNull(discoveryDocumentResponse, "Discovery Document Present");
        assertNotNull(discoveryDocumentResponse.getIdpSig(), "Idp Signature Cert Present");
        assertNotNull(discoveryDocumentResponse.getIdpEnc(), "Idp Pub Key Present");
    }
}