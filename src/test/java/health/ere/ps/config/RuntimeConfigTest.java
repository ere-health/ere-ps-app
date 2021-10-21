package health.ere.ps.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;

import health.ere.ps.event.AbortTasksEvent;
import health.ere.ps.model.config.UserConfigurations;

public class RuntimeConfigTest {
    @Test
    public void testUserConfigurations() throws FileNotFoundException {
        JsonObject messageWithRuntimeConfig = Json.createReader(new FileInputStream("src/test/resources/websocket-messages/AbortTasks-With-RuntimeConfig.json")).readObject();
        AbortTasksEvent abortTasksEvent = new AbortTasksEvent(messageWithRuntimeConfig, null, null);
        assertEquals("HBA-1", abortTasksEvent.getRuntimeConfig().getEHBAHandle());
        assertEquals("SMCB-1", abortTasksEvent.getRuntimeConfig().getSMCBHandle());
        assertEquals("https://localhost:8080/", abortTasksEvent.getRuntimeConfig().getConnectorBaseURL());
        assertEquals("admin", abortTasksEvent.getRuntimeConfig().getConfigurations().getBasicAuthUsername());
        assertEquals("password", abortTasksEvent.getRuntimeConfig().getConfigurations().getBasicAuthPassword());
        assertEquals("data:application/x-pkcs12;base64,MIA", abortTasksEvent.getRuntimeConfig().getConfigurations().getClientCertificate().substring(0, 36));
        assertEquals("00", abortTasksEvent.getRuntimeConfig().getConfigurations().getClientCertificatePassword());
        assertEquals("mandant-id", abortTasksEvent.getRuntimeConfig().getMandantId());
        assertEquals("crypt", abortTasksEvent.getRuntimeConfig().getTvMode());
        assertEquals("user-id", abortTasksEvent.getRuntimeConfig().getUserId());
        assertEquals("version-if-not-readable", abortTasksEvent.getRuntimeConfig().getConnectorVersion());
        assertEquals("workplace-id", abortTasksEvent.getRuntimeConfig().getWorkplaceId());
    }

    @Test
    public void testUserConfig() {
        JsonObject messageWithRuntimeConfig = Json.createObjectBuilder()
            .add("runtimeConfig", Json.createObjectBuilder())
        .build();
        AbortTasksEvent abortTasksEvent = new AbortTasksEvent(messageWithRuntimeConfig, null, null);
        UserConfig userConfig = new UserConfig();
        userConfig.defaultClientSystemId = "1";
        userConfig.defaultConnectorBaseURI = "2";
        userConfig.defaultConnectorVersion = "3";
        userConfig.defaultMandantId = "4";
        userConfig.defaultUserId = "5";
        userConfig.defaultWorkplaceId = "6";
        RuntimeConfig runtimeConfig = abortTasksEvent.getRuntimeConfig();
        userConfig.updateProperties(new UserConfigurations());
        runtimeConfig.copyValuesFromUserConfig(userConfig);
        assertEquals("1", runtimeConfig.getClientSystemId());
        assertEquals("2", runtimeConfig.getConnectorBaseURL());
        assertEquals("3", runtimeConfig.getConnectorVersion());
        assertEquals("4", runtimeConfig.getMandantId());
        assertEquals("5", runtimeConfig.getUserId());
        assertEquals("6", runtimeConfig.getWorkplaceId());

        runtimeConfig.getConfigurations().setConnectorBaseURL("7");
        assertEquals("7", runtimeConfig.getConnectorBaseURL());
    }
}
