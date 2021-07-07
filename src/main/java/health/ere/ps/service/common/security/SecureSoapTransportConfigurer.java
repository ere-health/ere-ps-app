package health.ere.ps.service.common.security;

import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.ws.BindingProvider;

import health.ere.ps.exception.common.security.SecretsManagerException;

@Dependent
public class SecureSoapTransportConfigurer {
    @Inject
    SecretsManagerService secretsManagerService;

    private BindingProvider bindingProvider;

    public void init(SoapClient soapClient) {
        this.bindingProvider = soapClient.getBindingProvider();
    }

    public void configureSecureTransport(String endpointAddress,
                                         SecretsManagerService.SslContextType sslContextType,
                                         String tlsCertTrustStore,
                                         String tlsCertTrustStorePassword) {
        if (bindingProvider != null && StringUtils.isNotBlank(endpointAddress) &&
                StringUtils.isNotBlank(tlsCertTrustStore) &&
                StringUtils.isNotBlank(tlsCertTrustStorePassword)) {
            bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                    endpointAddress);

            secretsManagerService.configureSSLTransportContext(tlsCertTrustStore,
                    tlsCertTrustStorePassword, sslContextType,
                    SecretsManagerService.KeyStoreType.PKCS12, bindingProvider);
        }
    }
}
