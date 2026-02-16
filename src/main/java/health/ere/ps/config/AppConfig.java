package health.ere.ps.config;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

@Getter
@ApplicationScoped
public class AppConfig {

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

    @ConfigProperty(name = "ere-workflow-service.retry.enable", defaultValue = "true")
    boolean retry;

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

    @ConfigProperty(name = "connector.base.url")
    String connectorBaseURL;

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
}