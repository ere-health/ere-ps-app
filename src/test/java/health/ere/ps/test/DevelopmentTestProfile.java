package health.ere.ps.test;

import io.quarkus.test.junit.QuarkusTestProfile;

public class DevelopmentTestProfile implements QuarkusTestProfile {
    @Override
    public String getConfigProfile() { 
        return "dev";
    }
}
