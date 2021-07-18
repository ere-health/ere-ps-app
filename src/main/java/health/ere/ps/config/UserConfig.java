package health.ere.ps.config;


import static health.ere.ps.config.UserConfigKey.CONNECTOR_BASE_URL;
import static health.ere.ps.config.UserConfigKey.CONNECTOR_CLIENT_SYSTEM_ID;
import static health.ere.ps.config.UserConfigKey.CONNECTOR_MANDANT_ID;
import static health.ere.ps.config.UserConfigKey.CONNECTOR_TV_MODE;
import static health.ere.ps.config.UserConfigKey.CONNECTOR_USER_ID;
import static health.ere.ps.config.UserConfigKey.CONNECTOR_WORKPLACE_ID;
import static health.ere.ps.config.UserConfigKey.EXTRACTOR_TEMPLATE_PROFILE;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import health.ere.ps.event.config.UserConfigurationsUpdateEvent;
import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.service.config.UserConfigurationService;
import health.ere.ps.service.logging.EreLogger;

@ApplicationScoped
public class UserConfig {

    private final EreLogger log = EreLogger.getLogger(UserConfig.class);

    @Inject
    UserConfigurationService configurationManagementService;

    @ConfigProperty(name = "connector.base-uri")
    String defaultConnectorBaseURI;

    @ConfigProperty(name = "connector.mandant-id")
    String defaultMandantId;

    @ConfigProperty(name = "connector.workplace-id")
    String defaultWorkplaceId;

    @ConfigProperty(name = "connector.client-system-id")
    String defaultClientSystemId;

    @ConfigProperty(name = "connector.user-id")
    String defaultUserId;

    @ConfigProperty(name = "connector.tvMode")
    String defaultTvMode;

    String defaultMuster16TemplateProfile = "DENS";

    private UserConfigurations configurations;

    @PostConstruct
    void init() {
        updateProperties();
    }

    public UserConfig() {
        configurationManagementService = new UserConfigurationService();
    }

    public UserConfigurations getConfigurations() {
        return configurations;
    }

    public String getErixaHotfolder() {
        return configurations.getErixaHotfolder();
    }

    public String getErixaReceiverEmail() {
        return configurations.getErixaDrugstoreEmail();
    }

    public String getErixaUserEmail() {
        return configurations.getErixaUserEmail();
    }

    public String getErixaUserPassword() {
        return configurations.getErixaUserPassword();
    }

    public String getConnectorBaseURL() {
        return getConfigOrDefault(configurations.getConnectorBaseURL(), defaultConnectorBaseURI, CONNECTOR_BASE_URL);
    }

    public String getMandantId() {
        return getConfigOrDefault(configurations.getMandantId(), defaultMandantId, CONNECTOR_MANDANT_ID);
    }

    public String getWorkplaceId() {
        return getConfigOrDefault(configurations.getWorkplaceId(), defaultWorkplaceId, CONNECTOR_WORKPLACE_ID);
    }

    public String getClientSystemId() {
        return getConfigOrDefault(configurations.getClientSystemId(), defaultClientSystemId, CONNECTOR_CLIENT_SYSTEM_ID);
    }

    public String getUserId() {
        return getConfigOrDefault(configurations.getUserId(), defaultUserId, CONNECTOR_USER_ID);
    }

    public String getTvMode() {
        return getConfigOrDefault(configurations.getTvMode(), defaultTvMode, CONNECTOR_TV_MODE);
    }

    public String getErixaApiKey() {
        // FIXME Erixa ApiKey does not belong here
        throw new UnsupportedOperationException();
    }

    public String getMuster16TemplateConfiguration() {
        return getConfigOrDefault(configurations.getMuster16TemplateProfile(), defaultMuster16TemplateProfile, EXTRACTOR_TEMPLATE_PROFILE);
    }

    public void handleUpdateProperties(@ObservesAsync UserConfigurationsUpdateEvent event) {
        updateProperties(event.getConfigurations());
    }

    private void updateProperties(UserConfigurations configurations) {
        this.configurations = configurations;
    }

    private void updateProperties() {
        updateProperties(configurationManagementService.getConfig());
    }

    private String getConfigOrDefault(String value, String defaultValue, UserConfigKey key) {
        if (value != null)
            return value;
        else if (defaultValue != null)
            return defaultValue;
        else {
            logMissingConfig(key);
            return null;
        }
    }

    private void logMissingConfig(UserConfigKey key) {
        log.info(String.format("Missing config value with key=%s.", key.key));
    }
}