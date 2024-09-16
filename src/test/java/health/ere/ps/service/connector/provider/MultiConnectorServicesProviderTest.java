package health.ere.ps.service.connector.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import health.ere.ps.config.RuntimeConfig;

public class MultiConnectorServicesProviderTest {
    @Test
    void testGetSignatureServicePortTypeV755() {
        MultiConnectorServicesProvider multiConnectorServicesProvider = new MultiConnectorServicesProvider();
        RuntimeConfig runtimeConfig = new RuntimeConfig("eHBAHandle", "SMCBHandle");
        multiConnectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig);
        runtimeConfig = new RuntimeConfig("eHBAHandle", "SMCBHandle");
        multiConnectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig);
        assertEquals(1, multiConnectorServicesProvider.singleConnectorServicesProvider.size());

    }

    @Test
    void testGetSignatureServicePortTypeV7552() {
        MultiConnectorServicesProvider multiConnectorServicesProvider = new MultiConnectorServicesProvider();
        RuntimeConfig runtimeConfig = new RuntimeConfig("eHBAHandle", "SMCBHandle");
        multiConnectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig);
        runtimeConfig = new RuntimeConfig("adasd", "SMCBHandle");
        multiConnectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig);
        assertEquals(2, multiConnectorServicesProvider.singleConnectorServicesProvider.size());

    }
}
