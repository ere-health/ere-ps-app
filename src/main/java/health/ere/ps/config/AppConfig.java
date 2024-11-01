package health.ere.ps.config;

import de.health.service.cetp.CETPServer;
import de.health.service.config.api.ISubscriptionConfig;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
public class AppConfig implements ISubscriptionConfig {

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

    @ConfigProperty(name = "cetp.subscriptions.renewal.safe.period.seconds")
    Optional<Integer> cetpSubscriptionsRenewalSafePeriodSeconds;

    @ConfigProperty(name = "cetp.subscriptions.maintenance.retry.interval.ms")
    Optional<Integer> subscriptionsMaintenanceRetryIntervalMs;

    @ConfigProperty(name = "cetp.subscriptions.force.resubscribe.period.seconds")
    Optional<Integer> forceResubscribePeriodSeconds;

    @ConfigProperty(name = "cetp.subscriptions.event-to-host")
    Optional<String> eventToHost;

    @ConfigProperty(name = "cetp.track.batch.size")
    Optional<Integer> trackBatch;

    @ConfigProperty(name = "ere.billing.csv.folder")
    Optional<String> csvFolder;

    @ConfigProperty(name = "idp.auth.request.url")
    String idpAuthRequestURL;

    @ConfigProperty(name = "idp.base.url")
    String idpBaseURL;

    @ConfigProperty(name = "ere-workflow-service.vau.enable")
    boolean enableVau;

    @ConfigProperty(name = "ere-workflow-service.batch-sign.enable")
    boolean enableBatchSign;

    @ConfigProperty(name = "trigger.smcb.pin.verification", defaultValue = "true")
    boolean triggerSmcbPinVerification;

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

    @ConfigProperty(name = "connector.host")
    String konnectorHost;

    @ConfigProperty(name = "connector.cetp.port")
    Optional<Integer> cetpPort;

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

    public List<Integer> getIdpInitializationRetriesMillis() {
        String seconds = idpInitializationRetriesSeconds.orElse("5,10,50");
        return Arrays.stream(seconds.split(",")).map(String::trim).map(s -> {
            try {
                return Integer.parseInt(s) * 1000;
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).toList();
    }

    public int getIdpInitializationPeriodMs() {
        return idpInitializationPeriodSeconds.orElse(180) * 1000;
    }

    public int getCetpSubscriptionsRenewalSafePeriodMs() {
        return cetpSubscriptionsRenewalSafePeriodSeconds.orElse(600) * 1000;
    }

    public int getCetpSubscriptionsMaintenanceRetryIntervalMs() {
        return subscriptionsMaintenanceRetryIntervalMs.orElse(5000);
    }

    public int getForceResubscribePeriodSeconds() {
        return forceResubscribePeriodSeconds.orElse(43200);
    }

    public int getTrackBatch() {
        return trackBatch.orElse(100);
    }

    public String getBillingCsvFolder() {
        return csvFolder.orElse("billing");
    }

    @Override
    public String getDefaultEventToHost() {
        return eventToHost.orElseThrow();
    }

    @Override
    public int getDefaultCetpServerPort() {
        return cetpPort.orElse(CETPServer.DEFAULT_PORT);
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

    public boolean triggerSmcbPinVerification() {
        return triggerSmcbPinVerification;
    }

    public boolean includeRevocationInfoEnabled() {
        return includeRevocationInfo;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getKonnectorHost() {
        return konnectorHost;
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

    public String getDefaultCardLinkServer() {
        return cardLinkServer.orElse(
            "wss://cardlink.service-health.de:8444/websocket/80276003650110006580-20230112"
        );
    }
}
