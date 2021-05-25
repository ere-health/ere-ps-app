package health.ere.ps.service.common.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import javax.enterprise.context.ApplicationScoped;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

@ApplicationScoped
public class SecretsManagerService {
    public enum SslContextType {
        SSL("SSL"), TLS("TLS");

        SslContextType(String sslContextType) {
            this.sslContextType = sslContextType;
        }

        private String sslContextType;

        public String getSslContextType() {
            return sslContextType;
        }
    }

    public enum KeyStoreType {
        JKS("jks"), PKCS12("pkcs12");

        KeyStoreType(String keyStoreType) {
            this.keyStoreType = keyStoreType;
        }

        private String keyStoreType;

        public String getKeyStoreType() {
            return keyStoreType;
        }
    }

    public SecretsManagerService() {

    }

    public KeyStore createTrustStore(String trustStoreFilePath,
                               KeyStoreType keyStoreType,
                               char[] keyStorePassword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance(keyStoreType.getKeyStoreType());

        ks.load(null, keyStorePassword);

        try(FileOutputStream trustStoreOutputStream = new FileOutputStream(trustStoreFilePath)) {
            ks.store(trustStoreOutputStream, keyStorePassword);
        }

        return ks;
    }

    public KeyStore saveTrustedCertificate(String trustStoreFilePath, char[] keyStorePassword,
                                           String certificateAlias,
                                       Certificate certificate) throws CertificateException,
            KeyStoreException, IOException, NoSuchAlgorithmException {
        KeyStore trustStore = initializeTrustStoreFromFile(trustStoreFilePath,
                keyStorePassword);

        trustStore.setCertificateEntry(certificateAlias, certificate);

        return trustStore;
    }

    public void deleteTrustStore(String trustStorePath) throws IOException {
        Files.delete(Paths.get(trustStorePath));
    }

    public KeyStore initializeTrustStoreFromFile(String trustStoreFilePath,
                                        char[] keyStorePassword)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore trustStore = KeyStore.getInstance(new File(trustStoreFilePath), keyStorePassword);
        
        return trustStore;
    }

    public KeyStore initializeTrustStoreFromInputStream(InputStream trustStoreInputStream,
                                                        KeyStoreType keyStoreType,
                                                 char[] keyStorePassword)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore trustStore = KeyStore.getInstance(keyStoreType.getKeyStoreType());
        trustStore.load(trustStoreInputStream, keyStorePassword);

        return trustStore;
    }

    public SSLContext createSSLContext(InputStream trustStoreInputStream, char[] keyStorePassword,
                                    SslContextType sslContextType, KeyStoreType keyStoreType)
            throws Exception {
        SSLContext sc = SSLContext.getInstance(sslContextType.getSslContextType());

        KeyManagerFactory kmf =
                KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );

        KeyStore ks = KeyStore.getInstance(keyStoreType.getKeyStoreType());
        ks.load(trustStoreInputStream, keyStorePassword);

        kmf.init(ks, keyStorePassword);

        sc.init( kmf.getKeyManagers(), null, null );

        return sc;
    }
}
