package health.ere.ps.config;


import health.ere.ps.service.config.UserConfigurationService;
import health.ere.ps.service.extractor.TemplateProfile;
import health.ere.ps.service.extractor.SVGExtractorConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Properties;
import java.util.logging.Logger;

@ApplicationScoped
public class UserConfig {

    private final Logger log = Logger.getLogger(UserConfig.class.getName());

    @Inject
    UserConfigurationService configurationManagementService;

    private Properties properties;

    public UserConfig() {
        configurationManagementService = new UserConfigurationService();
        properties = configurationManagementService.getProperties();
    }

    public String getErixaHotfolder() {
        return properties.getProperty("erixa.hotfolder");
    }

    public String getErixaReceiverEmail() {
        return properties.getProperty("erixa.receiver.email");
    }

    public String getErixaUserEmail() {
        return properties.getProperty("erixa.user.email");
    }
    public String getErixaUserPassword() {
        return properties.getProperty("erixa.user.password");
    }

    public String getErixaApiKey(){
        return properties.getProperty("erixa.api.key");
    }

    public String getCertificateFilePath() {
        return properties.getProperty("certificate.file.path");
    }

    public String getCertificateFilePassword() {
        return properties.getProperty("certificate.file.password");
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
        configurationManagementService.update(properties);
    }

    public SVGExtractorConfiguration getMuster16TemplateConfiguration() {
        TemplateProfile profile;
        String s = properties.getProperty("muster16.template.configuration", "DENS");
        try {
            profile = TemplateProfile.valueOf(s);
        } catch (IllegalArgumentException ignored) {
            profile = TemplateProfile.DENS;
        }
        return profile.configuration;
    }
}