package health.ere.ps.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AppConfig {

    @ConfigProperty(name = "ere.workflow-service.prescription.server.url")
    String prescriptionServerURL;

    @ConfigProperty(name = "idp.client.id")
    String clientId;

    @ConfigProperty(name = "connector.client.system.id")
    String clientSystemId;

    @ConfigProperty(name = "connector.mandant.id")
    String mandantId;

    @ConfigProperty(name = "connector.workplace.id")
    String workplaceId;

    @ConfigProperty(name = "connector.context.userId")
    String userId;

    @ConfigProperty(name = "ere.validator.validate.sign.request.bundles.enabled", defaultValue = "no")
    boolean validateSignRequestBundles;

    @ConfigProperty(name = "connector.tvMode")
    String tvMode;

    @ConfigProperty(name = "ere-workflow-service.vau.enable", defaultValue = "true")
    Boolean enableVau;

    @ConfigProperty(name = "ere-workflow-service.user-agent", defaultValue = "IncentergyGmbH-ere.health/SNAPSHOT")
    String userAgent;

    @ConfigProperty(name = "idp.auth.request.url")
    String idpAuthRequestURL;

    @ConfigProperty(name = "idp.auth.request.redirect.url")
    String idpAuthRequestRedirectURL;

    @ConfigProperty(name = "idp.base.url")
    String idpBaseURL;

    @ConfigProperty(name = "directory-watcher.dir", defaultValue = "watch-pdf")
    String directoryWatcherDir;

    @Inject
    CertConfig certConfig;

    public String getConnectorCertAuthStoreFile() {
        return certConfig.getConnectorCertAuthStoreFile();
    }

    public String getConnectorCertAuthStoreFilePwd() {
        return certConfig.getConnectorCertAuthStoreFilePwd();
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSystemId() {
        return clientSystemId;
    }

    public String getWorkplaceId() {
        return workplaceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getMandantId() {
        return mandantId;
    }

    public void setMandantId(String mandantId) {
        this.mandantId = mandantId;
    }

    public boolean isValidateSignRequestBundles() {
        return validateSignRequestBundles;
    }

    public String getPrescriptionServerURL() {
        return prescriptionServerURL;
    }

    public String getTvMode() {
        return tvMode;
    }

    public Boolean getEnableVau() {
        return enableVau;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getIdpAuthRequestURL() {
        return idpAuthRequestURL;
    }

    public String getIdpAuthRequestRedirectURL() {
        return idpAuthRequestRedirectURL;
    }

    public String getIdpBaseURL() {
        return idpBaseURL;
    }

    public String getDirectoryWatcherDir() {
        return directoryWatcherDir;
    }
}
