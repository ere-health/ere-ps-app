package health.ere.ps.config;

import de.health.service.cetp.CETPServer;
import de.health.service.config.api.ISubscriptionConfig;
import health.ere.ps.service.idp.client.IdpHttpClientService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
@ApplicationScoped
public class AppConfig implements ISubscriptionConfig {

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

    @ConfigProperty(name = "zeta.enabled", defaultValue = "false")
    boolean zetaEnabled;

    @ConfigProperty(name = "zeta.auth.server.url", defaultValue = "https://zeta-cd.westeurope.cloudapp.azure.com")
    String zetaAuthServerUrl;

    @ConfigProperty(name = "popp.server.url", defaultValue = "https://popp-server.com")
    String poppServerUrl;

    @ConfigProperty(name = "zeta.product.id", defaultValue = "ere-health-client")
    String zetaProductId;

    @ConfigProperty(name = "zeta.product.version", defaultValue = "0.0.1")
    String zetaProductVersion;

    @ConfigProperty(name = "zeta.client.name", defaultValue = "zetaClientName")
    String zetaClientName;

    @ConfigProperty(name = "zeta.assessment.name", defaultValue = "assessment.name")
    String zetaAssessmentName;

    @ConfigProperty(name = "zeta.assessment.client.id", defaultValue = "assessment.client.id")
    String zetaAssessmentClientId;

    @ConfigProperty(name = "zeta.assessment.manufacturer.id", defaultValue = "assessment.manufacturer.id")
    String zetaAssessmentManufacturerId;

    @ConfigProperty(name = "zeta.assessment.manufacturer.name", defaultValue = "assessment.manufacturer.name")
    String zetaAssessmentManufacturerName;

    @ConfigProperty(name = "zeta.assessment.owner.mail", defaultValue = "owner@mail.de")
    String zetaAssessmentOwnerMail;

    public String getDiscoveryDocumentUrl() {
        return getIdpBaseURL() + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;
    }

    public int getIdpInitializationPeriodMs() {
        return idpInitializationPeriodSeconds.orElse(180) * 1000;
    }

    @Override
    public int getCetpSubscriptionsRenewalSafePeriodMs() {
        return cetpSubscriptionsRenewalSafePeriodSeconds.orElse(600) * 1000;
    }

    @Override
    public int getCetpSubscriptionsMaintenanceRetryIntervalMs() {
        return subscriptionsMaintenanceRetryIntervalMs.orElse(5000);
    }

    @Override
    public int getForceResubscribePeriodSeconds() {
        return forceResubscribePeriodSeconds.orElse(43200);
    }

    @Override
    public String getDefaultCardLinkServer() {
        return cardLinkServer.orElse("wss://cardlink.service-health.de:8444/websocket/80276003650110006580-20230112");
    }

    @Override
    public String getDefaultEventToHost() {
        return eventToHost.orElseThrow();
    }

    @Override
    public int getDefaultCetpServerPort() {
        return cetpPort.orElse(CETPServer.DEFAULT_PORT);
    }

    public int getTrackBatch() {
        return trackBatch.orElse(100);
    }

    public String getBillingCsvFolder() {
        return csvFolder.orElse("billing");
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
}