package health.ere.ps.config;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AppConfig {

    @ConfigProperty(name = "directory-watcher.dir")
    String directoryWatcherDir;

    @ConfigProperty(name = "ere.workflow-service.prescription.server.url")
    String prescriptionServiceURL;

    @ConfigProperty(name = "connector.version")
    String connectorVersion;

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

    @ConfigProperty(name = "connector.cert.auth.store.file")
    Optional<String> certAuthStoreFile;

    @ConfigProperty(name = "connector.cert.auth.store.file.password")
    Optional<String> certAuthStoreFilePassword;

    public String getDirectoryWatcherDir() {
        return directoryWatcherDir;
    }

    public String getPrescriptionServiceURL() {
        return prescriptionServiceURL;
    }

    public String getConnectorVersion() {
        return connectorVersion;
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

    public Optional<String> getCertAuthStoreFile() {
        return certAuthStoreFile;
    }

    public Optional<String> getCertAuthStoreFilePassword() {
        return certAuthStoreFilePassword;
    }

    public void setConnectorVersion(String connectorVersion) {
        this.connectorVersion = connectorVersion;
    }
}
