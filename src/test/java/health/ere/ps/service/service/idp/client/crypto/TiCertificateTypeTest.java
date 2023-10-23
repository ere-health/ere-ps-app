package health.ere.ps.service.idp.client.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.cert.X509Certificate;

import org.junit.jupiter.api.Test;

import health.ere.ps.service.idp.crypto.TiCertificateType;

public class TiCertificateTypeTest {

    @Test
    public void testHBA() {
        X509Certificate certificate = createCertificate("HBA");
        TiCertificateType certificateType = TiCertificateType.determineCertificateType(certificate);
        assertEquals(TiCertificateType.HBA, certificateType);
    }

    @Test
    public void testEGK() {
        X509Certificate certificate = createCertificate("EGK");
        TiCertificateType certificateType = TiCertificateType.determineCertificateType(certificate);
        assertEquals(TiCertificateType.EGK, certificateType);
    }

    @Test
    public void testSMCB() {
        X509Certificate certificate = createCertificate("SMCB");
        TiCertificateType certificateType = TiCertificateType.determineCertificateType(certificate);
        assertEquals(TiCertificateType.SMCB, certificateType);
    }

    @Test
    public void testUNKNOWN() {
        X509Certificate certificate = createCertificate("UNKNOWN_TYPE");
        TiCertificateType certificateType = TiCertificateType.determineCertificateType(certificate);
        assertEquals(TiCertificateType.UNKNOWN, certificateType);
    }

    private X509Certificate createCertificate(String string) {
        return null;
    }
}
