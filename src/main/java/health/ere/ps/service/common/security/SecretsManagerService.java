package health.ere.ps.service.common.security;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.service.config.UserConfigurationService;
import health.ere.ps.service.connector.endpoint.SSLUtilities;

@ApplicationScoped
public class SecretsManagerService {

    private static final Logger log = Logger.getLogger(SecretsManagerService.class.getName());

    @Inject
    AppConfig appConfig;

    @Inject
    UserConfigurationService userConfigurationService;

    @Inject
    Event<Exception> exceptionEvent;

    private SSLContext sslContext;


    public SecretsManagerService() {
    }

    @PostConstruct
    void createSSLContext() {
        if(userConfigurationService.getConfig().getClientCertificate() != null && !userConfigurationService.getConfig().getClientCertificate().isEmpty()) {
            String clientCertificateString = userConfigurationService.getConfig().getClientCertificate().substring(33);
            byte[] clientCertificateBytes = Base64.getDecoder().decode(clientCertificateString);
            try (ByteArrayInputStream certificateInputStream = new ByteArrayInputStream(clientCertificateBytes)) {
                setUpSSLContext(userConfigurationService.getConfig().getClientCertificatePassword(), certificateInputStream);
            } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException
                    | UnrecoverableKeyException | KeyManagementException e) {
                log.severe("There was a problem when creating the SSLContext:");
                e.printStackTrace();
                exceptionEvent.fireAsync(e);
            }
        } else if (appConfig.getCertAuthStoreFile().isPresent() && appConfig.getCertAuthStoreFilePassword().isPresent()) {
            String connectorTlsCertAuthStoreFile = appConfig.getCertAuthStoreFile().get();
            String connectorTlsCertAuthStorePwd = appConfig.getCertAuthStoreFilePassword().get();

            try (FileInputStream certificateInputStream = new FileInputStream(connectorTlsCertAuthStoreFile)) {
                setUpSSLContext(connectorTlsCertAuthStorePwd, certificateInputStream);
            } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException
                    | UnrecoverableKeyException | KeyManagementException e) {
                log.severe("There was a problem when creating the SSLContext:");
                e.printStackTrace();
                exceptionEvent.fireAsync(e);
            }
        } else {
            // For the connector trust all certificates
            try {
                sslContext = SSLContext.getInstance(SslContextType.TLS.getSslContextType());
                sslContext.init(null, new TrustManager[]{new SSLUtilities.FakeX509TrustManager()},
                    null);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                log.severe("There was a problem when creating the SSLContext:");
                e.printStackTrace();
                exceptionEvent.fireAsync(e);
            }
        }
    }

    private void setUpSSLContext(String connectorTlsCertAuthStorePwd, InputStream certificateInputStream)
            throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException,
            UnrecoverableKeyException, KeyManagementException {
        sslContext = SSLContext.getInstance(SslContextType.TLS.getSslContextType());

        KeyStore ks = KeyStore.getInstance(KeyStoreType.PKCS12.getKeyStoreType());
        ks.load(certificateInputStream, connectorTlsCertAuthStorePwd.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, connectorTlsCertAuthStorePwd.toCharArray());

        sslContext.init(kmf.getKeyManagers(), new TrustManager[]{new SSLUtilities.FakeX509TrustManager()},
                null);
    }

    public void updateSSLContext() {
        createSSLContext();
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
