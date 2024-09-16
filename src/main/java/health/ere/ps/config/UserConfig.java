package health.ere.ps.config;


import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;

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

    //todo: shouldn't user-id be part of runtime-config? (comfortSignature!)
    @ConfigProperty(name = "connector.user-id")
    Optional<String> defaultUserId;

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
        return configurations == null ? new UserConfigurations() : configurations;
    }

    public String getErixaHotfolder() {
        return getConfigurations().getErixaHotfolder();
    }

    public String getErixaReceiverEmail() {
        return getConfigurations().getErixaDrugstoreEmail();
    }

    public String getErixaUserEmail() {
        return getConfigurations().getErixaUserEmail();
    }

    public String getErixaUserPassword() {
        return getConfigurations().getErixaUserPassword();
    }

    public String getConnectorBaseURL() {
        return getConfigOrDefault(getConfigurations().getConnectorBaseURL(), defaultConnectorBaseURI);
    }

    public String getMandantId() {
        return getConfigOrDefault(getConfigurations().getMandantId(), defaultMandantId);
    }

    public String getWorkplaceId() {
        return getConfigOrDefault(getConfigurations().getWorkplaceId(), defaultWorkplaceId);
    }

    public String getClientSystemId() {
        return getConfigOrDefault(getConfigurations().getClientSystemId(), defaultClientSystemId);
    }

    public String getUserId() {
        return getConfigOrDefault(getConfigurations().getUserId(), defaultUserId == null ? null : defaultUserId.orElse(null));
    }

    public String getTvMode() {
        return getConfigOrDefault(getConfigurations().getTvMode(), defaultTvMode);
    }
    
    public String getConnectorVersion() {
        return getConfigOrDefault(getConfigurations().getVersion(), defaultConnectorVersion);
    }

    public String getPruefnummer() {
        return getConfigOrDefault(getConfigurations().getPruefnummer(), defaultPruefnummer);
    }

    public String getErixaApiKey() {
        return getConfigurations().getErixaApiKey();
    }

    public String getMuster16TemplateConfiguration() {
        return getConfigOrDefault(getConfigurations().getMuster16TemplateProfile(), defaultMuster16TemplateProfile);
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