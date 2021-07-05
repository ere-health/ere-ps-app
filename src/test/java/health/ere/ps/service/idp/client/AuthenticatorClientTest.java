package health.ere.ps.service.idp.client;

import health.ere.ps.config.AppConfig;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.AuthorizationRequest;
import health.ere.ps.model.idp.client.AuthorizationResponse;
import health.ere.ps.model.idp.client.DiscoveryDocumentResponse;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.model.idp.client.field.CodeChallengeMethod;
import health.ere.ps.model.idp.client.field.IdpScope;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AuthenticatorClientTest {

    @Inject
    AuthenticatorClient authenticatorClient;

    @Inject
    Logger logger;

    @Inject
    AppConfig appConfig;

    @Test
    void test_Successful_Authorization_Request() throws IdpClientException, IdpException {
        AuthenticatorClient authenticatorClient = new AuthenticatorClient();

        AuthorizationResponse authorizationResponse =
                authenticatorClient.doAuthorizationRequest(AuthorizationRequest.builder()
                .clientId(appConfig.getClientId())
                .link(appConfig.getAuthRequestURL())
                .codeChallenge(ClientUtilities.generateCodeChallenge(
                        ClientUtilities.generateCodeVerifier()))
                .codeChallengeMethod(CodeChallengeMethod.S256)
                .redirectUri(appConfig.getRedirectURL())
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

        try(Response response = idpHttpClientService.doGenericGetRequest()) {
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