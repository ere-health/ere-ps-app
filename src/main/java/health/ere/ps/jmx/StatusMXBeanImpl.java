package health.ere.ps.jmx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.health.service.cetp.SubscriptionManager;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.model.status.Status;
import health.ere.ps.service.status.StatusService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class StatusMXBeanImpl implements StatusMXBean {
    private static final Logger LOG = Logger.getLogger(StatusMXBeanImpl.class.getName());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final StatusService statusService;
    private final SubscriptionManager subscriptionManager;

    @Inject
    public StatusMXBeanImpl(StatusService statusService, SubscriptionManager subscriptionManager) {
        this.statusService = statusService;
        this.subscriptionManager = subscriptionManager;
    }

//    void onStart(@Observes StartupEvent ev) {
//        PsMXBeanManager.registerMXBean(this);
//    }

    public String getStatus() {
        var konnektorConfigs = subscriptionManager.getKonnektorConfigs(null);
        ObjectNode rootNode = OBJECT_MAPPER.createObjectNode();

        for (var konnektorConfig : konnektorConfigs) {
            try {
                var runtimeConfig = new RuntimeConfig(konnektorConfig.getUserConfigurations());
                var status = statusService.getStatus(runtimeConfig);
                var childNode = mapToJson(status);  //gives each node a name so it can be displayed in grafana
                rootNode.set(konnektorConfig.getSubscriptionId(), childNode);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error while retrieving app status for " + konnektorConfig.getSubscriptionId() + " : " + e.getMessage(), e);
                return null;
            }
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }

    private ObjectNode mapToJson(Status status) {
        var objectNode = OBJECT_MAPPER.createObjectNode();
        objectNode.put("connectorReachable", status.getConnectorReachable());
        objectNode.put("connectorInformation", status.getConnectorInformation());
        objectNode.put("idpReachable", status.getIdpReachable());
        objectNode.put("idpInformation", status.getIdpInformation());
        objectNode.put("bearerToken", status.getBearerToken());
        objectNode.put("idpaccesstokenObtainable", status.getIdpaccesstokenObtainable());
        objectNode.put("idpaccesstokenInformation", status.getIdpaccesstokenInformation());
        objectNode.put("smcbAvailable", status.getSmcbAvailable());
        objectNode.put("smcbInformation", status.getSmcbInformation());
        objectNode.put("cautReadable", status.getCautReadable());
        objectNode.put("cautInformation", status.getCautInformation());
        objectNode.put("ehbaAvailable", status.getEhbaAvailable());
        objectNode.put("ehbaInformation", status.getEhbaInformation());
        objectNode.put("comfortsignatureAvailable", status.getComfortsignatureAvailable());
        objectNode.put("comfortsignatureInformation", status.getComfortsignatureInformation());
        objectNode.put("fachdienstReachable", status.getFachdienstReachable());
        objectNode.put("fachdienstInformation", status.getFachdienstInformation());
        return objectNode;
    }
}
