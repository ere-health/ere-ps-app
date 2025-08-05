package health.ere.ps.service.health.check;

import health.ere.ps.config.RuntimeConfig;

import java.util.Map;

public interface Check {

    String CARDLINK_WEBSOCKET_CHECK = "CardlinkWebsocketCheck";
    String CETP_SERVER_CHECK = "CETPServerCheck";
    String STATUS_CHECK = "StatusCheck";
    String GIT_CHECK = "GitCheck";

    String getName();

    Status getStatus(RuntimeConfig runtimeConfig);

    default Status getSafeStatus(RuntimeConfig runtimeConfig) {
        try {
            return getStatus(runtimeConfig);
        } catch (Throwable t) {
            return Status.Down500;
        }
    }

    Map<String, String> getData(RuntimeConfig runtimeConfig);
}