package health.ere.ps.jmx;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.model.status.Status;
import health.ere.ps.service.cetp.SubscriptionManager;
import health.ere.ps.service.status.StatusService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import javax.management.openmbean.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.Map.entry;
import static javax.management.openmbean.SimpleType.BOOLEAN;
import static javax.management.openmbean.SimpleType.STRING;

@ApplicationScoped
public class StatusMXBeanImpl implements StatusMXBean {
    private record TypeDescriptionComponent(String name, OpenType<?> type) {
    }

    private static final Logger LOG = Logger.getLogger(StatusMXBeanImpl.class.getName());

    private final StatusService statusService;
    private final SubscriptionManager subscriptionManager;

    private final CompositeType singleKonnectorType;
    private final CompositeType allKonnectorType;

    @Inject
    public StatusMXBeanImpl(StatusService statusService, SubscriptionManager subscriptionManager) throws OpenDataException {
        this.statusService = statusService;
        this.subscriptionManager = subscriptionManager;

        var parts = createStatusModel();
        var names = parts.stream().map(TypeDescriptionComponent::name).toArray(String[]::new);
        var types = parts.stream().map(TypeDescriptionComponent::type).toArray(OpenType<?>[]::new);

        singleKonnectorType = new CompositeType(
                "KonnektorStatus",
                "Status of an Konnektor",
                names, names, types);
        allKonnectorType = new CompositeType(
                "ERE-PS-APP Status",
                "Status of the ERE-PS-APP",
                new String[]{"Konnektors"},
                new String[]{"All Konnektors"},
                new OpenType[]{new ArrayType<>(1, singleKonnectorType)}
        );
    }

    void onStart(@Observes StartupEvent ev) {
        PsMXBeanManager.registerMXBean(this);
    }

    @Override
    public CompositeData getStatus() throws OpenDataException {
        var konnektorConfigs = subscriptionManager.getKonnektorConfigs(null);
        var data = new ArrayList<CompositeDataSupport>(konnektorConfigs.size());
        for (var konnektorConfig : konnektorConfigs) {
            try {
                var runtimeConfig = new RuntimeConfig(konnektorConfig.getUserConfigurations());
                Status status = statusService.getStatus(runtimeConfig);
                var items = mapStatus(status, konnektorConfig.getSubscriptionId());
                data.add(new CompositeDataSupport(singleKonnectorType, items));
            } catch (OpenDataException e) {
                LOG.severe("Error while retrieving app status: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        CompositeData[] dataArray = data.toArray(CompositeData[]::new);
        return new CompositeDataSupport(allKonnectorType, new String[]{"Konnektors"}, new Object[]{dataArray});
    }

    private static Map<String, Object> mapStatus(Status status, String id) {
        return Map.ofEntries(
                entry("KonnektorId", id),
                entry("connectorReachable", status.getConnectorReachable()),
                entry("connectorInformation", notNull(status.getConnectorInformation())),
                entry("idpReachable", status.getIdpReachable()),
                entry("idpInformation", notNull(status.getIdpInformation())),
                entry("bearerToken", notNull(status.getBearerToken())),
                entry("idpaccesstokenObtainable", status.getIdpaccesstokenObtainable()),
                entry("idpaccesstokenInformation", notNull(status.getIdpaccesstokenInformation())),
                entry("smcbAvailable", status.getSmcbAvailable()),
                entry("smcbInformation", notNull(status.getSmcbInformation())),
                entry("cautReadable", status.getCautReadable()),
                entry("cautInformation", notNull(status.getCautInformation())),
                entry("ehbaAvailable", status.getEhbaAvailable()),
                entry("ehbaInformation", notNull(status.getEhbaInformation())),
                entry("comfortsignatureAvailable", status.getComfortsignatureAvailable()),
                entry("comfortsignatureInformation", notNull(status.getComfortsignatureInformation())),
                entry("fachdienstReachable", status.getFachdienstReachable()),
                entry("fachdienstInformation", notNull(status.getFachdienstInformation()))
        );
    }

    private static List<TypeDescriptionComponent> createStatusModel() {
        return List.of(
                new TypeDescriptionComponent("KonnektorId", STRING),
                new TypeDescriptionComponent("connectorReachable", BOOLEAN),
                new TypeDescriptionComponent("connectorInformation", STRING),
                new TypeDescriptionComponent("idpReachable", BOOLEAN),
                new TypeDescriptionComponent("idpInformation", STRING),
                new TypeDescriptionComponent("bearerToken", STRING),
                new TypeDescriptionComponent("idpaccesstokenObtainable", BOOLEAN),
                new TypeDescriptionComponent("idpaccesstokenInformation", STRING),
                new TypeDescriptionComponent("smcbAvailable", BOOLEAN),
                new TypeDescriptionComponent("smcbInformation", STRING),
                new TypeDescriptionComponent("cautReadable", BOOLEAN),
                new TypeDescriptionComponent("cautInformation", STRING),
                new TypeDescriptionComponent("ehbaAvailable", BOOLEAN),
                new TypeDescriptionComponent("ehbaInformation", STRING),
                new TypeDescriptionComponent("comfortsignatureAvailable", BOOLEAN),
                new TypeDescriptionComponent("comfortsignatureInformation", STRING),
                new TypeDescriptionComponent("fachdienstReachable", BOOLEAN),
                new TypeDescriptionComponent("fachdienstInformation", STRING)
        );
    }

    private static String notNull(String s) {
        if (s == null) {
            return "null";
        }
        return s;
    }
}
