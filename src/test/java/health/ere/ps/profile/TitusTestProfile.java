package health.ere.ps.profile;

import io.quarkus.test.junit.QuarkusTestProfile;

public class TitusTestProfile implements QuarkusTestProfile {
    @Override
    public String getConfigProfile() { 
        return "titus";
    }
}
