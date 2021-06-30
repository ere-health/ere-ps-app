package health.ere.ps.test;

import io.quarkus.test.junit.QuarkusTestProfile;

public class DefaultTestProfile implements QuarkusTestProfile {
    @Override
    public String getConfigProfile() { 
        return "dev";
    }
}
