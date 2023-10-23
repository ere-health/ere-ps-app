package health.ere.ps.service.connector.endpoint;

import javax.net.ssl.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SSLUtilitiesTest {

    @Test
    public void testIsDeprecatedSSLProtocol() {
        final boolean isDeprecated = SSLUtilities.isDeprecatedSSLProtocol();
        assertFalse(isDeprecated);
    }

    @Test
    public void testTrustAllHostnames() {
        SSLUtilities.trustAllHostnames();
        HostnameVerifier verifier = HttpsURLConnection.getDefaultHostnameVerifier();
        assertNotNull(verifier);
        assertTrue(verifier.verify("example.com", null));
    }

    @Test
    public void testTrustAllHttpsCertificates() {
        SSLUtilities.trustAllHttpsCertificates();
        SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();
        assertNotNull(factory);
        try {
            SSLSocket socket = (SSLSocket) factory.createSocket();
            socket.startHandshake();
        } catch (Exception e) {
            fail("Exception should not be thrown for a trusted certificate.");
        }
    }

    @Test
    public void testFakeHostnameVerifier() {
        SSLUtilities.FakeHostnameVerifier verifier = new SSLUtilities.FakeHostnameVerifier();
        assertTrue(verifier.verify("example.com", null));
    }

    @Test
    public void testFakeX509TrustManager() {
        SSLUtilities.FakeX509TrustManager trustManager = new SSLUtilities.FakeX509TrustManager();
        X509Certificate[] chain = new X509Certificate[0];

        trustManager.checkClientTrusted(chain, "RSA");
        trustManager.checkServerTrusted(chain, "RSA");
        X509Certificate[] acceptedIssuers = trustManager.getAcceptedIssuers();
        assertNotNull(acceptedIssuers);
        assertEquals(0, acceptedIssuers.length);
    }

    @Test
    public void testReset() {
        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();
        SSLUtilities.reset();

        HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        SSLSocketFactory socketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();

        assertNotEquals(hostnameVerifier, SSLUtilities.get_hostnameVerifier());
        assertNotEquals(socketFactory, SSLUtilities.get_trustManagers());
    }
}
