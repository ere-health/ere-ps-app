package health.ere.ps.service.connector.endpoint;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.logging.Logger;

@QuarkusTest
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
