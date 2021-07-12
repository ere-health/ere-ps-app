package health.ere.ps.config;


import health.ere.ps.event.UserConfigurationsUpdateEvent;
import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.service.config.UserConfigurationService;
import health.ere.ps.service.extractor.SVGExtractorConfiguration;
import health.ere.ps.service.extractor.TemplateProfile;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import java.util.logging.Logger;

@ApplicationScoped
public class UserConfig {

    // TODO
    //  Send warning messages to the user when configurations are incomplete
    //  This should be done on startup, as well as when retrieving configurations
    private final Logger log = Logger.getLogger(UserConfig.class.getName());

    @Inject
    UserConfigurationService configurationManagementService;

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

    public String getErixaApiKey() {
        // FIXME Erixa ApiKey does not belong here
        throw new UnsupportedOperationException();
    }

    public String getCertificateFilePath() {
        return configurations.getConnectorCertificateFile();
    }

    public String getCertificateFilePassword() {
        return configurations.getConnectorCertificatePassword();
    }

    public SVGExtractorConfiguration getMuster16TemplateConfiguration() {
        TemplateProfile profile;
        String s = configurations.getMuster16Profile();
        try {
            profile = TemplateProfile.valueOf(s);
        } catch (IllegalArgumentException ignored) {
            profile = TemplateProfile.DENS;
        }
        return profile.configuration;
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
}