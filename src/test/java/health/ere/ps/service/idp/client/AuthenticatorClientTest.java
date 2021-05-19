package health.ere.ps.service.idp.client;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import health.ere.ps.model.idp.client.AuthorizationRequest;
import health.ere.ps.model.idp.client.AuthorizationResponse;
import health.ere.ps.model.idp.client.DiscoveryDocumentResponse;
import health.ere.ps.model.idp.client.field.CodeChallengeMethod;
import health.ere.ps.model.idp.client.field.IdpScope;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AuthenticatorClientTest {
    @Inject
    Logger logger;

    @ConfigProperty(name = "idp.base.url")
    String idpBaseUrl;

    @ConfigProperty(name = "idp.client.id")
    String idpCientId;
    
    @ConfigProperty(name = "idp.auth.request.url")
    String idpAuthRequestUrl;

    @ConfigProperty(name = "idp.auth.request.redirect.url")
    String idpAuthRequestRedirectUrl;

    @Test
    void test_Successful_Authorization_Request() {
        AuthenticatorClient authenticatorClient = new AuthenticatorClient();

        AuthorizationResponse authorizationResponse =
                authenticatorClient.doAuthorizationRequest(AuthorizationRequest.builder()
                .clientId(idpCientId)
                .link(idpAuthRequestUrl)
                .codeChallenge(ClientUtilities.generateCodeChallenge(
                        ClientUtilities.generateCodeVerifier()))
                .codeChallengeMethod(CodeChallengeMethod.S256)
                .redirectUri(idpAuthRequestRedirectUrl)
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
    void test_Successful_Retrieval_Of_Discovery_Document_Using_Idp_Http_Client() {
        IdpHttpClientService idpHttpClientService =
                AuthenticatorClient.getIdpHttpClientInstanceByUrl(
                        idpBaseUrl + IdpHttpClientService.DISCOVERY_DOCUMENT_URI);

        Response response = idpHttpClientService.doGenericGetRequest();
        String jsonString = response.readEntity(String.class);
        JsonWebToken jsonWebToken = new JsonWebToken(jsonString);

        logger.info("Status = " + response.getStatus());
        response.getHeaders().entrySet().stream().forEach(
                (entry -> logger.info(entry.getKey() + " = " + entry.getValue())));
        logger.info("Body = " + jsonWebToken.getPayloadDecoded());

        assertNotNull(jsonString);
    }

    @Test
    void test_Successful_Retrieval_Of_Discovery_Document_Using_Auth_Client() {
        AuthenticatorClient authenticatorClient = new AuthenticatorClient();
        DiscoveryDocumentResponse discoveryDocumentResponse =
                authenticatorClient.retrieveDiscoveryDocument(
                idpBaseUrl + IdpHttpClientService.DISCOVERY_DOCUMENT_URI);

        assertNotNull(discoveryDocumentResponse, "Discovery Document Present");
        assertNotNull(discoveryDocumentResponse.getIdpSig(), "Idp Signature Cert Present");
        assertNotNull(discoveryDocumentResponse.getIdpEnc(), "Idp Pub Key Present");
    }
}