package health.ere.ps.resource.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.profile.TitusTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
public class UserConfigurationsResourceTest {

    @Inject
    UserConfigurationsResource userConfigurationsResource;

    @Test
    public void shouldUpdateAndReturnConfig() throws FileNotFoundException { 
        
        UserConfigurations newConfig = new UserConfigurations();
        
        newConfig.setBasicAuthPassword("testBasicAuthPassword");
        newConfig.setBasicAuthUsername("testBasicAuthUsername");
        newConfig.setClientCertificate("testClientCertificate");
        newConfig.setClientCertificatePassword("testClientCertificatePassword");
        newConfig.setClientSystemId("testClientSystemId");
        newConfig.setConnectorBaseURL("testConnectorBaseURL");
        newConfig.setErixaApiKey("testErixaApiKey");
        newConfig.setErixaDrugstoreEmail("testErixaDrugstoreEmail");
        newConfig.setErixaHotfolder("testErixaHotfolder");
        newConfig.setErixaUserEmail("testErixaUserEmail");
        newConfig.setErixaUserPassword("testErixaUserPassword");
        newConfig.setMandantId("testMandantId");
        newConfig.setMuster16TemplateProfile("testMuster16TemplateProfile");
        newConfig.setPruefnummer("testPruefnummer");
        newConfig.setTvMode("testTvMode");
        newConfig.setUserId("testUserId");
        newConfig.setVersion("testVersion");
        newConfig.setWorkplaceId("testWorkplaceId");

        assertTrue(userConfigurationsResource.updateConfigurations(newConfig).getStatus() == 200);

        UserConfigurations configResponse = (UserConfigurations)userConfigurationsResource.getConfigurations().getEntity();
        
        assertTrue(configResponse.properties().equals(newConfig.properties()));

        // TODO configure proper file path according to getConfigFilePath() in UserConfigurationService
        new PrintWriter("user.properties").close();
        
    }
    
}
