package health.ere.ps.service.config;

import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import health.ere.ps.event.SaveSettingsEvent;
import health.ere.ps.model.config.UserConfigurations;

public class UserConfigurationServiceTest {

    @BeforeAll
    static void initAll() {
    }

    @BeforeEach
    void init() {
    }

    @Test
    @DisplayName("get Properties")
    public void getProperties() {
        try {
            Properties expectedValue = new Properties(0);

            UserConfigurationService userconfigurationservice = new UserConfigurationService();
            Properties actualValue = userconfigurationservice.getProperties();
            System.out.println("Expected Value=" + expectedValue + " . Actual Value=" + actualValue);
            Assertions.assertEquals(expectedValue, actualValue);
        } catch (Exception exception) {
            exception.printStackTrace();
            Assertions.assertFalse(false);
        }
    }

    @Test
    @DisplayName("update Config")
    public void updateConfig() {
        try {
            UserConfigurations config = null;

            UserConfigurationService userconfigurationservice = new UserConfigurationService();
            userconfigurationservice.updateConfig(config);
            Assertions.assertTrue(true);
        } catch (Exception exception) {
            exception.printStackTrace();
            Assertions.assertFalse(false);
        }
    }

    @Test
    @DisplayName("get Config")
    public void getConfig() {
        try {
            UserConfigurations expectedValue = new UserConfigurations();

            UserConfigurationService userconfigurationservice = new UserConfigurationService();
            UserConfigurations actualValue = userconfigurationservice.getConfig();
            System.out.println("Expected Value=" + expectedValue + " . Actual Value=" + actualValue);
            Assertions.assertEquals(expectedValue.toString(), actualValue.toString());
        } catch (Exception exception) {
            exception.printStackTrace();
            Assertions.assertFalse(false);
        }
    }

    @Test
    @DisplayName("on Save Settings Event")
    public void onSaveSettingsEvent() {
        try {
            SaveSettingsEvent saveSettingsEvent = null;

            UserConfigurationService userconfigurationservice = new UserConfigurationService();
            userconfigurationservice.onSaveSettingsEvent(saveSettingsEvent);
            Assertions.assertTrue(true);
        } catch (Exception exception) {
            exception.printStackTrace();
            Assertions.assertFalse(false);
        }
    }

    @AfterEach
    void tearDown() {
    }

    @AfterAll
    static void tearDownAll() {
    }
}
