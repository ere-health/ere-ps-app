package health.ere.ps.service.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import javax.enterprise.event.Event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import health.ere.ps.event.SaveSettingsEvent;
import health.ere.ps.event.config.UserConfigurationsUpdateEvent;
import health.ere.ps.model.config.UserConfigurations;

public class UserConfigurationServiceTest {

    private UserConfigurationService userConfigurationService;

    @TempDir
    File tempDir;

    @BeforeEach
    public void setup() {
        userConfigurationService = new UserConfigurationService() {
            @Override
            protected String getConfigFilePath() {
                return tempDir.getAbsolutePath() + "/user.properties";
            }
        };
    }

    @Test
    public void testGetPropertiesFileDoesNotExist() {
        assertNull(userConfigurationService.getProperties());
    }

    @Test
    public void testCreateAndReadConfigurationFile() throws IOException {
        userConfigurationService.createConfigurationFile(new File(userConfigurationService.getConfigFilePath()));

        Properties properties = userConfigurationService.getProperties();
        assertNotNull(properties);
        assertTrue(properties.isEmpty());
    }

    @Test
    public void testReadFile() throws IOException {
        Properties props = new Properties();
        props.setProperty("key1", "value1");
        props.setProperty("key2", "value2");

        try (FileOutputStream fos = new FileOutputStream(userConfigurationService.getConfigFilePath())) {
            props.store(fos, "");
        }

        Properties readProps = userConfigurationService.getProperties();

        assertEquals("value1", readProps.getProperty("key1"));
        assertEquals("value2", readProps.getProperty("key2"));
    }

    @Test
    public void testUpdateConfig() {
        UserConfigurations userConfigurations = new UserConfigurations();
        userConfigurations.put("key", "value");

        userConfigurationService.updateConfig(userConfigurations);

        Properties properties = userConfigurationService.getProperties();
        assertEquals("value", properties.getProperty("key"));
    }

    @Test
    public void testOnSaveSettingsEvent() {
        UserConfigurations userConfigurations = new UserConfigurations();
        userConfigurations.put("key", "value");

        SaveSettingsEvent saveSettingsEvent = new SaveSettingsEvent(userConfigurations);
        userConfigurationService.onSaveSettingsEvent(saveSettingsEvent);

        Properties properties = userConfigurationService.getProperties();
        assertEquals("value", properties.getProperty("key"));
    }
}

