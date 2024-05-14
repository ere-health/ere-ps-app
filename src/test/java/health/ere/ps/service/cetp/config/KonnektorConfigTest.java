package health.ere.ps.service.cetp.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class KonnektorConfigTest {
    @Test
    void testGenerateKonnektorConfig() {
        var configs = KonnektorConfig.readFromFolder("src/test/resources/config/konnektoren/");
        assertEquals(3, configs.size());
        assertEquals(configs.get(0).getPort(), 8585);
        assertEquals(configs.get(1).getPort(), 8586);
        assertEquals(configs.get(2).getPort(), 8587);


        assertEquals(configs.get(0).getCardlinkEndpoint().toString(), "wss://cardlink.service-health.de:8444/websocket/80276883662000004801-20220128");
        assertEquals(configs.get(2).getCardlinkEndpoint().toString(), "wss://cardlink.service-health.de:8444/websocket/80276883580000040142-20201102");
    }
}
