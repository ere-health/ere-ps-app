package health.ere.ps.service.idp.crypto;

import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.common.security.SecretsManagerService;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Enumeration;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

public class CryptoLoader {

    private static final BouncyCastleProvider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();

    public static X509Certificate getCertificateFromP12(final byte[] crt,
                                                        final String p12Password)
            throws IdpCryptoException {
        try {
            final KeyStore p12 = KeyStore.getInstance("pkcs12", BOUNCY_CASTLE_PROVIDER);
            p12.load(new ByteArrayInputStream(crt), p12Password.toCharArray());
            final Enumeration<String> e = p12.aliases();
            while (e.hasMoreElements()) {
                final String alias = e.nextElement();
                return (X509Certificate) p12.getCertificate(alias);
            }
        } catch (final IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new IdpCryptoException(e);
        }
        throw new IdpCryptoException("Could not find certificate in P12-File");
    }

    public static X509Certificate getCertificateFromPem(final byte[] crt) {
        try {
            final CertificateFactory certFactory = CertificateFactory.getInstance("X.509", BOUNCY_CASTLE_PROVIDER);
            final InputStream in = new ByteArrayInputStream(crt);
            final X509Certificate x509Certificate = (X509Certificate) certFactory.generateCertificate(in);
            if (x509Certificate == null) {
                throw new IllegalStateException("Error while loading certificate!");
            }
            return x509Certificate;
        } catch (final CertificateException ex) {
            throw new IllegalStateException("Error while loading certificate!", ex);
        }
    }

    public static PkiIdentity getIdentityFromP12(InputStream p12FileInputStream,
                                                 final String p12Password) throws IdpCryptoException {
        try {
            final KeyStore p12 = KeyStore.getInstance("pkcs12", BOUNCY_CASTLE_PROVIDER);

            p12.load(p12FileInputStream, p12Password.toCharArray());

            final Enumeration<String> e = p12.aliases();

            while (e.hasMoreElements()) {
                final String alias = e.nextElement();
                final X509Certificate certificate = (X509Certificate) p12.getCertificate(alias);
                final PrivateKey privateKey = (PrivateKey) p12.getKey(alias, p12Password.toCharArray());
                return new PkiIdentity(certificate, privateKey, Optional.empty(), null);
            }
        } catch (final IOException | KeyStoreException | NoSuchAlgorithmException
                | UnrecoverableKeyException | CertificateException e) {
            throw new IdpCryptoException(e);
        }
        throw new IdpCryptoException("Could not find certificate in P12-File");
    }

    public static PublicKey getEcPublicKeyFromBytes(final byte[] keyBytes) throws IdpCryptoException {
        final X509EncodedKeySpec publicKeyEncoded = new X509EncodedKeySpec(keyBytes);
        try {
            final KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePublic(publicKeyEncoded);
        } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IdpCryptoException(e);
        }
    }
}
