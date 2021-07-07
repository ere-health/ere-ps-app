package health.ere.ps.test;

import io.quarkus.test.junit.QuarkusTestProfile;

public class StagingTestProfile implements QuarkusTestProfile {
    @Override
    public String getConfigProfile() { 
        return "staging";
    }
}
