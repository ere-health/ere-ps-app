
package health.ere.ps.service.idp.crypto;

import de.gematik.idp.crypto.exceptions.IdpCryptoException;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CertificateAnalysis {

    private static final String OID_HBA_AUT = "1.2.276.0.76.4.75"; // A_4445, gemSpec_oid
    private static final String OID_SMC_B_AUT = "1.2.276.0.76.4.77"; // A_4445, gemSpec_oid
    private static final String OID_EGK_AUT = "1.2.276.0.76.4.70"; // A_4445, gemSpec_oid

    public static boolean doesCertificateContainPolicyExtensionOid(final X509Certificate certificate,
        final ASN1ObjectIdentifier policyOid) {
        try {
            final byte[] policyBytes = certificate.getExtensionValue(Extension.certificatePolicies.toString());
            if (policyBytes == null) {
                return false;
            }

            final CertificatePolicies policies = CertificatePolicies
                .getInstance(JcaX509ExtensionUtils.parseExtensionValue(policyBytes));
            return Stream.of(policies.getPolicyInformation())
                .map(PolicyInformation::getPolicyIdentifier)
                .anyMatch(policyId -> policyId.equals(policyOid));
        } catch (final IOException e) {
            throw new IdpCryptoException("Error while checking Policy-Extension!", e);
        }
    }


    public static de.gematik.idp.crypto.TiCertificateType determineCertificateType(final X509Certificate certificate) {
        if (doesCertificateContainPolicyExtensionOid(certificate, new ASN1ObjectIdentifier(OID_HBA_AUT))) {
            return de.gematik.idp.crypto.TiCertificateType.HBA;
        }
        if (doesCertificateContainPolicyExtensionOid(certificate, new ASN1ObjectIdentifier(OID_SMC_B_AUT))) {
            return de.gematik.idp.crypto.TiCertificateType.SMCB;
        }
        if (doesCertificateContainPolicyExtensionOid(certificate, new ASN1ObjectIdentifier(OID_EGK_AUT))) {
            return de.gematik.idp.crypto.TiCertificateType.EGK;
        }
        return de.gematik.idp.crypto.TiCertificateType.UNKNOWN;
    }
}
