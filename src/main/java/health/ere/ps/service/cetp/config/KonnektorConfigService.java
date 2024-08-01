package health.ere.ps.service.cetp.config;

import java.util.Map;

public interface KonnektorConfigService {

    Map<String, KonnektorConfig> loadConfigs();

    void saveSubscription(KonnektorConfig konnektorConfig, String subscriptionId, String error);

    void cleanUp(KonnektorConfig konnektorConfig, String subscriptionId);
}
