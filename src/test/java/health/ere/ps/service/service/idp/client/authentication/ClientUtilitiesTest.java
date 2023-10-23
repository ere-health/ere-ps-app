package health.ere.ps.service.idp.client.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.PublicKey;

import org.junit.jupiter.api.Test;

import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.service.idp.client.ClientUtilities;

public class ClientUtilitiesTest {

    @Test
    public void testGenerateCodeChallenge() {
        String codeVerifier = "random_code_verifier";
        String codeChallenge = ClientUtilities.generateCodeChallenge(codeVerifier);

        assertNotNull(codeChallenge);
        assertEquals(43, codeChallenge.length()); // Length of Base64 URL-encoded SHA-256 hash
    }

    @Test
    public void testGenerateCodeVerifier() {
        String codeVerifier = ClientUtilities.generateCodeVerifier();

        assertNotNull(codeVerifier);
        assertEquals(43, codeVerifier.length()); // Length of Base64 URL-encoded SHA-256 hash
    }

    @Test
    public void testVerifyJwtWithValidJwt() throws IdpClientException {
        PublicKey publicKey = mock(PublicKey.class);
        String validJwt = "valid_jwt";

        try {
            ClientUtilities.verifyJwt(validJwt, publicKey);
        } catch (IdpClientException e) {
            fail("Exception should not be thrown for a valid JWT.");
        }
    }

    @Override
    public String toString() {
        return "ClientUtilitiesTest []";
    }
}
