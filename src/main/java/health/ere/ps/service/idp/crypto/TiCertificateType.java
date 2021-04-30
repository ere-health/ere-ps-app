package health.ere.ps.service.idp.crypto;

import java.security.cert.X509Certificate;

public enum TiCertificateType {
    HBA,
    EGK,
    SMCB,
    UNKNOWN;

    public static TiCertificateType determineCertificateType(X509Certificate certificate) {
        return CertificateAnalysis.determineCertificateType(certificate);
    }
}
