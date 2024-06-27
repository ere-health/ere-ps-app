package health.ere.ps.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class AppConfig {

    private static final Logger log = Logger.getLogger(AppConfig.class.getName());

    @ConfigProperty(name = "ere.workflow-service.prescription.server.url")
    String prescriptionServiceURL;

    @ConfigProperty(name = "connector.verify-hostname")
    String verifyHostname;

    @ConfigProperty(name = "idp.client.id")
    String idpClientId;

    @ConfigProperty(name = "idp.auth.request.redirect.url")
    String idpAuthRequestRedirectURL;

    @ConfigProperty(name = "idp.initialization.retries.seconds")
    Optional<String> idpInitializationRetriesSeconds;

    @ConfigProperty(name = "idp.initialization.period.seconds")
    Optional<Integer> idpInitializationPeriodSeconds;

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

    @ConfigProperty(name = "connector.base.url")
    String connectorBaseURL;

    public Optional<String> getCardLinkServer() {
        return cardLinkServer;
    }

    public void setCardLinkServer(Optional<String> cardLinkServer) {
        this.cardLinkServer = cardLinkServer;
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

    public List<Integer> getIdpInitializationRetriesSeconds() {
        String seconds = idpInitializationRetriesSeconds.orElse("5,10,50");
        return Arrays.stream(seconds.split(",")).map(String::trim).map(s -> {
            try {
                return Integer.parseInt(s);
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).toList();
    }

    public int getIdpInitializationPeriodMs() {
        return idpInitializationPeriodSeconds.orElse(180) * 1000;
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

    public String getConnectorBaseURL() {
        return connectorBaseURL;
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

    public URI getCardLinkURI() {
        try {
            String cardLinkServer = getCardLinkServer().orElse(
                "wss://cardlink.service-health.de:8444/websocket/80276003650110006580-20230112"
            );
            log.info("Starting websocket connection to: " + cardLinkServer);
            return new URI(cardLinkServer);
        } catch (URISyntaxException e) {
            log.log(Level.WARNING, "Could not connect to card link", e);
            return null;
        }
    }
}
