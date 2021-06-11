package health.ere.ps.config;

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

    @ConfigProperty(name = "idp.connector.client.system.id")
    String clientSystem;

    @ConfigProperty(name = "idp.connector.workplace.id")
    String workplace;

    @ConfigProperty(name = "idp.connector.card.handle")
    String cardHandle;

    @ConfigProperty(name = "idp.connector.auth-signature.endpoint.address")
    String idpConnectorAuthSignatureEndpointAddress;

    @ConfigProperty(name = "signature-service.context.mandantId")
    String signatureServiceContextMandantId;

    @ConfigProperty(name = "signature-service.context.clientSystemId")
    String signatureServiceContextClientSystemId;

    @ConfigProperty(name = "signature-service.context.workplaceId")
    String signatureServiceContextWorkplaceId;

    @ConfigProperty(name = "signature-service.context.userId")
    String signatureServiceContextUserId;

    @ConfigProperty(name = "connector.simulator.titusClientCertificate")
    String titusClientCertificate;

    @ConfigProperty(name = "event-service.endpointAddress")
    String eventServiceEndpointAddress;

    public String getIdpConnectorTlsCertTrustStore() {

        return idpConnectorTlsCertTrustStore;
    }

    public String getIdpConnectorTlsCertTustStorePwd() {

        return idpConnectorTlsCertTustStorePwd;
    }

    public String getClientId() {

        return clientId;
    }

    public String getClientSystem() {

        return clientSystem;
    }

    public String getWorkplace() {

        return workplace;
    }

    public String getCardHandle() {

        return cardHandle;
    }

    public String getIdpConnectorAuthSignatureEndpointAddress() {
        return idpConnectorAuthSignatureEndpointAddress;
    }

    public String getSignatureServiceContextMandantId() {
        return signatureServiceContextMandantId;
    }

    public String getSignatureServiceContextClientSystemId() {
        return signatureServiceContextClientSystemId;
    }

    public String getSignatureServiceContextWorkplaceId() {
        return signatureServiceContextWorkplaceId;
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
}
