package health.ere.ps.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.health.service.cetp.config.IRuntimeConfig;
import de.health.service.cetp.config.IUserConfigurations;
import de.health.service.cetp.config.UserRuntimeConfig;
import de.health.service.cetp.konnektorconfig.KCUserConfigurations;
import health.ere.ps.event.config.UserConfigurationsUpdateEvent;
import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.service.config.UserConfigurationService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class UserConfig implements UserRuntimeConfig {

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

    // todo: shouldn't user-id be part of runtime-config? (comfortSignature!)
    @ConfigProperty(name = "connector.user-id")
    Optional<String> defaultUserId;

    @ConfigProperty(name = "connector.tvMode")
    String defaultTvMode;

    @ConfigProperty(name = "connector.version")
    String defaultConnectorVersion;

    @ConfigProperty(name = "kbv.pruefnummer")
    String defaultPruefnummer;

    String defaultMuster16TemplateProfile = "DENS";

    protected UserConfigurations configurations;

    @PostConstruct
    void init() {
        updateProperties();
    }

    public UserConfig() {
    }

    @Override
    public IUserConfigurations getConfigurations() {
        return configurations == null ? new UserConfigurations() : configurations;
    }

    @JsonIgnore
    @Override
    public IRuntimeConfig getRuntimeConfig() {
        if (this instanceof RuntimeConfig runtimeConfig) {
            return runtimeConfig;
        } else {
            return null;
        }
    }

    @Override
    public UserRuntimeConfig copy() {
        RuntimeConfig runtimeConfig = new RuntimeConfig();
        runtimeConfig.copyValuesFromUserConfig(this);
        return runtimeConfig;
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

    public void updateProperties(IUserConfigurations configurations) {
        if (configurations instanceof UserConfigurations) {
            this.configurations = (UserConfigurations) configurations;
        } else if (configurations instanceof KCUserConfigurations kcUserConfigurations) {
            this.configurations = new UserConfigurations(kcUserConfigurations.properties());
        }
    }

    private void updateProperties() {
        updateProperties(configurationManagementService.getConfig());
    }

    private String getConfigOrDefault(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof UserConfig userConfig) {
            return Objects.equals(defaultConnectorBaseURI, userConfig.defaultConnectorBaseURI)
                && Objects.equals(defaultMandantId, userConfig.defaultMandantId)
                && Objects.equals(defaultWorkplaceId, userConfig.defaultWorkplaceId)
                && Objects.equals(defaultClientSystemId, userConfig.defaultClientSystemId)
                && Objects.equals(defaultUserId, userConfig.defaultUserId)
                && Objects.equals(defaultTvMode, userConfig.defaultTvMode)
                && Objects.equals(defaultConnectorVersion, userConfig.defaultConnectorVersion)
                && Objects.equals(defaultPruefnummer, userConfig.defaultPruefnummer)
                && Objects.equals(defaultMuster16TemplateProfile, userConfig.defaultMuster16TemplateProfile)
                && Objects.equals(configurations, userConfig.configurations);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            defaultConnectorBaseURI, defaultMandantId, defaultWorkplaceId, defaultClientSystemId, defaultUserId,
            defaultTvMode, defaultConnectorVersion, defaultPruefnummer, defaultMuster16TemplateProfile, configurations
        );
    }

    @Override
    public String toString() {
        return "UserConfig{" +
               "defaultConnectorBaseURI='" + defaultConnectorBaseURI + '\'' +
               ", defaultMandantId='" + defaultMandantId + '\'' +
               ", defaultWorkplaceId='" + defaultWorkplaceId + '\'' +
               ", defaultClientSystemId='" + defaultClientSystemId + '\'' +
               ", defaultUserId=" + defaultUserId +
               ", defaultTvMode='" + defaultTvMode + '\'' +
               ", defaultConnectorVersion='" + defaultConnectorVersion + '\'' +
               ", defaultPruefnummer='" + defaultPruefnummer + '\'' +
               ", defaultMuster16TemplateProfile='" + defaultMuster16TemplateProfile + '\'' +
               ", configurations=" + configurations +
               '}';
    }
}