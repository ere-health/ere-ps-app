package health.ere.ps.service.cetp;

import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.tel.error.v2.Error;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.jmx.PsMXBeanManager;
import health.ere.ps.jmx.SubscriptionsMXBean;
import health.ere.ps.jmx.SubscriptionsMXBeanImpl;
import health.ere.ps.service.cetp.config.KonnektorConfig;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import static health.ere.ps.service.cetp.config.KonnektorConfig.FAILED;
import static health.ere.ps.service.cetp.config.KonnektorConfig.saveFile;
import static health.ere.ps.utils.Utils.getHostFromNetworkInterfaces;
import static health.ere.ps.utils.Utils.printException;

@ApplicationScoped
public class SubscriptionManager {

    private final static Logger log = Logger.getLogger(SubscriptionManager.class.getName());

    public static final String CONFIG_KONNEKTOREN_FOLDER = "config/konnektoren";

    private final Map<String, KonnektorConfig> hostToKonnektorConfig = new ConcurrentHashMap<>();

    @ConfigProperty(name = "konnektor.subscription.event.to.host")
    Optional<String> eventToHostProperty;

    @Inject
    AppConfig appConfig;

    @Inject
    UserConfig userConfig;

    @Inject
    KonnektorClient konnektorClient;

    private String configFolder = CONFIG_KONNEKTOREN_FOLDER;

    private volatile boolean configsLoaded = false;

    void onStart(@Observes StartupEvent ev) {
        loadKonnektorConfigs();

        SubscriptionsMXBeanImpl subscriptionsMXBean = new SubscriptionsMXBeanImpl(hostToKonnektorConfig.size());
        PsMXBeanManager.registerMXBean(SubscriptionsMXBean.OBJECT_NAME, subscriptionsMXBean);

        renewSubscriptions();
    }

    @Scheduled(cron = "{konnektor.subscription.renew.cron}")
    void cronJobWithExpressionInConfig() {
        renewSubscriptions();
    }

    private void renewSubscriptions() {
        String eventToHost = getHostFromNetworkInterfaces();
        if (eventToHost == null) {
            eventToHost = eventToHostProperty.orElseThrow(() ->
                new IllegalStateException("eventToHost property is not found")
            );
        }
        manage(new RuntimeConfig(userConfig.getConfigurations()), null, eventToHost, false, true);
    }

    public void setConfigFolder(String configFolder) {
        this.configFolder = configFolder;
    }

    public Collection<KonnektorConfig> getKonnektorConfigs(String host) {
        if (!configsLoaded) {
            loadKonnektorConfigs();
        }
        return host == null
            ? hostToKonnektorConfig.values()
            : hostToKonnektorConfig.entrySet().stream().filter(entry -> entry.getKey().contains(host)).map(Map.Entry::getValue).toList();
    }

    private RuntimeConfig getRuntimeConfig(RuntimeConfig runtimeConfig, KonnektorConfig konnektorConfig) {
        if (runtimeConfig == null) {
            return new RuntimeConfig(konnektorConfig.getUserConfigurations());
        } else {
            runtimeConfig.updateProperties(konnektorConfig.getUserConfigurations());
            return runtimeConfig;
        }
    }

    public List<String> manage(
        RuntimeConfig runtimeConfig,
        String host,
        String eventToHost,
        boolean forceCetp,
        boolean subscribe
    ) {
        Collection<KonnektorConfig> konnektorConfigs = getKonnektorConfigs(host);
        List<String> statuses = konnektorConfigs.stream().map(kc -> {
                Semaphore semaphore = kc.getSemaphore();
                if (semaphore.tryAcquire()) {
                    try {
                        return process(kc, getRuntimeConfig(runtimeConfig, kc), eventToHost, forceCetp, subscribe);
                    } finally {
                        semaphore.release();
                    }
                } else {
                    try {
                        String h = host == null ? kc.getUserConfigurations().getConnectorBaseURL().split("//")[1] : host;
                        String s = subscribe ? "subscription" : "unsubscription";
                        return String.format("[%s] Host %s is in progress, try later", h, s);
                    } catch (Exception e) {
                        return e.getMessage();
                    }
                }
            })
            .filter(Objects::nonNull)
            .toList();

        if (statuses.isEmpty()) {
            return List.of(String.format("No configuration is found for the given host: %s", host));
        }
        return statuses;
    }

    private String process(
        KonnektorConfig konnektorConfig,
        RuntimeConfig runtimeConfig,
        String eventToHost,
        boolean forceCetp,
        boolean subscribe
    ) {
        String subscriptionId = konnektorConfig.getSubscriptionId();
        String failedUnsubscriptionFileName = subscriptionId != null && subscriptionId.startsWith(FAILED)
            ? subscriptionId
            : String.format("%s-unsubscription-%s", FAILED, subscriptionId);

        String failedSubscriptionFileName = String.format("%s-subscription", FAILED);
        String cetpHost = "cetp://" + eventToHost + ":" + konnektorConfig.getPort();

        String statusResult;
        boolean unsubscribed = false;
        try {
            Status status = konnektorClient.unsubscribeFromKonnektor(runtimeConfig, subscriptionId, cetpHost, forceCetp);
            Error error = status.getError();
            if (error == null) {
                unsubscribed = true;
                statusResult = status.getResult();
                log.info(String.format("Unsubscribe status for subscriptionId=%s: %s", subscriptionId, statusResult));
                if (subscribe) {
                    Triple<Status, String, String> triple = konnektorClient.subscribeToKonnektor(runtimeConfig, cetpHost);
                    status = triple.getLeft();
                    error = status.getError();
                    if (error == null) {
                        String newSubscriptionId = triple.getMiddle();
                        statusResult = status.getResult() + " " + newSubscriptionId + " " + triple.getRight();
                        saveFile(konnektorConfig, newSubscriptionId, null);
                        log.info(String.format("Subscribe status for subscriptionId=%s: %s", newSubscriptionId, status.getResult()));
                    } else {
                        statusResult = error.toString();
                        saveFile(konnektorConfig, failedSubscriptionFileName, statusResult);
                        log.log(Level.WARNING, String.format("Could not subscribe -> %s", error));
                    }
                } else {
                    KonnektorConfig.cleanUp(konnektorConfig.getFolder(), null);
                }
            } else {
                statusResult = error.toString();
                saveFile(konnektorConfig, failedUnsubscriptionFileName, statusResult);
                log.log(Level.WARNING, String.format("Could not unsubscribe from %s -> %s", subscriptionId, error));
            }
        } catch (Exception e) {
            String fileName = unsubscribed ? failedSubscriptionFileName : failedUnsubscriptionFileName;
            saveFile(konnektorConfig, fileName, printException(e));
            log.log(Level.WARNING, "Error: " + fileName, e);
            statusResult = e.getMessage();
        }
        return statusResult;
    }

    private String getConnectorBaseURL(KonnektorConfig config) {
        String connectorBaseURL = config.getUserConfigurations().getConnectorBaseURL();
        return connectorBaseURL == null ? appConfig.getConnectorBaseURL() : connectorBaseURL;
    }

    private void loadKonnektorConfigs() {
        List<KonnektorConfig> configs = new ArrayList<>();
        var konnektorConfigFolder = new File(configFolder);
        if (konnektorConfigFolder.exists()) {
            configs = KonnektorConfig.readFromFolder(konnektorConfigFolder.getAbsolutePath());
        }
        if (configs.isEmpty()) {
            configs.add(new KonnektorConfig(
                konnektorConfigFolder,
                CETPServer.PORT,
                userConfig.getConfigurations(),
                appConfig.getCardLinkURI())
            );
        }
        configs.forEach(config -> hostToKonnektorConfig.put(getConnectorBaseURL(config), config));
        configsLoaded = true;
    }
}
