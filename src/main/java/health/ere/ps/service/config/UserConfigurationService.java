package health.ere.ps.service.config;


import health.ere.ps.event.config.UserConfigurationsUpdateEvent;
import health.ere.ps.model.config.UserConfigurations;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

import static health.ere.ps.config.UserConfigKey.*;

@ApplicationScoped
public class UserConfigurationService {


    final Logger log = Logger.getLogger(getClass().getName());

    @Inject
    Event<UserConfigurationsUpdateEvent> configurationsUpdateEvent;

    private String getConfigFilePath() {
        // TODO configure proper file path
        return "user.properties";
    }

    public Properties getProperties() {
        File file = new File(getConfigFilePath());
        if (!file.exists())
            createConfigurationFile(file);
        if (file.exists())
            return readFile(file);
        else
            return null;
    }

    private void createConfigurationFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            log.severe("Could not create missing configuration file");
        }
    }

    private Properties readFile(File file) {
        final Properties properties = new Properties();
        try {
            properties.load(new FileReader(file));
        } catch (IOException e) {
            log.warning("Failed to load user-config file");
        }
        return properties;
    }

    private void writeFile(File file, Properties properties) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            properties.store(outputStream, "");
        } catch (IOException e) {
            log.severe("Could not store configurations");
        }
    }

    private void updateConfig(Properties updated) {
        File file = new File(getConfigFilePath());
        if (!file.exists())
            createConfigurationFile(file);
        Properties properties = readFile(file);
        properties.putAll(updated);
        writeFile(file, properties);
    }

    public void updateConfig(UserConfigurations config) {
        Properties properties = new Properties();
        // TODO implement a cleaner mapping
        properties.setProperty(ERIXA_HOTFOLDER.key, config.getErixaHotfolder());
        properties.setProperty(ERIXA_DRUGSTORE_EMAIL_ADDRESS.key, config.getErixaDrugstoreEmail());
        properties.setProperty(ERIXA_USER_EMAIL.key, config.getErixaUserEmail());
        properties.setProperty(ERIXA_USER_PASSWORD.key, config.getErixaUserPassword());
        properties.setProperty(EXTRACTOR_TEMPLATE_PROFILE.key, config.getMuster16TemplateProfile());
        properties.setProperty(CONNECTOR_BASE_URL.key, config.getConnectorBaseURL());
        properties.setProperty(CONNECTOR_USER_ID.key, config.getUserId());
        properties.setProperty(CONNECTOR_MANDANT_ID.key, config.getMandantId());
        properties.setProperty(CONNECTOR_WORKPLACE_ID.key, config.getWorkplaceId());
        properties.setProperty(CONNECTOR_CLIENT_SYSTEM_ID.key, config.getClientSystemId());
        properties.setProperty(CONNECTOR_TV_MODE.key, config.getTvMode());
        updateConfig(properties);
        configurationsUpdateEvent.fireAsync(new UserConfigurationsUpdateEvent(config));
    }

    public UserConfigurations getConfig() {
        Properties properties = getProperties();
        UserConfigurations config = new UserConfigurations();
        config.setErixaHotfolder(properties.getProperty(ERIXA_HOTFOLDER.key));
        config.setErixaDrugstoreEmail(properties.getProperty(ERIXA_DRUGSTORE_EMAIL_ADDRESS.key));
        config.setErixaUserEmail(properties.getProperty(ERIXA_USER_EMAIL.key));
        config.setErixaUserPassword(properties.getProperty(ERIXA_USER_PASSWORD.key));
        config.setMuster16TemplateProfile(properties.getProperty(EXTRACTOR_TEMPLATE_PROFILE.key));
        config.setConnectorBaseURL(properties.getProperty(CONNECTOR_BASE_URL.key));
        config.setUserId(properties.getProperty(CONNECTOR_USER_ID.key));
        config.setMandantId(properties.getProperty(CONNECTOR_MANDANT_ID.key));
        config.setWorkplaceId(properties.getProperty(CONNECTOR_WORKPLACE_ID.key));
        config.setClientSystemId(properties.getProperty(CONNECTOR_CLIENT_SYSTEM_ID.key));
        config.setTvMode(properties.getProperty(CONNECTOR_TV_MODE.key));
        return config;
    }
}