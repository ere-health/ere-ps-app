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
        Collection<KonnektorConfig> konnektorConfigs = getKonnektorConfigs(host);
        List<String> statuses = konnektorConfigs.stream()
            .map(kc -> process(kc, runtimeConfig, host, eventToHost, subscribe))
            .filter(Objects::nonNull)
            .toList();

        if (statuses.isEmpty()) {
            return List.of(String.format("No configuration is found for the given host: %s", host));
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
                    Pair<String, String> pair = subscribeToKonnektor(kc, config, port, eventToHost);
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

    private Pair<String, String> subscribeToKonnektor(
        KonnektorConfig konnektorConfig,
        RuntimeConfig runtimeConfig,
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
                KonnektorConfig.createNewSubscriptionIdFile(
                    konnektorConfig.getFolder(),
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
        if (subscriptionId == null) {
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
