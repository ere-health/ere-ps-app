package health.ere.ps.service.idp.client;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.idp.crypto.CryptoLoader;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class IdpClientTest {
    
    @ConfigProperty(name = "idp.cert.store.file")
    String idpCertStoreFile;

    @ConfigProperty(name = "idp.base.url")
    String idpBaseUrl;

    @ConfigProperty(name = "idp.client.id")
    String clientId;

    String discoveryDocumentUrl;

    @ConfigProperty(name = "idp.auth.request.redirect.url")
    String redirectUrl;

    private AuthenticatorClient authenticatorClient;

    @Test
    public void test_Successful_Idp_Login() throws IOException {
        AuthenticatorClient authenticatorClient = new AuthenticatorClient();
        final String codeVerifier = ClientUtilities.generateCodeVerifier();
        final String nonce = RandomStringUtils.randomAlphanumeric(20);

        discoveryDocumentUrl = idpBaseUrl + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;

        IdpClient idpClient = new IdpClient.IdpClientBuilder()
                .clientId(clientId)
                .authenticatorClient(authenticatorClient)
                .discoveryDocumentUrl(discoveryDocumentUrl)
                .redirectUrl(redirectUrl)
                .build().initialize();

        try (InputStream is = getClass().getResourceAsStream(idpCertStoreFile)) {
            PkiIdentity identity = CryptoLoader.getIdentityFromP12(is, "00");

            IdpTokenResult idpTokenResult = idpClient.login(identity);

            Assertions.assertNotNull(idpTokenResult);
        }
    }
}
