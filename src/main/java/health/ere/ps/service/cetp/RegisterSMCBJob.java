package health.ere.ps.service.cetp;

import de.health.service.cetp.SubscriptionManager;
import de.health.service.cetp.cardlink.CardlinkWebsocketClient;
import de.health.service.cetp.config.KonnektorConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.jmx.SubscriptionsMXBean;
import health.ere.ps.jmx.SubscriptionsMXBeanImpl;
import health.ere.ps.service.cardlink.EreJwtConfigurator;
import health.ere.ps.service.idp.BearerTokenService;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.Priority;
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

import static health.ere.ps.jmx.PsMXBeanManager.registerMXBean;

@ApplicationScoped
public class RegisterSMCBJob {

    private final static Logger log = Logger.getLogger(RegisterSMCBJob.class.getName());

    @Inject
    KonnektorClient konnektorClient;

    @Inject
    BearerTokenService bearerTokenService;

    @Inject
    SubscriptionManager subscriptionManager;

    private List<CardlinkWebsocketClient> cardlinkWebsocketClients = new ArrayList<>();

    // Make sure subscription manager calls onStart first, before RegisterSMCBJob at least!
    void onStart(@Observes @Priority(5300) StartupEvent ev) {
        log.info("RegisterSMCBJob init onStart");
        initWSClients();
    }

    private void initWSClients() {
        Collection<KonnektorConfig> konnektorConfigs = subscriptionManager.getKonnektorConfigs(null);

        SubscriptionsMXBeanImpl subscriptionsMXBean = new SubscriptionsMXBeanImpl(konnektorConfigs.size());
        registerMXBean(SubscriptionsMXBean.OBJECT_NAME, subscriptionsMXBean);

        cardlinkWebsocketClients = new ArrayList<>();
        konnektorConfigs.forEach(kc ->
            cardlinkWebsocketClients.add(
                new CardlinkWebsocketClient(
                    kc.getCardlinkEndpoint(),
                    new EreJwtConfigurator(
                        new RuntimeConfig(kc.getUserConfigurations()),
                        konnektorClient,
                        bearerTokenService
                    )
                )
            )
        );
    }

    @Scheduled(
        every = "${cetp.register.smcb.maintenance.interval.sec:30s}",
        delayUnit = TimeUnit.SECONDS,
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP
    )
    void registerSmcbMaintenance() {
        String correlationId = UUID.randomUUID().toString();
        log.info(String.format("RegisterSMCBJob started with %s", correlationId));
        // reload cardlink configs if necessary
        if (cardlinkWebsocketClients.isEmpty()) {
            initWSClients();
        }
        cardlinkWebsocketClients.forEach(client -> {
            try {
                client.connect();
                client.sendJson(correlationId, null, "registerSMCB", Map.of());
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error while sending registerSMCB", e);
            } finally {
                client.close();
            }
        });
    }
}
