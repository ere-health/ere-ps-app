package health.ere.ps.profile;

import io.quarkus.test.junit.QuarkusTestProfile;

public class RUTestProfile implements QuarkusTestProfile {
    @Override
    public String getConfigProfile() { 
        return "RU";
    }
}
