package health.ere.ps.service.health;

import java.util.Map;

public record CheckInfo(String name, String status, Map<String, String> data) {
}
