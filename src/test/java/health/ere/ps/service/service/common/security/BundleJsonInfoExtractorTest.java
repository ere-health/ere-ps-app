package health.ere.ps.service.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mysql.cj.util.TestUtils;

import health.ere.ps.service.common.util.BundleJsonInfoExtractor;

import static org.mockito.Mockito.*;

public class BundleJsonInfoExtractorTest {

    @Mock
    private Logger logger;

    @Test
    public void testExtractDefaultBundleInfoFromValidBundleJson() {
        String validBundleJson = "{"
            + "\"timestamp\":\"2023-10-23T12:00:00Z\","
            + "\"entry\":[{},{},"
            + "{\"resource\":{\"code\":{\"text\":\"Sample Medication Text\"}}},"
            + "{\"resource\":{\"name\":[{\"given\":[\"John\"],\"family\":\"Doe\"}],\"birthDate\":\"1980-01-01\"}}"
            + "]"
            + "}";

        Map<String, String> result = BundleJsonInfoExtractor.extractDefaultBundleInfoFromBundleJson(validBundleJson);

        assertEquals("2023-10-23T12:00:00Z", result.get("bundleTimestamp"));
        assertEquals("John", result.get("patientFirstName"));
        assertEquals("Doe", result.get("patientLastName"));
        assertEquals("1980-01-01", result.get("patientBirthDate"));
        assertEquals("Sample Medication Text", result.get("medicationText"));
    }

    @Test
    public void testExtractDefaultBundleInfoFromIncompleteBundleJson() {
        String incompleteBundleJson = "{"
            + "\"timestamp\":\"2023-10-23T12:00:00Z\","
            + "\"entry\":[{},{},"
            + "{\"resource\":{\"code\":{}}},"
            + "{\"resource\":{\"name\":[{\"given\":[\"John\"]}],\"birthDate\":\"1980-01-01\"}}"
            + "]"
            + "}";

        Map<String, String> result = BundleJsonInfoExtractor.extractDefaultBundleInfoFromBundleJson(incompleteBundleJson);

        assertEquals("2023-10-23T12:00:00Z", result.get("bundleTimestamp"));
        assertEquals("John", result.get("patientFirstName"));
        assertTrue(result.get("patientLastName").isEmpty());
        assertEquals("1980-01-01", result.get("patientBirthDate"));
        assertTrue(result.get("medicationText").isEmpty());
    }

    @Test
    public void testExtractDefaultBundleInfoFromEmptyBundleJson() {
        String emptyBundleJson = "";

        Map<String, String> result = BundleJsonInfoExtractor.extractDefaultBundleInfoFromBundleJson(emptyBundleJson);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testExtractDefaultBundleInfoFromInvalidBundleJson() {
        String invalidBundleJson = "{"
            + "\"timestamp\":\"2023-10-23T12:00:00Z\","
            + "\"entry\":\"invalid value\""
            + "}";

        Map<String, String> result = BundleJsonInfoExtractor.extractDefaultBundleInfoFromBundleJson(invalidBundleJson);

        assertTrue(result.isEmpty());
        verify(logger).error(anyString(), any(Throwable.class));
    }
}

// Note: 'TestUtils.setStaticMock()' is a hypothetical utility function to mock static fields.
