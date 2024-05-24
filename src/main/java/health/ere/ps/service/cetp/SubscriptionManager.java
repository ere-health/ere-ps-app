package health.ere.ps.service.cetp;

import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.v7.SubscriptionType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.tel.error.v2.Error;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.jmx.PsMXBeanManager;
import health.ere.ps.jmx.SubscriptionsMXBean;
import health.ere.ps.jmx.SubscriptionsMXBeanImpl;
import health.ere.ps.service.cetp.config.KonnektorConfig;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.xml.ws.Holder;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import static health.ere.ps.service.cetp.config.KonnektorConfig.DEFAULT_SUBSCRIPTION;

@ApplicationScoped
public class SubscriptionManager {

    private final static Logger log = Logger.getLogger(SubscriptionManager.class.getName());

    public static final String CONFIG_KONNEKTOREN_FOLDER = "config/konnektoren";

    private final Map<String, KonnektorConfig> hostToKonnektorConfig = new ConcurrentHashMap<>();

    @ConfigProperty(name = "konnektor.subscription.eventToHost")
    Optional<String> eventToHostProperty;

    @Inject
    AppConfig appConfig;

    @Inject
    UserConfig userConfig;

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;

    private String configFolder = CONFIG_KONNEKTOREN_FOLDER;

    private final String eventToHost = getEventToHost();

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
        String eventHost = eventToHostProperty.orElse(eventToHost);
        manage(new RuntimeConfig(userConfig.getConfigurations()), null, eventHost, true);
    }

    public List<String> manage(RuntimeConfig runtimeConfig, String host, String eventToHost, boolean subscribe) {
        Optional<KonnektorConfig> kcOpt = getKonnektorConfig(host);
        List<String> statuses = new ArrayList<>();
        if (kcOpt.isPresent()) {
            KonnektorConfig kc = kcOpt.get();
            statuses.add(process(kc, runtimeConfig, host, eventToHost, subscribe));
        } else {
            Collection<KonnektorConfig> konnektorConfigs = getKonnektorConfigs();
            statuses.addAll(konnektorConfigs.stream()
                .map(kc -> process(kc, runtimeConfig, host, eventToHost, subscribe))
                .filter(Objects::nonNull)
                .toList()
            );
        }
        return statuses;
    }

    private String process(
        KonnektorConfig kc,
        RuntimeConfig runtimeConfig,
        String host,
        String eventToHost,
        boolean subscribe
    ) {
        Semaphore semaphore = kc.getSemaphore();
        if (semaphore.tryAcquire()) {
            try {
                Integer port = kc.getPort();
                RuntimeConfig config = getRuntimeConfig(runtimeConfig, kc);
                String subscriptionId = kc.getSubscriptionId();
                String status = unsubscribeFromKonnektor(config, subscriptionId, port, eventToHost);
                log.info(String.format("Unsubscribe status for subscriptionId=%s: %s", subscriptionId, status));
                if (subscribe) {
                    Pair<String, String> pair = subscribeToKonnektor(kc, config, subscriptionId, port, eventToHost);
                    status = pair.getValue();
                    log.info(String.format("Subscribe status for subscriptionId=%s: %s", pair.getKey(), status));
                }
                return status;
            } finally {
                semaphore.release();
            }
        } else {
            return String.format("[%s] Host subscription is in progress, try later", host);
        }
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
        configs.forEach(config ->
            hostToKonnektorConfig.put(config.getUserConfigurations().getConnectorBaseURL(), config)
        );
        configsLoaded = true;
    }

    public void setConfigFolder(String configFolder) {
        this.configFolder = configFolder;
    }

    public Collection<KonnektorConfig> getKonnektorConfigs() {
        return hostToKonnektorConfig.values();
    }

    public Optional<KonnektorConfig> getKonnektorConfig(String host) {
        if (!configsLoaded) {
            loadKonnektorConfigs();
        }
        return host == null
            ? Optional.empty()
            : Optional.ofNullable(hostToKonnektorConfig.get(findHostKey(host)));
    }

    private String findHostKey(String host) {
        Optional<String> keyOpt = hostToKonnektorConfig.keySet().stream().filter(s -> s.contains(host)).findFirst();
        return keyOpt.orElse("");
    }

    private RuntimeConfig getRuntimeConfig(RuntimeConfig runtimeConfig, KonnektorConfig konnektorConfig) {
        if (runtimeConfig == null) {
            return new RuntimeConfig(konnektorConfig.getUserConfigurations());
        } else {
            runtimeConfig.updateProperties(konnektorConfig.getUserConfigurations());
            return runtimeConfig;
        }
    }

    private Pair<String, String> subscribeToKonnektor(
        KonnektorConfig konnektorConfig,
        RuntimeConfig runtimeConfig,
        String prevSubscriptionId,
        Integer port,
        String eventToHost
    ) {
        try {
            ContextType context = connectorServicesProvider.getContextType(runtimeConfig);

            EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);
            SubscriptionType subscriptionType = new SubscriptionType();

            subscriptionType.setEventTo("cetp://" + eventToHost + ":" + port);
            subscriptionType.setTopic("CARD/INSERTED");
            Holder<Status> status = new Holder<>();
            Holder<String> subscriptionId = new Holder<>();
            Holder<XMLGregorianCalendar> terminationTime = new Holder<>();

            eventService.subscribe(context, subscriptionType, status, subscriptionId, terminationTime);

            try {
                KonnektorConfig.recreateSubscriptionProperties(
                    konnektorConfig.getFolder(),
                    prevSubscriptionId,
                    subscriptionId.value
                );
                konnektorConfig.setSubscriptionId(subscriptionId.value + ".properties");
            } catch (IOException e) {
                String msg = String.format(
                    "Error while recreating subscription properties in folder: %s",
                    konnektorConfig.getFolder().getAbsolutePath()
                );
                log.log(Level.SEVERE, msg, e);
            }

            String fullStatus = status.value.getResult() + " " + subscriptionId.value + " " + terminationTime.value.toString();
            return Pair.of(subscriptionId.value, fullStatus);
        } catch (Exception e) {
            return Pair.of(null, e.getMessage());
        }
    }

    private String unsubscribeFromKonnektor(
        RuntimeConfig runtimeConfig,
        String subscriptionId,
        Integer port,
        String eventToHost
    ) {
        if (DEFAULT_SUBSCRIPTION.equals(subscriptionId)) {
            return null;
        }
        try {
            ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
            EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);

            Status status = eventService.unsubscribe(context, subscriptionId, "cetp://" + eventToHost + ":" + port);
            Error error = status.getError();
            return error == null ? status.getResult() : error.toString();
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not unsubscribe " + subscriptionId, e);
            return e.getMessage();
        }
    }

    private String getEventToHost() {
        Inet4Address localAddress = null;
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = e.nextElement();
                Enumeration<InetAddress> ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = ee.nextElement();
                    if (i instanceof Inet4Address && i.getAddress()[0] != 127) {
                        localAddress = (Inet4Address) i;
                        break;
                    }
                }
            }
        } catch (SocketException ignored) {
        }
        return localAddress == null ? "localhost" : localAddress.getHostAddress();
    }
}
