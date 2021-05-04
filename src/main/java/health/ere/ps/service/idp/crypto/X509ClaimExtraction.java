package health.ere.ps.service.idp.crypto;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.isismtt.ISISMTTObjectIdentifiers;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.security.auth.x500.X500Principal;

import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.model.idp.crypto.CertificateExtractedFieldEnum;


/**
 * Implements the extraction of claims from certificates according to A_20524
 */
public class X509ClaimExtraction {

    private static final int KVNR_LENGTH = 10; // gemSpec_PKI, 4.2

    private X509ClaimExtraction() {

    }

    /**
     * Detects the certificate-type and returns a key/value store for claims and the corresponding values.
     *
     * @param certificateData
     * @return
     */
    public static Map<String, Object> extractClaimsFromCertificate(final byte[] certificateData) {
        return extractClaimsFromCertificate(CryptoLoader.getCertificateFromPem(certificateData));
    }

    public static Map<String, Object> extractClaimsFromCertificate(final X509Certificate certificate) {
        final HashMap<String, Object> claimMap = new HashMap<>();
        final TiCertificateType certificateType = TiCertificateType.determineCertificateType(certificate);
        claimMap.put(CertificateExtractedFieldEnum.GIVEN_NAME.getFieldname(),
            getValueFromDn(certificate.getSubjectX500Principal(), RFC4519Style.givenName)
                .orElse(null));
        claimMap.put(CertificateExtractedFieldEnum.FAMILY_NAME.getFieldname(), getValueFromDn(certificate.getSubjectX500Principal(), RFC4519Style.sn)
            .orElse(null));

        if (certificateType == TiCertificateType.HBA) {
            claimMap.put(CertificateExtractedFieldEnum.ORGANIZATION_NAME.getFieldname(), null);
        } else if (certificateType == TiCertificateType.SMCB) {
            claimMap.put(CertificateExtractedFieldEnum.ORGANIZATION_NAME.getFieldname(),
                getValueFromDn(certificate.getSubjectX500Principal(), RFC4519Style.o)
                    .orElse(null));
        } else if (certificateType == TiCertificateType.EGK) {
            claimMap.put(CertificateExtractedFieldEnum.ORGANIZATION_NAME.getFieldname(),
                getValueFromDn(certificate.getSubjectX500Principal(), RFC4519Style.o)
                    .orElse(null));
        }

        claimMap.put(CertificateExtractedFieldEnum.PROFESSION_OID.getFieldname(), getProfessionOid(certificate)
            .map(ASN1ObjectIdentifier::toString)
            .orElse(null));

        if (certificateType == TiCertificateType.HBA) {
            claimMap.put(CertificateExtractedFieldEnum.ID_NUMMER.getFieldname(), getRegistrationNumber(certificate)
                .orElse(null));
        } else if (certificateType == TiCertificateType.SMCB) {
            claimMap.put(CertificateExtractedFieldEnum.ID_NUMMER.getFieldname(), getRegistrationNumber(certificate)
                .orElse(null));
        } else if (certificateType == TiCertificateType.EGK) {
            claimMap.put(CertificateExtractedFieldEnum.ID_NUMMER.getFieldname(),
                getAllValuesFromDn(certificate.getSubjectX500Principal(), RFC4519Style.ou)
                    .stream()
                    .filter(ou -> ou.length() == KVNR_LENGTH)
                    .findFirst()
                    .orElseThrow(() -> new IdpCryptoException(
                        "Could not find OU in EGK Subject-DN: '" + certificate.getSubjectDN().toString())));
        }
        return claimMap;
    }

    private static Optional<String> getValueFromDn(final X500Principal principal, final ASN1ObjectIdentifier field) {
        return getAllValuesFromDn(principal, field)
            .stream()
            .findFirst();
    }

    private static List<String> getAllValuesFromDn(final X500Principal principal, final ASN1ObjectIdentifier field) {
        return Stream.of(X500Name.getInstance(principal.getEncoded())
            .getRDNs(field))
            .flatMap(rdn -> Stream.of(rdn.getTypesAndValues()))
            .filter(attributeTypeAndValue -> attributeTypeAndValue.getType().equals(field))
            .map(AttributeTypeAndValue::getValue)
            .map(Objects::toString)
            .collect(Collectors.toList());
    }

    private static Optional<ASN1ObjectIdentifier> getProfessionOid(final X509Certificate certificate) {
        final Optional<DLSequence> admissionEntry = getAdmissionEntry(certificate);
        if (admissionEntry.isEmpty()) {
            return Optional.empty();
        }
        for (final ASN1Encodable encodable : admissionEntry.get()) {
            if (encodable instanceof DLSequence) {
                final ASN1Encodable obj = ((DLSequence) encodable).getObjectAt(0);
                if (obj instanceof ASN1ObjectIdentifier) {
                    return Optional.of((ASN1ObjectIdentifier) obj);
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<String> getRegistrationNumber(final X509Certificate certificate) {
        final Optional<DLSequence> admissionEntry = getAdmissionEntry(certificate);
        if (admissionEntry.isEmpty()) {
            return Optional.empty();
        }
        for (final ASN1Encodable encodable : admissionEntry.get()) {
            if (encodable instanceof DERPrintableString) {
                return Optional.ofNullable(((DERPrintableString) encodable).getString());
            }
        }
        return Optional.empty();
    }

    private static Optional<DLSequence> getAdmissionEntry(final X509Certificate certificate) {
        try {
            final byte[] data = certificate.getExtensionValue(ISISMTTObjectIdentifiers.id_isismtt_at_admission.getId());
            if (data == null) {
                return Optional.empty();
            }

            final ASN1Encodable parsedValue = JcaX509ExtensionUtils.parseExtensionValue(data);
            final DLSequence a = (DLSequence) parsedValue;
            DLSequence b = null;
            final Iterator<ASN1Encodable> iterator = a.iterator();
            while (iterator.hasNext()) {
                final ASN1Encodable next = iterator.next();
                if (next instanceof DLSequence) {
                    b = (DLSequence) next;
                }
            }
            if (b == null) {
                return Optional.empty();
            }
            final DLSequence c = (DLSequence) b.getObjectAt(0);
            final DLSequence d = (DLSequence) c.getObjectAt(0);
            return Optional.ofNullable((DLSequence) d.getObjectAt(0));
        } catch (final IOException e) {
            throw new IdpCryptoException(e);
        }
    }
}
