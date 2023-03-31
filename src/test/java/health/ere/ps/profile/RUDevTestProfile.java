package health.ere.ps.profile;

import io.quarkus.test.junit.QuarkusTestProfile;

public class RUDevTestProfile implements QuarkusTestProfile {
    @Override
    public String getConfigProfile() { 
        return "RUDev";
    }
}
