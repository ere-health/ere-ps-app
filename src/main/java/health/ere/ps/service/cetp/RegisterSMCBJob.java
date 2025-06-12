package health.ere.ps.service.cetp;

import de.health.service.cetp.SubscriptionManager;
import de.health.service.cetp.cardlink.CardlinkWebsocketClient;
import de.health.service.cetp.config.KonnektorConfig;
import de.health.service.cetp.domain.eventservice.card.Card;
import de.health.service.cetp.domain.eventservice.card.CardType;
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
import java.util.Arrays;
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

    private static final Logger LOGGER_I18N = Logger.getLogger("org.jboss.resteasy.client.jaxrs.i18n");
    private static final Logger LOGGER_WS = Logger.getLogger("de.health.service.cetp.cardlink.CardlinkWebsocketClient");

    @Inject
    KonnektorClient konnektorClient;

    @Inject
    BearerTokenService bearerTokenService;

    @Inject
    SubscriptionManager subscriptionManager;

    int counter = 0;

    List<CardlinkWebsocketClient> cardlinkWebsocketClients = new ArrayList<>();

    // Make sure subscription manager calls onStart first, before RegisterSMCBJob at least!
    void onStart(@Observes @Priority(5300) StartupEvent ev) {
        LOGGER_I18N.setLevel(Level.SEVERE);
        LOGGER_WS.setLevel(Level.WARNING);
        
        log.info("RegisterSMCBJob init onStart");
        initWSClients();
    }

    void initWSClients() {
        Collection<KonnektorConfig> konnektorConfigs = subscriptionManager.getKonnektorConfigs(null, null);

        SubscriptionsMXBeanImpl subscriptionsMXBean = new SubscriptionsMXBeanImpl(konnektorConfigs.size());
        registerMXBean(SubscriptionsMXBean.OBJECT_NAME, subscriptionsMXBean);

        cardlinkWebsocketClients = new ArrayList<>();
        konnektorConfigs.forEach(kc -> {

            List<Card> cards;
            try {
                cards = konnektorClient.getCards(new RuntimeConfig(kc.getUserConfigurations()), CardType.SMC_B);
            } catch (Exception e) {
                log.log(Level.WARNING, "Could not read SMC-Bs", e);
                cards=Arrays.asList(new Card());
            }
            for(Card card : cards) {
                log.info("Creating Websockets for SMC-B: " + card.getCardHandle());
                RuntimeConfig userRuntimeConfig = new RuntimeConfig(kc.getUserConfigurations());
                userRuntimeConfig.setSMCBHandle(card.getCardHandle());
                cardlinkWebsocketClients.add(
                    new CardlinkWebsocketClient(
                        kc.getCardlinkEndpoint(),
                        new EreJwtConfigurator(
                            userRuntimeConfig,
                            konnektorClient,
                            bearerTokenService,
                            (e) -> {
                                log.info("Reloading websocket clients due to exception");
                                initWSClients();
                            }
                        )
                    )
                );
            }

        });
    }

    @Scheduled(
        every = "${cetp.register.smcb.maintenance.interval.sec:30s}",
        delayUnit = TimeUnit.SECONDS,
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP
    )
    void registerSmcbMaintenance() {
        String correlationId = UUID.randomUUID().toString();
        log.fine(String.format("RegisterSMCBJob started with %s", correlationId));
        // reload cardlink configs if necessary, or all 100 tries
        if (cardlinkWebsocketClients.isEmpty() || counter % 100 == 99) {
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
        counter++;
    }
}