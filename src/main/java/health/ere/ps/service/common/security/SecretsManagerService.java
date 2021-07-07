package health.ere.ps.service.common.security;

import com.sun.xml.ws.developer.JAXWSProperties;
import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.service.connector.endpoint.SSLUtilities;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.xml.ws.BindingProvider;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class SecretsManagerService {

    private static final Logger log = Logger.getLogger(SecretsManagerService.class.getName());

    @Inject
    AppConfig appConfig;

    public SecretsManagerService() {
    }

    public KeyStore createTrustStore(String trustStoreFilePath, KeyStoreType keyStoreType, char[] keyStorePassword)
            throws SecretsManagerException {
        KeyStore ks;

        try {
            ks = KeyStore.getInstance(keyStoreType.getKeyStoreType());
            ks.load(null, keyStorePassword);

            Path tsFile = Paths.get(trustStoreFilePath);

            if (!Files.exists(tsFile) && tsFile.toFile().getParentFile() != null) {
                tsFile.toFile().getParentFile().mkdirs();
                tsFile.toFile().createNewFile();
            }

            try (FileOutputStream trustStoreOutputStream = new FileOutputStream(trustStoreFilePath)) {
                ks.store(trustStoreOutputStream, keyStorePassword);
            }
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
            throw new SecretsManagerException("Error creating trust store.", e);
        }

        return ks;
    }

    public KeyStore initializeTrustStoreFromInputStream(InputStream trustStoreInputStream,
                                                        KeyStoreType keyStoreType,
                                                        char[] keyStorePassword)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore trustStore = KeyStore.getInstance(keyStoreType.getKeyStoreType());
        trustStore.load(trustStoreInputStream, keyStorePassword);

        return trustStore;
    }

    public void configureSSLTransportContext(String trustStoreFilePath,
                                             String trustStorePassword,
                                             SslContextType sslContextType,
                                             KeyStoreType keyStoreType,
                                             BindingProvider bp) throws SecretsManagerException {

        if ("!".equals(trustStoreFilePath)) {
            return;
        }
        try (FileInputStream fileInputStream = new FileInputStream(trustStoreFilePath)) {
            SSLContext sc = createSSLContext(fileInputStream, trustStorePassword.toCharArray(),
                    sslContextType, keyStoreType, bp);

            bp.getRequestContext().put("com.sun.xml.ws.transport.https.client.SSLSocketFactory",
                    sc.getSocketFactory());

        } catch (IOException e) {
            throw new SecretsManagerException("SSL transport configuration error.", e);
        }
    }

    public SSLContext createSSLContext(InputStream trustStoreInputStream, char[] keyStorePassword,
                                       SslContextType sslContextType, KeyStoreType keyStoreType, BindingProvider bp)
            throws SecretsManagerException {
        SSLContext sc;

        try {
            sc = SSLContext.getInstance(sslContextType.getSslContextType());

            bp.getRequestContext().put(JAXWSProperties.HOSTNAME_VERIFIER, new SSLUtilities.FakeHostnameVerifier());

            KeyManagerFactory kmf =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

            KeyStore ks = KeyStore.getInstance(keyStoreType.getKeyStoreType());
            ks.load(trustStoreInputStream, keyStorePassword);

            kmf.init(ks, keyStorePassword);

            sc.init(kmf.getKeyManagers(), new TrustManager[]{new SSLUtilities.FakeX509TrustManager()}, null);
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException
                | UnrecoverableKeyException | KeyManagementException e) {
            throw new SecretsManagerException("SSL context creation error.", e);
        }

        return sc;
    }

    public SSLContext setUpCustomSSLContext(String p12CertificateFile) {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

            KeyStore ks = KeyStore.getInstance("PKCS12");
            // Download this file from the titus backend
            // https://frontend.titus.ti-dienste.de/#/platform/mandant
            String pwd = appConfig.getIdpConnectorTlsCertTustStorePwd();
            ks.load(new FileInputStream(p12CertificateFile), pwd.toCharArray());
            kmf.init(ks, pwd.toCharArray());
            sc.init(kmf.getKeyManagers(), new TrustManager[]{new SSLUtilities.FakeX509TrustManager()}, null);
            return sc;
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException
                | UnrecoverableKeyException | KeyManagementException e) {
            log.log(Level.SEVERE, "Could not set up custom SSLContext", e);
            throw new RuntimeException(e);
        }
    }

    public enum SslContextType {
        SSL("SSL"), TLS("TLS");

        private final String sslContextType;

        SslContextType(String sslContextType) {
            this.sslContextType = sslContextType;
        }

        public String getSslContextType() {
            return sslContextType;
        }
    }

    public enum KeyStoreType {
        JKS("jks"), PKCS12("pkcs12");

        private final String keyStoreType;

        KeyStoreType(String keyStoreType) {
            this.keyStoreType = keyStoreType;
        }

        public String getKeyStoreType() {
            return keyStoreType;
        }
    }
}
