package health.ere.ps.service.health;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.health.check.Check;
import health.ere.ps.service.health.check.Status;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class HealthChecker {

    @Inject
    @Any
    Instance<Check> checks;

    public HealthInfo getHealthInfo(RuntimeConfig runtimeConfig) {
        boolean anyChecks500 = checks.stream()
            .map(check -> check.getSafeStatus(runtimeConfig))
            .anyMatch(status -> status.equals(Status.Down500));
        
        return new HealthInfo(
            anyChecks500 ? "DOWN" : "UP",
            checks.stream().map(check -> {
                Status status;
                Map<String, String> data = new HashMap<>();
                try {
                    data = check.getData(runtimeConfig);
                    status = check.getStatus(runtimeConfig);
                } catch (Throwable t) {
                    status = Status.Down500;
                    data.clear();
                    data.put("rootCause", t.getMessage());
                }
                return new CheckInfo(check.getName(), status.toSingleStatusString(), data);
            }).toList()
        );
    }
}