package health.ere.ps.service.health.check;

import health.ere.ps.config.RuntimeConfig;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@ApplicationScoped
public class CardlinkWebsocketCheck implements Check {

    private boolean connected;

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public String getName() {
        return CARDLINK_WEBSOCKET_CHECK;
    }

    @Override
    public Status getStatus(RuntimeConfig runtimeConfig) {
        return connected ? Status.Up200 : Status.Down503;
    }

    @Override
    public Map<String, String> getData(RuntimeConfig runtimeConfig) {
        return Map.of("CardlinkWebsocket", connected ? "CONNECTED" : "DISCONNECTED");
    }
}
