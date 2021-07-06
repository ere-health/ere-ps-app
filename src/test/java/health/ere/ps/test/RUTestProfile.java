package health.ere.ps.test;

import io.quarkus.test.junit.QuarkusTestProfile;

public class RUTestProfile implements QuarkusTestProfile {
    @Override
    public String getConfigProfile() { 
        return "RU";
    }
}
