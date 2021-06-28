package health.ere.ps.config;

import health.ere.ps.service.connector.endpoint.EndpointDiscoveryService;
import org.apache.commons.lang3.StringUtils;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AppConfig {

    @ConfigProperty(name = "connector.cert.auth.store.file")
    String idpConnectorTlsCertTrustStore;

    @ConfigProperty(name = "connector.cert.auth.store.file.password")
    String idpConnectorTlsCertTustStorePwd;

    @ConfigProperty(name = "idp.client.id")
    String clientId;

    @ConfigProperty(name = "connector.client.system.id")
    String clientSystemId;

    @ConfigProperty(name = "connector.mandant.id")
    String mandantId;

    @ConfigProperty(name = "connector.workplace.id")
    String workplaceId;

    @ConfigProperty(name = "connector.card.handle")
    String cardHandle;

    @ConfigProperty(name = "connector.context.userId")
    String signatureServiceContextUserId;

    @ConfigProperty(name = "connector.simulator.titusClientCertificate")
    String titusClientCertificate;

    @ConfigProperty(name = "ere.validator.validate.sign.request.bundles.enabled")
    String validateSignRequestBundles;

    @Inject
    EndpointDiscoveryService endpointDiscoveryService;

    public String getIdpConnectorTlsCertTrustStore() {
        return idpConnectorTlsCertTrustStore;
    }

    public String getIdpConnectorTlsCertTustStorePwd() {
        return StringUtils.defaultString(idpConnectorTlsCertTustStorePwd).trim();
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSystem() {
        return clientSystemId;
    }

    public String getWorkplace() {
        return workplaceId;
    }

    public String getCardHandle() {
        return cardHandle;
    }

    public String getIdpConnectorAuthSignatureEndpointAddress() {
        return endpointDiscoveryService.getAuthSignatureServiceEndpointAddress();
    }

    public String getSignatureServiceContextUserId() {
        return signatureServiceContextUserId;
    }

    public String getTitusClientCertificate() {
        return titusClientCertificate;
    }

    public String getMandantId() {
        return mandantId;
    }

    public void setMandantId(String mandantId) {
        this.mandantId = mandantId;
    }

    public boolean isValidateSignRequestBundles() {
        return StringUtils.isNotBlank(validateSignRequestBundles) &&
                validateSignRequestBundles.equalsIgnoreCase("Yes");
    }

    public String getAuthSignatureServiceEndpointAddress() {
        return endpointDiscoveryService.getAuthSignatureServiceEndpointAddress();
    }

    public String getCertificateServiceEndpointAddress() {
        return endpointDiscoveryService.getCertificateServiceEndpointAddress();
    }

    public String getSignatureServiceEndpointAddress() {
        return endpointDiscoveryService.getSignatureServiceEndpointAddress();
    }

    public String getEventServiceEndpointAddress() {
        return endpointDiscoveryService.getEventServiceEndpointAddress();
    }
}
