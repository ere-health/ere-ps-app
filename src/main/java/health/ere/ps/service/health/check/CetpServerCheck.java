package health.ere.ps.service.health.check;

import de.health.service.cetp.CETPServer;
import health.ere.ps.config.RuntimeConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;

@ApplicationScoped
public class CetpServerCheck implements Check {

    @Inject
    CETPServer cetpServer;

    @Override
    public String getName() {
        return CETP_SERVER_CHECK;
    }

    @Override
    public Status getStatus(RuntimeConfig runtimeConfig) {
        Map<String, String> startedOnPorts = cetpServer.getStartedOnPorts();
        boolean someFailed = startedOnPorts.values().stream().anyMatch(s -> s.startsWith("FAILED"));
        return someFailed || startedOnPorts.isEmpty() ? Status.Down503 : Status.Up200;
    }

    @Override
    public Map<String, String> getData(RuntimeConfig runtimeConfig) {
        return cetpServer.getStartedOnPorts();
    }
}
