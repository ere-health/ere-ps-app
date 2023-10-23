package health.ere.ps.service.idp.client.crypto;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import health.ere.ps.service.idp.client.authentication.Before;
import health.ere.ps.service.idp.crypto.CertificateAnalysis;
import health.ere.ps.service.idp.crypto.TiCertificateType;

import java.io.IOException;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

public class CertificateAnalysisTest {

    @Mock
    private X509Certificate mockCertificate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDoesCertificateContainPolicyExtensionOidWithNonMatchingOid() throws IOException {
        byte[] policyBytes = null;
        when(mockCertificate.getExtensionValue("2.5.29.32")).thenReturn(policyBytes);

        boolean result = CertificateAnalysis.doesCertificateContainPolicyExtensionOid(
                mockCertificate,
                new ASN1ObjectIdentifier("2.5.29.32")
        );

        assertFalse(result);
    }

    @Test
    public void testDetermineCertificateTypeUnknown() throws IOException {
        when(mockCertificate.getExtensionValue("2.5.29.32")).thenReturn(null);

        TiCertificateType result = CertificateAnalysis.determineCertificateType(mockCertificate);

        assertEquals(TiCertificateType.UNKNOWN, result);
    }
}
