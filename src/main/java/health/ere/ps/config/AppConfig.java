package health.ere.ps.config;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppConfig {
	
	@ConfigProperty(name = "idp.connector.cert.auth.store.file")
    String idpConnectorTlsCertTrustStore;

    @ConfigProperty(name = "idp.connector.cert.auth.store.file.password")
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

    @ConfigProperty(name = "idp.connector.auth-signature.endpoint.address")
    String idpConnectorAuthSignatureEndpointAddress;

    @ConfigProperty(name = "ere.signature-service.context.userId")
    String signatureServiceContextUserId;

    @ConfigProperty(name = "connector.simulator.titusClientCertificate")
    String titusClientCertificate;

    @ConfigProperty(name = "titus.event-service.endpoint.address")
    String eventServiceEndpointAddress;

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
        return idpConnectorAuthSignatureEndpointAddress;
    }

    public String getSignatureServiceContextUserId() {
        return signatureServiceContextUserId;
    }

    public String getEventServiceEndpointAddress() {
        return eventServiceEndpointAddress;
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
}
