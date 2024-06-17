package health.ere.ps.jmx;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.model.status.Status;
import health.ere.ps.service.cetp.SubscriptionManager;
import health.ere.ps.service.status.StatusService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

@ApplicationScoped
public class StatusMXBeanImpl implements StatusMXBean {
    private static final Logger LOG = Logger.getLogger(StatusMXBeanImpl.class.getName());
    private static final Integer CACHEKEY = 1;

    private final StatusService statusService;
    private final SubscriptionManager subscriptionManager;

    private final LoadingCache<Integer, List<Status>> statusCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .maximumSize(1) //cache holds only a one single item
            .build(key -> getStatus());

    @Inject
    public StatusMXBeanImpl(StatusService statusService, SubscriptionManager subscriptionManager) {
        this.statusService = statusService;
        this.subscriptionManager = subscriptionManager;
    }

    void onStart(@Observes StartupEvent ev) {
        PsMXBeanManager.registerMXBean(this);
    }

    private List<Status> getStatus() {
        var konnektorConfigs = subscriptionManager.getKonnektorConfigs(null);
        var data = new ArrayList<Status>(konnektorConfigs.size());
        for (var konnektorConfig : konnektorConfigs) {
            try {
                var runtimeConfig = new RuntimeConfig(konnektorConfig.getUserConfigurations());
                var status = statusService.getStatus(runtimeConfig);
                data.add(status);
            } catch (Exception e) {
                LOG.severe("Error while retrieving app status for " + konnektorConfig.getSubscriptionId() + " : " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        return data;
    }

    private boolean[] toBooleanArray(Predicate<Status> extractor) {
        List<Status> statuses = statusCache.get(CACHEKEY);
        var array = new boolean[statuses.size()];
        for (int i = 0; i < statuses.size(); i++) {
            array[i] = extractor.test(statuses.get(i));
        }
        return array;
    }

    private String[] toStringArray(Function<Status, String> extractor) {
        List<Status> statuses = statusCache.get(CACHEKEY);
        var array = new String[statuses.size()];
        for (int i = 0; i < statuses.size(); i++) {
            array[i] = extractor.apply(statuses.get(i));
        }
        return array;
    }

    @Override
    public boolean[] isConnectorReachable() {
        return toBooleanArray(Status::getConnectorReachable);
    }

    @Override
    public String[] getConnectorInformation() {
        return toStringArray(Status::getConnectorInformation);
    }

    @Override
    public boolean[] isIdpReachable() {
        return toBooleanArray(Status::getIdpReachable);
    }

    @Override
    public String[] getIdpInformation() {
        return toStringArray(Status::getIdpInformation);
    }

    @Override
    public String[] getBearerToken() {
        return toStringArray(Status::getBearerToken);
    }

    @Override
    public boolean[] isIdpaccesstokenObtainable() {
        return toBooleanArray(Status::getIdpaccesstokenObtainable);
    }

    @Override
    public String[] getIdpaccesstokenInformation() {
        return toStringArray(Status::getIdpaccesstokenInformation);
    }

    @Override
    public boolean[] isSmcbAvailable() {
        return toBooleanArray(Status::getSmcbAvailable);
    }

    @Override
    public String[] getSmcbInformation() {
        return toStringArray(Status::getSmcbInformation);
    }

    @Override
    public boolean[] isCautReadable() {
        return toBooleanArray(Status::getCautReadable);
    }

    @Override
    public String[] getCautInformation() {
        return toStringArray(Status::getCautInformation);
    }

    @Override
    public boolean[] isEhbaAvailable() {
        return toBooleanArray(Status::getEhbaAvailable);
    }

    @Override
    public String[] getEhbaInformation() {
        return toStringArray(Status::getEhbaInformation);
    }

    @Override
    public boolean[] isComfortsignatureAvailable() {
        return toBooleanArray(Status::getComfortsignatureAvailable);
    }

    @Override
    public String[] getComfortsignatureInformation() {
        return toStringArray(Status::getComfortsignatureInformation);
    }

    @Override
    public boolean[] isFachdienstReachable() {
        return toBooleanArray(Status::getFachdienstReachable);
    }

    @Override
    public String[] getFachdienstInformation() {
        return toStringArray(Status::getFachdienstInformation);
    }
}
