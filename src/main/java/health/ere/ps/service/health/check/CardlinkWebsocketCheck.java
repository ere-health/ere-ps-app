package health.ere.ps.service.health.check;

import de.health.service.check.Check;
import de.health.service.check.Status;
import de.health.service.config.api.IRuntimeConfig;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ApplicationScoped
public class CardlinkWebsocketCheck implements Check {

    private final Map<URI, Supplier<Boolean>> wsConnections = new HashMap<>();

    public void register(URI uri, Supplier<Boolean> connected) {
        wsConnections.put(uri, connected);
    }

    @Override
    public String getName() {
        return CARDLINK_WEBSOCKET_CHECK;
    }

    @Override
    public Status getStatus(IRuntimeConfig runtimeConfig) {
        return wsConnections.values().stream().allMatch(Supplier::get) ? Status.Up200 : Status.Down503;
    }

    @Override
    public Map<String, Object> getData(IRuntimeConfig runtimeConfig) {
        return wsConnections.entrySet().stream().collect(
            Collectors.toMap(
                e -> String.format("CardlinkWebsocket -> %s", e.getKey()),
                e -> e.getValue().get() ? "CONNECTED" : "DISCONNECTED"
            )
        );
    }
}
