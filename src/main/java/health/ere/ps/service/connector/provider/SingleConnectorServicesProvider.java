package health.ere.ps.service.connector.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.logging.Logger;

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

    public UserConfig getUserConfig() {
        return userConfig;
    }
}
