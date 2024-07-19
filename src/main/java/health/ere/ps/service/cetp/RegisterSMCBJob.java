package health.ere.ps.service.cetp;

import health.ere.ps.service.cardlink.AddJWTConfigurator;
import health.ere.ps.service.cardlink.CardlinkWebsocketClient;
import health.ere.ps.service.cetp.config.KonnektorConfig;
import health.ere.ps.service.health.check.CardlinkWebsocketCheck;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class RegisterSMCBJob {

    private final static Logger log = Logger.getLogger(RegisterSMCBJob.class.getName());

    @Inject
    CardlinkWebsocketCheck cardlinkWebsocketCheck;

    @Inject
    SubscriptionManager subscriptionManager;

    private List<CardlinkWebsocketClient> cardlinkWebsocketClients;

    void onStart(@Observes StartupEvent ev) {
        log.info("RegisterSMCBJob init onStart");
        Collection<KonnektorConfig> konnektorConfigs = subscriptionManager.getKonnektorConfigs(null);
        cardlinkWebsocketClients = new ArrayList<>();
        konnektorConfigs.forEach(kc ->
            cardlinkWebsocketClients.add(new CardlinkWebsocketClient(
                kc.getCardlinkEndpoint(),
                cardlinkWebsocketCheck)
            ));
        AddJWTConfigurator.initConfigs(konnektorConfigs);
    }

    @Scheduled(
        every = "${cetp.register.smcb.maintenance.interval.sec:30s}",
        delayUnit = TimeUnit.SECONDS,
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP
    )
    void registerSmcbMaintenance() {
        String correlationId = UUID.randomUUID().toString();
        log.info(String.format("RegisterSMCBJob started with %s", correlationId));
        cardlinkWebsocketClients.forEach(client -> {
            try {
                client.connect();
                client.sendJson(correlationId, null,"registerSMCB", Map.of());
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error while sending registerSMCB", e);
            } finally {
                client.close();
            }
        });
    }
}
