package health.ere.ps.config;


import java.util.Objects;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import health.ere.ps.event.config.UserConfigurationsUpdateEvent;
import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.service.config.UserConfigurationService;

@ApplicationScoped
public class UserConfig {

    private final static Logger log = Logger.getLogger(UserConfig.class.getName());

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

    @ConfigProperty(name = "connector.version")
    String defaultConnectorVersion;

    @ConfigProperty(name = "kbv.pruefnummer")
    String defaultPruefnummer;

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
    
    public String getConnectorVersion() {
        return getConfigOrDefault(configurations.getVersion(), defaultConnectorVersion);
    }

    public String getPruefnummer() {
        return getConfigOrDefault(configurations.getPruefnummer(), defaultPruefnummer);
    }

    public String getErixaApiKey() {
        return configurations.getErixaApiKey();
    }

    public String getMuster16TemplateConfiguration() {
        return getConfigOrDefault(configurations.getMuster16TemplateProfile(), defaultMuster16TemplateProfile);
    }

    public void handleUpdateProperties(@ObservesAsync UserConfigurationsUpdateEvent event) {
        updateProperties(event.getConfigurations());
    }

    public void updateProperties(UserConfigurations configurations) {
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

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof UserConfig)) {
            return false;
        }
        UserConfig userConfig = (UserConfig) o;
        return Objects.equals(defaultConnectorBaseURI, userConfig.defaultConnectorBaseURI) && Objects.equals(defaultMandantId, userConfig.defaultMandantId) && Objects.equals(defaultWorkplaceId, userConfig.defaultWorkplaceId) && Objects.equals(defaultClientSystemId, userConfig.defaultClientSystemId) && Objects.equals(defaultUserId, userConfig.defaultUserId) && Objects.equals(defaultTvMode, userConfig.defaultTvMode) && Objects.equals(defaultConnectorVersion, userConfig.defaultConnectorVersion) && Objects.equals(defaultPruefnummer, userConfig.defaultPruefnummer) && Objects.equals(defaultMuster16TemplateProfile, userConfig.defaultMuster16TemplateProfile) && Objects.equals(configurations, userConfig.configurations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultConnectorBaseURI, defaultMandantId, defaultWorkplaceId, defaultClientSystemId, defaultUserId, defaultTvMode, defaultConnectorVersion, defaultPruefnummer, defaultMuster16TemplateProfile, configurations);
    }
}