package health.ere.ps.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.service.config.UserConfigurationService;

public class UserConfigTest {

    private UserConfig userConfig;

    @BeforeEach
    public void setup() {
        UserConfigurationService mockConfigService = mock(UserConfigurationService.class);
        UserConfigurations sampleConfig = createSampleConfig();
        // Mock UserConfigurationService
        when(mockConfigService.getConfig()).thenReturn(sampleConfig);

        userConfig = new UserConfig();
        userConfig.configurationManagementService = mockConfigService;
        userConfig.init();
    }

    @Test
    public void testGetConfigurations() {
        UserConfigurations configurations = userConfig.getConfigurations();
        assertEquals("https://example.com", configurations.getConnectorBaseURL());
        assertEquals("123456", configurations.getMandantId());
        // Add more assertions for other configuration properties
    }

    @Test
    public void testGetConnectorBaseURL() {
        assertEquals("https://example.com", userConfig.getConnectorBaseURL());
    }

    @Test
    public void testGetMandantId() {
        assertEquals("123456", userConfig.getMandantId());
    }

    // Add more tests for other configuration properties as needed

    private UserConfigurations createSampleConfig() {
        UserConfigurations config = new UserConfigurations();
        config.setConnectorBaseURL("https://example.com");
        config.setMandantId("123456");
        // Set other configuration properties as needed
        return config;
    }
}
