package health.ere.ps.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import health.ere.ps.event.config.UserConfigurationsUpdateEvent;
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
        userConfig.defaultConnectorBaseURI = "https://default.com";
        userConfig.defaultMandantId = "000000";
        userConfig.defaultWorkplaceId = "000000";
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

    @Test
    public void testGetWorkplaceId_UsesDefault() {
        assertEquals("000000", userConfig.getWorkplaceId());
    }

    // Add more tests for other configuration properties as needed

    @Test
    public void testHandleUpdateProperties() {
        UserConfigurations newConfig = new UserConfigurations();
        newConfig.setConnectorBaseURL("https://new-url.com");
        newConfig.setMandantId("999999");

        UserConfigurationsUpdateEvent event = new UserConfigurationsUpdateEvent(newConfig);
        userConfig.handleUpdateProperties(event);

        assertEquals("https://new-url.com", userConfig.getConnectorBaseURL());
        assertEquals("999999", userConfig.getMandantId());
    }

    private UserConfigurations createSampleConfig() {
        UserConfigurations config = new UserConfigurations();
        config.setConnectorBaseURL("https://example.com");
        config.setMandantId("123456");
        // Set other configuration properties as needed
        return config;
    }
}
