package health.ere.ps.service.connector.provider;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.logging.Logger;
import java.net.URI;
import java.net.URISyntaxException;

import javax.enterprise.event.Event;

import health.ere.ps.config.UserConfig;
import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.connector.endpoint.EndpointDiscoveryService;

public class SingleConnectorServicesProvider extends AbstractConnectorServicesProvider {
    private final static Logger log = Logger.getLogger(SingleConnectorServicesProvider.class.getName());

    UserConfig userConfig;

    public SingleConnectorServicesProvider(UserConfig userConfig, Event<Exception> exceptionEvent) {
        this.userConfig = userConfig;
        this.secretsManagerService = new SecretsManagerService();
        if(userConfig.getConfigurations().getClientCertificate() != null && !userConfig.getConfigurations().getClientCertificate().isEmpty()) {
            
            String clientCertificateString = userConfig.getConfigurations().getClientCertificate().substring(33);
            byte[] clientCertificateBytes = Base64.getDecoder().decode(clientCertificateString);
            
            // byte[] clientCertificateBytes = getCertificateFromUriString(userConfig.getConfigurations().getClientCertificate(), userConfig.getConfigurations().getClientCertificatePassword());
            try (ByteArrayInputStream certificateInputStream = new ByteArrayInputStream(clientCertificateBytes)) {
                this.secretsManagerService.setUpSSLContext(userConfig.getConfigurations().getClientCertificatePassword(), certificateInputStream);
            } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException
            | UnrecoverableKeyException | KeyManagementException e) {
                log.severe("There was a problem when creating the SSLContext:");
                e.printStackTrace();
                exceptionEvent.fireAsync(e);
            }
        }
        this.endpointDiscoveryService = new EndpointDiscoveryService(userConfig, secretsManagerService);

        initializeServices();
    }

    public static byte[] getCertificateFromUriString(String uri, String keystorePassword) throws URISyntaxException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        String keystoreBase64;
        KeyStore store = KeyStore.getInstance("PKCS12");

        // parse URL with java.net.URL
        URI uriParser = new URI(uri);
        String scheme = uriParser.getScheme();
        if (scheme.equalsIgnoreCase("data")){
            String[] schemeSpecificParts = uriParser.getSchemeSpecificPart().split(";");
            String contentType = schemeSpecificParts[0];
            if (contentType.equalsIgnoreCase("application/x-pkcs12")){
                String[] dataParts = schemeSpecificParts[1].split(",");
                String encodingType = dataParts[0];
                if (encodingType.equalsIgnoreCase("base64")){
                    keystoreBase64 = dataParts[1];
                    try (ByteArrayInputStream keystoreInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(keystoreBase64))){
                      store.load(keystoreInputStream, keystorePassword.toCharArray());
                    }
                    Certificate certificate = store.getCertificate(store.aliases().nextElement());
                    return certificate.getEncoded();
                }
            }
        } else if (scheme.equalsIgnoreCase("file")) {
            String[] schemeSpecificParts = uriParser.getSchemeSpecificPart().split("?");
            String keystoreFile = schemeSpecificParts[0];
            String keyAlias = schemeSpecificParts[1];
            store.load(new FileInputStream(keystoreFile), keystorePassword.toCharArray());
            Certificate certificate = store.getCertificate(keyAlias);
            return certificate.getEncoded();
        }

        // figure out if it is a data or a file url

        // if a data url, return data as byte array

        // if file url
        // search for parameter alias
        // read file as P12 Keystore
        // read key with alias alias from P12 keystore
        return null;
    }

    public UserConfig getUserConfig() {
        return userConfig;
    }
}
