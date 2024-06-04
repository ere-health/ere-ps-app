package health.ere.ps.service.health;

import java.util.List;

public record HealthInfo(String status, List<CheckInfo> checks) {
}
