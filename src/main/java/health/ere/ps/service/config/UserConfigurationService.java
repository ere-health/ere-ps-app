package health.ere.ps.service.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import health.ere.ps.event.SaveSettingsEvent;
import health.ere.ps.event.config.UserConfigurationsUpdateEvent;
import health.ere.ps.model.config.UserConfigurations;
import io.vertx.core.json.Json;

@ApplicationScoped
public class UserConfigurationService {

    final Logger log = Logger.getLogger(getClass().getName());

    @Inject
    Event<UserConfigurationsUpdateEvent> configurationsUpdateEvent;

    private String getConfigFilePath() {
        // TODO configure proper file path
        return "src/test/resources/user.properties";
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
            outputStream.close();
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
        updateConfig(config.properties());
        configurationsUpdateEvent.fireAsync(new UserConfigurationsUpdateEvent(config));
    }

    public UserConfigurations getConfig() {
        Properties properties = getProperties();

        System.out.println("'properties'");

        System.out.println(Json.encode(properties));

        UserConfigurations config = new UserConfigurations(properties);
        return config;
    }

    public void onSaveSettingsEvent(@ObservesAsync SaveSettingsEvent saveSettingsEvent) {
        updateConfig(saveSettingsEvent.getUserConfigurations());
    }
}