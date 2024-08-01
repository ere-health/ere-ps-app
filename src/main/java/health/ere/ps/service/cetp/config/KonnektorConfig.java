package health.ere.ps.service.cetp.config;

import health.ere.ps.model.config.UserConfigurations;

import java.io.File;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.concurrent.Semaphore;

public class KonnektorConfig {

    File folder;
    Integer cetpPort;
    URI cardlinkEndpoint;
    String subscriptionId;
    OffsetDateTime subscriptionTime;
    UserConfigurations userConfigurations;

    private final Semaphore semaphore = new Semaphore(1);

    public KonnektorConfig() {
    }

    public KonnektorConfig(
        File folder,
        Integer cetpPort,
        UserConfigurations userConfigurations,
        URI cardlinkEndpoint
    ) {
        this.folder = folder;
        this.cetpPort = cetpPort;
        this.userConfigurations = userConfigurations;
        this.cardlinkEndpoint = cardlinkEndpoint;

        subscriptionId = null;
        subscriptionTime = OffsetDateTime.now().minusDays(30);
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public Integer getCetpPort() {
        return cetpPort;
    }

    public UserConfigurations getUserConfigurations() {
        return userConfigurations;
    }

    public String getHost() {
        String connectorBaseURL = userConfigurations.getConnectorBaseURL();
        return connectorBaseURL == null ? null : connectorBaseURL.split("//")[1];
    }

    public URI getCardlinkEndpoint() {
        return cardlinkEndpoint;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public File getFolder() {
        return folder;
    }

    public OffsetDateTime getSubscriptionTime() {
        return subscriptionTime;
    }

    public void setSubscriptionTime(OffsetDateTime subscriptionTime) {
        this.subscriptionTime = subscriptionTime;
    }
}
