package health.ere.ps.service.cetp;

import de.health.service.cetp.config.KonnektorConfig;
import de.health.service.cetp.domain.eventservice.card.Card;
import de.health.service.cetp.konnektorconfig.KonnektorsConfigs;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegisterSMCBJobTest {

    private RegisterSMCBJob getRegisterSMCBJob() throws Exception {
        RegisterSMCBJob registerSMCBJob = new RegisterSMCBJob();
        assertEquals(0, registerSMCBJob.cardlinkWebsocketClients.size());
        registerSMCBJob.konnektorClient = mock(KonnektorClient.class);
        registerSMCBJob.konnektorsConfigs = mock(KonnektorsConfigs.class);
        when(registerSMCBJob.konnektorClient.getCards(any(), any())).thenReturn(List.of(new Card()));
        when(registerSMCBJob.konnektorClient.isInitialized(any())).thenReturn(true);
        KonnektorConfig konnektorConfig = new KonnektorConfig(
            new File("."),
            8585,
            new URI("wss://cardlink.service-health.de:8444/websocket/80276883662000004801-20220128"),
            new de.health.service.cetp.konnektorconfig.UserConfigurations(new Properties())
        );

        when(registerSMCBJob.konnektorsConfigs.getConfigs()).thenReturn(List.of(konnektorConfig));
        return registerSMCBJob;
    }

    @Test
    public void testRegisterSmcbMaintenance() throws Exception {
        RegisterSMCBJob registerSMCBJob = getRegisterSMCBJob();
        registerSMCBJob.initWSClients();
        assertEquals(1, registerSMCBJob.cardlinkWebsocketClients.size());
        registerSMCBJob.registerSmcbMaintenance();
    }
}