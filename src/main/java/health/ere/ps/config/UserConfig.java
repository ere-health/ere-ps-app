package health.ere.ps.config;


import health.ere.ps.service.extractor.TemplateProfile;
import health.ere.ps.service.extractor.SVGExtractorConfiguration;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

@ApplicationScoped
public class UserConfig {

    private final Logger log = Logger.getLogger(UserConfig.class.getName());

    private final Properties erixaProperties;

    @ConfigProperty(name = "muster16.template.configuration", defaultValue = "DENS")
    String muster16TemplateConfiguration;

    public UserConfig() {
        erixaProperties = erixaProperties();
    }

    public String getErixaHotfolder() {
        return erixaProperties.getProperty("erixa.hotfolder");
    }

    public String getErixaReceiverEmail() {
        return erixaProperties.getProperty("erixa.receiver.email");
    }

    public SVGExtractorConfiguration getMuster16TemplateConfiguration() {
        TemplateProfile profile;
        try {
            profile = TemplateProfile.valueOf(muster16TemplateConfiguration);
        } catch (IllegalArgumentException ignored) {
            profile = TemplateProfile.DENS;
        }
        return profile.configuration;
    }

    private Properties erixaProperties() {
        final Properties properties = new Properties();
        try {
            properties.load(UserConfig.class.getResourceAsStream("/eRiXa.properties"));
        } catch (IOException e) {
            log.warning("Failed to load eRiXa.properties");
        }
        return properties;
    }
}
