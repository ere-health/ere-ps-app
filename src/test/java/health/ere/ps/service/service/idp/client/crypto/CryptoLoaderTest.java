package health.ere.ps.service.idp.client.crypto;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.model.idp.client.token.PublicKey;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.idp.client.authentication.Before;
import health.ere.ps.service.idp.crypto.CryptoLoader;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class CryptoLoaderTest {

    private static final BouncyCastleProvider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();
    private static final String P12_PASSWORD = "yourP12Password";
    private static byte[] validCertificateP12; // Initialize with a valid P12 certificate in byte[] format
    private static byte[] validCertificatePEM; // Initialize with a valid PEM certificate in byte[] format
    private static byte[] validAsn1DERCertificate; // Initialize with a valid ASN.1 DER certificate in byte[] format

    @Before
    public static void setUp() {
        // Initialize valid certificate byte arrays here
    }

    @Test
    public void testGetCertificateFromP12() throws IdpCryptoException {
        X509Certificate certificate = CryptoLoader.getCertificateFromP12(validCertificateP12, P12_PASSWORD);
        assertNotNull(certificate);
    }

    @Test
    public void testGetCertificateFromPem() {
        X509Certificate certificate = CryptoLoader.getCertificateFromPem(validCertificatePEM);
        assertNotNull(certificate);
    }

    @Test
    public void testGetCertificateFromAsn1DERCertBytes() throws CryptoException {
        X509Certificate certificate = CryptoLoader.getCertificateFromAsn1DERCertBytes(validAsn1DERCertificate);
        assertNotNull(certificate);
    }

    @Test
    public void testGetIdentityFromP12() throws IdpCryptoException {
        InputStream p12InputStream = new ByteArrayInputStream(validCertificateP12);
        PkiIdentity pkiIdentity = CryptoLoader.getIdentityFromP12(p12InputStream, P12_PASSWORD);
        assertNotNull(pkiIdentity);
        assertNotNull(pkiIdentity.getCertificate());
        assertNotNull(pkiIdentity.getPrivateKey());
    }

    @Test
    public void testGetEcPublicKeyFromBytes() throws IdpCryptoException {
        byte[] publicKeyBytes = null; // Initialize with valid EC public key bytes
        PublicKey publicKey = (PublicKey) CryptoLoader.getEcPublicKeyFromBytes(publicKeyBytes);
        assertNotNull(publicKey);
    }
}
