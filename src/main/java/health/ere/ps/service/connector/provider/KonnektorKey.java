package health.ere.ps.service.connector.provider;

import de.health.service.config.api.UserRuntimeConfig;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class KonnektorKey {

    private final String userId;
    private final String mandantId;
    private final String workplaceId;
    private final String clientSystemId;
    private final String konnektorBaseUrl;

    public KonnektorKey(
        String userId,
        String mandantId,
        String workplaceId,
        String clientSystemId,
        String konnektorBaseUrl
    ) {
        this.userId = userId;
        this.mandantId = mandantId;
        this.workplaceId = workplaceId;
        this.clientSystemId = clientSystemId;
        this.konnektorBaseUrl = konnektorBaseUrl;
    }

    public KonnektorKey(UserRuntimeConfig runtimeConfig) {
        // Use deprecated until default Konnektor config is still used
        userId = runtimeConfig.getUserId();
        mandantId = runtimeConfig.getMandantId();
        workplaceId = runtimeConfig.getWorkplaceId();
        clientSystemId = runtimeConfig.getClientSystemId();
        konnektorBaseUrl = runtimeConfig.getConnectorBaseURL();
    }
}
