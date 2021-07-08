package health.ere.ps.service.common.security;

import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.service.connector.endpoint.SSLUtilities;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.logging.Logger;

@ApplicationScoped
public class SecretsManagerService {

    private static final Logger log = Logger.getLogger(SecretsManagerService.class.getName());

    @ConfigProperty(name = "connector.cert.auth.store.file.password", defaultValue = "")
    String connectorTlsCertAuthStorePwd;
    @ConfigProperty(name = "connector.cert.auth.store.file", defaultValue = "")
    String connectorTlsCertAuthStoreFile;

    private SSLContext sslContext;


    public SecretsManagerService() {
    }

    @PostConstruct
    void createSSLContext() {
        if (StringUtils.isEmpty(connectorTlsCertAuthStoreFile) || StringUtils.isEmpty(connectorTlsCertAuthStorePwd)) {
            log.severe("Certificate file or password missing, cannot instantiate SSLContext");
        } else {
            try (FileInputStream certificateInputStream = new FileInputStream(connectorTlsCertAuthStoreFile)) {
                sslContext = SSLContext.getInstance(SslContextType.TLS.getSslContextType());

                KeyStore ks = KeyStore.getInstance(KeyStoreType.PKCS12.getKeyStoreType());
                ks.load(certificateInputStream, connectorTlsCertAuthStorePwd.toCharArray());

                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ks, connectorTlsCertAuthStorePwd.toCharArray());

                sslContext.init(kmf.getKeyManagers(), new TrustManager[]{new SSLUtilities.FakeX509TrustManager()},
                        null);
            } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException
                    | UnrecoverableKeyException | KeyManagementException e) {
                log.severe("There was a problem when creating the SSLContext:");
                e.printStackTrace();
            }
        }
    }

    public SSLContext getSslContext() {
        return sslContext;
    }


    //METHODS USED IN TESTS
    KeyStore createTrustStore(String trustStoreFilePath, KeyStoreType keyStoreType, char[] keyStorePassword)
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

    KeyStore initializeTrustStoreFromInputStream(InputStream trustStoreInputStream,
                                                 KeyStoreType keyStoreType,
                                                 char[] keyStorePassword)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore trustStore = KeyStore.getInstance(keyStoreType.getKeyStoreType());
        trustStore.load(trustStoreInputStream, keyStorePassword);

        return trustStore;
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
