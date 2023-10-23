package health.ere.ps.service.idp.client.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.cert.X509Certificate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import health.ere.ps.model.idp.crypto.CertificateExtractedFieldEnum;
import health.ere.ps.service.idp.crypto.X509ClaimExtraction;

public class X509ClaimExtractionTest {

    @Test
    public void testExtractClaimsFromCertificate() {
        // Load a test X509 certificate (you should replace with your own certificate data)
        byte[] certificateData = getTestCertificateData();

        // Extract claims from the certificate
        Map<String, Object> claimMap = X509ClaimExtraction.extractClaimsFromCertificate(certificateData);

        // Add your assertions here to validate the extracted claims
        assertNotNull(claimMap);
        // Example assertions:
        assertEquals("John", claimMap.get(CertificateExtractedFieldEnum.GIVEN_NAME.getFieldname()));
        assertEquals("Doe", claimMap.get(CertificateExtractedFieldEnum.FAMILY_NAME.getFieldname()));
    }

    // Helper method to load test certificate data (replace with your own)
    private byte[] getTestCertificateData() {
        // Replace this with your test certificate data
        return new byte[0];
    }
}
