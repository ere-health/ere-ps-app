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

    @ConfigProperty(name = "idp.connector.mandant.id")
    String mandantId;

    @ConfigProperty(name = "idp.connector.workplace.id")
    String workplace;

    @ConfigProperty(name = "idp.connector.card.handle")
    String cardHandle;

    @ConfigProperty(name = "idp.connector.auth-signature.endpoint.address")
    String idpConnectorAuthSignatureEndpointAddress;

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

    public String getMandantId() {
        return mandantId;
    }
}
