package health.ere.ps.service.idp.client;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class AuthenticatorClientTest {
    @Inject
    Logger logger;

    @Disabled
    @Test
    void test_Successful_Retrieval_Of_Server_Cert_From_Location_Using_Idp_Http_Client()
            throws MalformedURLException {
        // "http://url.des.idp/.well-known/openid-configuration"
        // "http://localhost:8080/auth/realms/idp/.well-known/openid-configuration"
        // " https://idp.erezept-instanz1.titus.ti-dienste.de"

        IdpHttpClientService idpHttpClientService = RestClientBuilder.newBuilder()
                .baseUrl(new URL("https://idp.erezept-instanz1.titus.ti-dienste.de"))
                .build(IdpHttpClientService.class);

        Response certResponse = idpHttpClientService.getServerCertsList();

        String jsonString = certResponse.readEntity(String.class);

        logger.info("Status = " + certResponse.getStatus());
        certResponse.getHeaders().entrySet().stream().forEach(
                (entry -> logger.info(entry.getKey() + " = " + entry.getValue())));
        logger.info("Body = " + jsonString);

        assertNotNull(jsonString);
    }

    @Disabled
    @Test
    void test_Successful_Retrieval_Of_Server_Cert_From_Location_Using_Auth_Client() {
        // "http://url.des.idp/.well-known/openid-configuration"
        // "http://localhost:8080/auth/realms/idp/.well-known/openid-configuration"
        // " https://idp.erezept-instanz1.titus.ti-dienste.de"

        AuthenticatorClient authenticatorClient = new AuthenticatorClient();

        assertNotNull(authenticatorClient.retrieveServerCertFromLocation(
                "https://idp.erezept-instanz1.titus.ti-dienste.de"));
    }
}