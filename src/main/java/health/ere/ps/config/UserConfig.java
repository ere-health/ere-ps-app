package health.ere.ps.config;


import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

@ApplicationScoped
public class UserConfig {

    private final Logger log = Logger.getLogger(UserConfig.class.getName());

    private final Properties erixaProperties;

    public UserConfig() {
        erixaProperties = erixaProperties();
    }

    public String getErixaHotfolder() {
        return erixaProperties.getProperty("erixa.hotfolder");
    }

    public String getErixaReceiverEmail() {
        return erixaProperties.getProperty("erixa.receiver.email");
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
