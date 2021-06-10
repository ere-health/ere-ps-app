package health.ere.ps.config;

import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppConfig {
	@Inject
	Logger logger;

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

    public String getIdpConnectorTlsCertTrustStore() {

        return idpConnectorTlsCertTrustStore;
    }

    public String getIdpConnectorTlsCertTustStorePwd() {
		String password = StringUtils.defaultString(
		idpConnectorTlsCertTustStorePwd).trim();
		
		logger.info("Idp Connector Password is: " + password);
		
        return password;
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
}
