package health.ere.ps.service.connector.endpoint;

import java.util.logging.Logger;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import health.ere.ps.profile.TitusTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
public class EndpointDiscoveryServiceTest {

    @Inject
    EndpointDiscoveryService endpointDiscoveryService;

    private final Logger log = Logger.getLogger(EndpointDiscoveryService.class.getName());

    @Test
    void test_getEndpointURLs(){
        log.info(endpointDiscoveryService.getEventServiceEndpointAddress());
        log.info(endpointDiscoveryService.getCertificateServiceEndpointAddress());
        log.info(endpointDiscoveryService.getSignatureServiceEndpointAddress());
        log.info(endpointDiscoveryService.getAuthSignatureServiceEndpointAddress());
    }
}
