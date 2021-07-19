package health.ere.ps.config;


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
        return getConfigOrDefault(configurations.getConnectorBaseURL(), defaultConnectorBaseURI);
    }

    public String getMandantId() {
        return getConfigOrDefault(configurations.getMandantId(), defaultMandantId);
    }

    public String getWorkplaceId() {
        return getConfigOrDefault(configurations.getWorkplaceId(), defaultWorkplaceId);
    }

    public String getClientSystemId() {
        return getConfigOrDefault(configurations.getClientSystemId(), defaultClientSystemId);
    }

    public String getUserId() {
        return getConfigOrDefault(configurations.getUserId(), defaultUserId);
    }

    public String getTvMode() {
        return getConfigOrDefault(configurations.getTvMode(), defaultTvMode);
    }

    public String getErixaApiKey() {
        // FIXME Erixa ApiKey does not belong here
        throw new UnsupportedOperationException();
    }

    public String getMuster16TemplateConfiguration() {
        return getConfigOrDefault(configurations.getMuster16TemplateProfile(), defaultMuster16TemplateProfile);
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

    private String getConfigOrDefault(String value, String defaultValue) {
        if (value != null)
            return value;
        else if (defaultValue != null)
            return defaultValue;
        else {
            return null;
        }
    }
}