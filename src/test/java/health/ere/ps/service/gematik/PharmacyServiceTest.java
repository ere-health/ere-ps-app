package health.ere.ps.service.gematik;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.profile.RUTestProfile;
import health.ere.ps.service.connector.endpoint.SSLUtilities;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestProfile(RUTestProfile.class)
@Disabled
public class PharmacyServiceTest {

    private static final Logger log = Logger.getLogger(ERezeptWorkflowServiceTest.class.getName());

    @Inject
    PharmacyService pharmacyService;

    @Inject
    UserConfig userConfig;

    @BeforeEach
    void init() {
        try {
            // https://community.oracle.com/thread/1307033?start=0&tstart=0
            LogManager.getLogManager().readConfiguration(
                    ERezeptWorkflowServiceTest.class
                            .getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            log.severe(e.getMessage());
        }

        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dumpTreshold", "999999");

        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();
    }

    @Test
    void testGetEPrescriptionsForCardHandle() {
        RuntimeConfig runtimeConfig = new RuntimeConfig();
        runtimeConfig.copyValuesFromUserConfig(userConfig);

        WebApplicationException exception = assertThrows(WebApplicationException.class, () ->
            pharmacyService.getEPrescriptionsForCardHandle(null, null, runtimeConfig)
        );
        assertTrue(exception.getMessage().contains("endpoint is forbidden for professionOID 1.2.276.0.76.4.50"));
    }
}
