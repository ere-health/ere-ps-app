package health.ere.ps;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class LocalOfflineQuarkusTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("digital-green-certificate-service.issuerAPIUrl", "http://localhost:8123/issue");
    }
}
