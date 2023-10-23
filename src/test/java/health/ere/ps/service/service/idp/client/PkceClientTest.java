package health.ere.ps.service.idp.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class PkceClientTest {
    @Test
    // https://tools.ietf.org/html/rfc7636#section-4.1
    public void checkAlphabetforCodeVerifier() {
        final String codeVerifier = ClientUtilities.generateCodeVerifier();
        assertTrue(codeVerifier.matches("[\\w-_.~]*"));
    }

    @Test
    // https://tools.ietf.org/html/rfc7636#section-4.1
    public void checkLengthOfCodeVerifier() {
        final String codeVerifier = ClientUtilities.generateCodeVerifier();
        assertTrue(codeVerifier.length() >= 43 || codeVerifier.length() <= 128);
    }

    @Test
    // https://tools.ietf.org/html/rfc7636#section-4.1
    public void checkEachCodeVerifierIsDifferent() {
        final String firstCodeVerifier = ClientUtilities.generateCodeVerifier();
        final String seccondCodeVerifier = ClientUtilities.generateCodeVerifier();
        assertTrue(!firstCodeVerifier.equals(seccondCodeVerifier));
    }

    @Test
    // rfc7636 Appendix B
    // This example is in rfc7636 Appendix B
    public void checkTransformationS256() {
        assertTrue(ClientUtilities.generateCodeChallenge(
                "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk")
            .equals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"));
    }

    @Test
    // base64urlencode auf output von sha256 gibt laenger 43 oder 44
    // rfc7636
    public void checkLengthOfCodeChellange() {
        final String codeChellange = ClientUtilities.generateCodeChallenge(ClientUtilities.generateCodeVerifier());
        assertTrue(codeChellange.length() == 43);
    }
}
