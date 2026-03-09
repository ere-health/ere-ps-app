package health.ere.ps.service.connector.provider;

import health.ere.ps.config.RuntimeConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
public class MultiConnectorServicesProviderTest {

    @Test
    void testGetSignatureServicePortTypeV755() {
        MultiConnectorServicesProvider multiConnectorServicesProvider = new MultiConnectorServicesProvider();
        RuntimeConfig runtimeConfig = new RuntimeConfig("eHBAHandle", "SMCBHandle");
        multiConnectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig);
        runtimeConfig = new RuntimeConfig("eHBAHandle", "SMCBHandle");
        multiConnectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig);
        assertEquals(1, multiConnectorServicesProvider.portMap.size());
    }

    @Test
    void testGetSignatureServicePortTypeV7552() {
        MultiConnectorServicesProvider multiConnectorServicesProvider = new MultiConnectorServicesProvider();
        RuntimeConfig runtimeConfig = new RuntimeConfig("eHBAHandle", "SMCBHandle");
        multiConnectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig);
        runtimeConfig = new RuntimeConfig("adasd", "SMCBHandle");
        multiConnectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig);
        assertEquals(2, multiConnectorServicesProvider.portMap.size());
    }
}
