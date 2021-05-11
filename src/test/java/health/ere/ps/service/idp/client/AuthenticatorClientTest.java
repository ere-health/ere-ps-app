package health.ere.ps.service.idp.client;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import health.ere.ps.model.idp.client.token.JsonWebToken;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class AuthenticatorClientTest {
    @Inject
    Logger logger;

    @ConfigProperty(name = "idp.base.url")
    String idpBaseUrl;

    @Test
    void test_Successful_Retrieval_Of_Server_Cert_From_Location_Using_Idp_Http_Client()
            throws MalformedURLException {
        IdpHttpClientService idpHttpClientService = RestClientBuilder.newBuilder()
                .baseUrl(new URL(idpBaseUrl))
                .build(IdpHttpClientService.class);

        Response certResponse = idpHttpClientService.getServerCertsList();
        String jsonString = certResponse.readEntity(String.class);
        JsonWebToken jsonWebToken = new JsonWebToken(jsonString);

        logger.info("Status = " + certResponse.getStatus());
        certResponse.getHeaders().entrySet().stream().forEach(
                (entry -> logger.info(entry.getKey() + " = " + entry.getValue())));
        logger.info("Body = " + jsonWebToken.getPayloadDecoded());

        assertNotNull(jsonString);
    }

    @Test
    void test_Successful_Retrieval_Of_Server_Cert_From_Location_Using_Auth_Client() throws MalformedURLException {
        AuthenticatorClient authenticatorClient = new AuthenticatorClient();
        X509Certificate cert = authenticatorClient.retrieveServerCertFromLocation(idpBaseUrl);

        assertNotNull(cert);
    }
}