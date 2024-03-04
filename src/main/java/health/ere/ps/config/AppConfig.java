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

    @ConfigProperty(name = "ere-workflow-service.batch-sign.enable")
    boolean enableBatchSign;

    @ConfigProperty(name = "ere-workflow-service.includeRevocationInfo.enable", defaultValue = "true")
    boolean includeRevocationInfo;

    @ConfigProperty(name = "ere.workflow-service.prescription.write-signature-file")
    boolean writeSignatureFile;

    @ConfigProperty(name = "ere-workflow-service.user-agent")
    String userAgent;

    @ConfigProperty(name = "connector.crypt")
    String connectorCrypt;

    @ConfigProperty(name = "ere.websocket.xml-bundle.direct-process")
    boolean xmlBundleDirectProcess;

    @ConfigProperty(name = "connector.cert.auth.store.file")
    Optional<String> certAuthStoreFile;

    @ConfigProperty(name = "connector.cert.auth.store.file.password")
    Optional<String> certAuthStoreFilePassword;

    @ConfigProperty(name = "cardlink.server.url")
    Optional<String> cardLinkServer;

    public Optional<String> getCardLinkServer() {
        return cardLinkServer;
    }

    public void setCardLinkServer(Optional<String> cardLinkServer) {
        this.cardLinkServer = cardLinkServer;
    }

    public String getDirectoryWatcherDir() {
        return directoryWatcherDir;
    }

    public String getPrescriptionServiceURL() {
        return prescriptionServiceURL;
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

    public boolean enableBatchSign() {
        return enableBatchSign;
    }

    public boolean includeRevocationInfoEnabled() {
        return includeRevocationInfo;
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

    public boolean isWriteSignatureFile() {
        return this.writeSignatureFile;
    }

    public boolean getWriteSignatureFile() {
        return this.writeSignatureFile;
    }
    
    public boolean getXmlBundleDirectProcess() {
        return this.xmlBundleDirectProcess;
    }

}
