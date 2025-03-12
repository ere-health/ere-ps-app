package health.ere.ps.service.cetp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import de.health.service.cetp.SubscriptionManager;
import de.health.service.cetp.config.KonnektorConfig;
import de.health.service.cetp.domain.eventservice.card.Card;
import de.health.service.cetp.domain.fault.CetpFault;

public class RegisterSMCBJobTest {

    private RegisterSMCBJob getRegisterSMCBJob() throws CetpFault {
        RegisterSMCBJob registerSMCBJob = new RegisterSMCBJob();
        assertEquals(0, registerSMCBJob.cardlinkWebsocketClients.size());
        registerSMCBJob.subscriptionManager = mock(SubscriptionManager.class);
        registerSMCBJob.konnektorClient = mock(KonnektorClient.class);
        when(registerSMCBJob.konnektorClient.getCards(any(), any())).thenReturn(Arrays.asList(new Card()));
        when(registerSMCBJob.subscriptionManager.getKonnektorConfigs(any(), any())).thenReturn(Arrays.asList(new KonnektorConfig()));
        return registerSMCBJob;
    }

    @Test
    public void testRegisterSmcbMaintenance() throws CetpFault {
        RegisterSMCBJob registerSMCBJob = getRegisterSMCBJob();
        registerSMCBJob.initWSClients();
        assertEquals(1, registerSMCBJob.cardlinkWebsocketClients.size());
        registerSMCBJob.registerSmcbMaintenance();
    }

}
