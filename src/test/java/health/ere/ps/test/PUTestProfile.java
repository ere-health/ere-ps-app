package health.ere.ps.test;

import io.quarkus.test.junit.QuarkusTestProfile;

public class PUTestProfile implements QuarkusTestProfile {
    @Override
    public String getConfigProfile() { 
        return "PU";
    }
}
