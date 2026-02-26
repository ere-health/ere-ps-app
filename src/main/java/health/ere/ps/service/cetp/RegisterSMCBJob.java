package health.ere.ps.service.cetp;

import de.health.service.cetp.SubscriptionManager;
import de.health.service.cetp.cardlink.CardlinkWebsocketClient;
import de.health.service.cetp.config.KonnektorConfig;
import de.health.service.cetp.domain.eventservice.card.Card;
import de.health.service.cetp.domain.eventservice.card.CardType;
import de.health.service.config.api.IUserConfigurations;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.jmx.SubscriptionsMXBean;
import health.ere.ps.jmx.SubscriptionsMXBeanImpl;
import health.ere.ps.service.cardlink.EreJwtConfigurator;
import health.ere.ps.service.connector.provider.KonnektorKey;
import health.ere.ps.service.idp.BearerTokenService;
import health.ere.ps.startup.StartableService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static health.ere.ps.jmx.PsMXBeanManager.registerMXBean;

@ApplicationScoped
public class RegisterSMCBJob extends StartableService {

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

    @Override
    public int getPriority() {
        return RegisterSMCB;
    }

    @Override
    protected void doStart() throws Exception {
        LOGGER_I18N.setLevel(Level.SEVERE);
        LOGGER_WS.setLevel(Level.INFO);

        initWSClients();
    }

    private Set<KonnektorKey> getKonnektorKeys() {
        return subscriptionManager.getKonnektorConfigs(null, null).stream()
            .map(kc -> {
                IUserConfigurations userConfigurations = kc.getUserConfigurations();
                String userId = userConfigurations.getUserId();
                String mandantId = userConfigurations.getMandantId();
                String workplaceId = userConfigurations.getWorkplaceId();
                String clientSystemId = userConfigurations.getClientSystemId();
                String konnektorBaseUrl = userConfigurations.getConnectorBaseURL();
                return new KonnektorKey(userId, mandantId, workplaceId, clientSystemId, konnektorBaseUrl);
            }).collect(Collectors.toSet());
    }

    void initWSClients() {
        if (konnektorClient.isInitialized(getKonnektorKeys())) {
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
                    cards = List.of(new Card());
                }
                for (Card card : cards) {
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
    }

    @Scheduled(
        every = "${cetp.register.smcb.maintenance.interval.sec:300s}",
        delayUnit = TimeUnit.SECONDS,
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP
    )
    void registerSmcbMaintenance() {
        if (konnektorClient.isInitialized(getKonnektorKeys())) {
            String correlationId = UUID.randomUUID().toString();
            log.fine(String.format("RegisterSMCBJob started with %s", correlationId));
            // reload cardlink configs if necessary, or all 100 tries
            if (cardlinkWebsocketClients.isEmpty() || counter % 100 == 99) {
                initWSClients();
            }
            new ArrayList<>(cardlinkWebsocketClients).forEach(client -> {
                try {
                    client.connect();
                    client.sendJson(correlationId, null, "registerSMCB", Map.of());
                } catch (Exception e) {
                    log.log(Level.SEVERE, String.format("Error while sending registerSMCB: %s", e.getMessage()));
                } finally {
                    client.close();
                }
            });
            counter++;
        }
    }
}