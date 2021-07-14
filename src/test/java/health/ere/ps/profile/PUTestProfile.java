package health.ere.ps.profile;

import io.quarkus.test.junit.QuarkusTestProfile;

public class PUTestProfile implements QuarkusTestProfile {
    @Override
    public String getConfigProfile() { 
        return "RU";
    }
}
