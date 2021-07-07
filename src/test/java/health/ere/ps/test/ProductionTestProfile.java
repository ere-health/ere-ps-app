package health.ere.ps.test;

import io.quarkus.test.junit.QuarkusTestProfile;

public class ProductionTestProfile implements QuarkusTestProfile {
    @Override
    public String getConfigProfile() { 
        return "prod";
    }
}
