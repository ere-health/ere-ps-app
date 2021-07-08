package health.ere.ps.profile;

import io.quarkus.test.junit.QuarkusTestProfile;

public class DevelopmentTestProfile implements QuarkusTestProfile {
    @Override
    public String getConfigProfile() { 
        return "dev";
    }
}
