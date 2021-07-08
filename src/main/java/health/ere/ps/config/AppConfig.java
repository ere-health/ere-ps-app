package health.ere.ps.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppConfig {

    @ConfigProperty(name = "directory-watcher.dir")
    String directoryWatcherDir;

    @ConfigProperty(name = "ere.validator.validate.sign.request.bundles.enabled")
    boolean validateSignRequestBundles;

    @ConfigProperty(name = "ere.workflow-service.prescription.server.url")
    String prescriptionServiceURL;

    @ConfigProperty(name = "connector.base-uri")
    String connectorBaseURI;

    @ConfigProperty(name = "connector.version")
    String connectorVersion;

    @ConfigProperty(name = "connector.mandant.id")
    String mandantId;

    @ConfigProperty(name = "connector.workplace.id")
    String workplaceId;

    @ConfigProperty(name = "connector.client.system.id")
    String clientSystemId;

    @ConfigProperty(name = "connector.context.userId")
    String userId;

    @ConfigProperty(name = "connector.verify-hostname")
    String verifyHostname;

    @ConfigProperty(name = "idp.client.id")
    String idpClientId;

    @ConfigProperty(name = "idp.auth.request.redirect.url")
    String idpAuthRequestRedirectURL;

    @ConfigProperty(name = "idp.auth.request.url")
    String idpAuthRequestURL;

    @ConfigProperty(name = "idp.base.url")
    String idpBaseURL;

    @ConfigProperty(name = "ere-workflow-service.vau.enable")
    boolean enableVau;

    @ConfigProperty(name = "ere-workflow-service.user-agent")
    String userAgent;

    @ConfigProperty(name = "connector.crypt")
    String connectorCrypt;

    @ConfigProperty(name = "connector.tvMode")
    String tvMode;

    @ConfigProperty(name = "connector.cert.auth.store.file")
    String certAuthStoreFile;

    @ConfigProperty(name = "connector.cert.auth.store.file.password")
    String certAuthStoreFilePassword;

    public String getDirectoryWatcherDir() {
        return directoryWatcherDir;
    }

    public boolean isValidateSignRequestBundles() {
        return validateSignRequestBundles;
    }

    public String getPrescriptionServiceURL() {
        return prescriptionServiceURL;
    }

    public String getConnectorBaseURI() {
        return connectorBaseURI;
    }

    public String getConnectorVersion() {
        return connectorVersion;
    }

    public String getMandantId() {
        return mandantId;
    }

    public String getWorkplaceId() {
        return workplaceId;
    }

    public String getClientSystemId() {
        return clientSystemId;
    }

    public String getUserId() {
        return userId;
    }

    public String getVerifyHostname() {
        return verifyHostname;
    }

    public String getIdpClientId() {
        return idpClientId;
    }

    public String getIdpAuthRequestRedirectURL() {
        return idpAuthRequestRedirectURL;
    }

    public String getIdpAuthRequestURL() {
        return idpAuthRequestURL;
    }

    public String getIdpBaseURL() {
        return idpBaseURL;
    }

    public boolean vauEnabled() {
        return enableVau;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getConnectorCrypt() {
        return connectorCrypt;
    }

    public String getTvMode() {
        return tvMode;
    }

    public String getCertAuthStoreFile() {
        return certAuthStoreFile;
    }

    public String getCertAuthStoreFilePassword() {
        return certAuthStoreFilePassword;
    }
}
