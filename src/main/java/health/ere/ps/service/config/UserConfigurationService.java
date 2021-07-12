package health.ere.ps.service.config;


import health.ere.ps.event.UserConfigurationsUpdateEvent;
import health.ere.ps.model.config.UserConfigurations;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

@ApplicationScoped
public class UserConfigurationService {


    final Logger log = Logger.getLogger(getClass().getName());

    @Inject
    Event<UserConfigurationsUpdateEvent> dynamicConfigurationsUpdateEvent;

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
        properties.setProperty("erixa.hotfolder", config.getErixaHotfolder());
        properties.setProperty("erixa.drugstore.email", config.getErixaDrugstoreEmail());
        properties.setProperty("erixa.user.email", config.getErixaUserEmail());
        properties.setProperty("erixa.user.password", config.getErixaUserPassword());
        properties.setProperty("connector.certificate.file", config.getConnectorCertificateFile());
        properties.setProperty("connector.certificate.password", config.getConnectorCertificatePassword());
        properties.setProperty("extractor.template.profile", config.getMuster16Profile());
        updateConfig(properties);
        dynamicConfigurationsUpdateEvent.fireAsync(new UserConfigurationsUpdateEvent(config));
    }

    public UserConfigurations getConfig() {
        Properties properties = getProperties();
        UserConfigurations config = new UserConfigurations();
        config.setErixaHotfolder(properties.getProperty("erixa.hotfolder"));
        config.setErixaDrugstoreEmail(properties.getProperty("erixa.drugstore.email"));
        config.setErixaUserEmail(properties.getProperty("erixa.user.email"));
        config.setErixaUserPassword(properties.getProperty("erixa.user.password"));
        config.setConnectorCertificateFile(properties.getProperty("connector.certificate.file"));
        config.setConnectorCertificatePassword(properties.getProperty("connector.certificate.password"));
        config.setMuster16Profile(properties.getProperty("extractor.template.profile"));
        return config;
    }
}