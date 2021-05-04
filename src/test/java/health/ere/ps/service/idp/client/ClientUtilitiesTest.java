package health.ere.ps.service.idp.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientUtilitiesTest {
    private static final String BASE64_URL_REGEX = "^[0-9a-zA-Z\\-\\.~_]+$";
    private static final int SHA256_AS_B64_LENGTH = 43;

    @Test
    public void generateCodeChallengeFromVerifier() {
        final String codeVerifier = ClientUtilities.generateCodeVerifier();

        final String codeChallenge = ClientUtilities.generateCodeChallenge(codeVerifier);

        assertTrue(codeChallenge
                .matches(BASE64_URL_REGEX) &&
                codeChallenge.equals(ClientUtilities.generateCodeChallenge(codeVerifier)) &&
                        codeChallenge.length() == SHA256_AS_B64_LENGTH);
    }
}
