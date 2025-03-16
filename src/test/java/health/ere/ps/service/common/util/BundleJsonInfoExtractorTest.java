package health.ere.ps.service.common.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;


public class BundleJsonInfoExtractorTest {
    @Test
    public void testExtractDefaultBundleInfoFromBundleJson_bundleResource() throws URISyntaxException, IOException {
        // load BundleJson from resources
        String path = "/bundle-samples/bundleTemplatev2_filled-debug-3.json";
        URL bundleJsonUrl = Objects.requireNonNull(
                getClass().getResource(path),
                "Resource not found: " + path);

        Map<String, String> extractedBundles =
                BundleJsonInfoExtractor.extractDefaultBundleInfoFromBundleJson(
                        Files.readString(Path.of(bundleJsonUrl.toURI()), StandardCharsets.UTF_8));

        assertNotNull(extractedBundles);
        assertEquals(5, extractedBundles.size());
        assertEquals("2021-07-01T08:30:00Z", extractedBundles.get("bundleTimestamp"));
        assertEquals("Stefan", extractedBundles.get("patientFirstName"));
        assertEquals("Odenbach-Wanner", extractedBundles.get("patientLastName"));
        assertEquals("1981-08-21", extractedBundles.get("patientBirthDate"));
        assertEquals("Symbicort Turbohaler 160/4,5 µg/Dosis 60", extractedBundles.get("medicationText"));
    }

    @Test
    public void testExtractDefaultBundleInfoFromBundleJson_bundleDefault() {
        String json = """
                {
                    "timestamp": "2025-01-01T10:30:00Z",
                    "entry": [
                        {},
                        {},
                        {
                            "resource": {
                                "code": {
                                    "text": "Test"
                                }
                            }
                        },
                        {
                            "resource": {
                                "name": [
                                    {
                                        "given": [
                                            "Name"
                                        ],
                                        "family": "Surname"
                                    }
                                ],
                                "birthDate": "2025-01-01"
                            }
                        }
                    ]
                }
                """;

        Map<String, String> extractedBundles =
                BundleJsonInfoExtractor.extractDefaultBundleInfoFromBundleJson(json);

        assertNotNull(extractedBundles);
        assertEquals(5, extractedBundles.size());
        assertEquals("2025-01-01T10:30:00Z", extractedBundles.get("bundleTimestamp"));
        assertEquals("Name", extractedBundles.get("patientFirstName"));
        assertEquals("Surname", extractedBundles.get("patientLastName"));
        assertEquals("2025-01-01", extractedBundles.get("patientBirthDate"));
        assertEquals("Test", extractedBundles.get("medicationText"));
    }

    @Test
    public void testExtractDefaultBundleInfoFromBundleJson_bundleMissingData() {
        String json = """
                {
                    "entry": [
                        {},
                        {},
                        {
                            "resource": {
                                "code": {}
                            }
                        },
                        {
                            "resource": {
                                "name": [
                                    {
                                        "given": []
                                    }
                                ]
                            }
                        }
                    ]
                }
                """;

        Map<String, String> extractedBundles =
                BundleJsonInfoExtractor.extractDefaultBundleInfoFromBundleJson(json);

        assertNotNull(extractedBundles);
        assertEquals(5, extractedBundles.size());
        assertEquals("", extractedBundles.get("bundleTimestamp"));
        assertEquals("", extractedBundles.get("patientFirstName"));
        assertEquals("", extractedBundles.get("patientLastName"));
        assertEquals("", extractedBundles.get("patientBirthDate"));
        assertEquals("", extractedBundles.get("medicationText"));
    }

    @Test
    public void testExtractDefaultBundleInfoFromBundleJson_bundleEmpty() {
        Map<String, String> extractedBundles =
                BundleJsonInfoExtractor.extractDefaultBundleInfoFromBundleJson("{}");

        assertNotNull(extractedBundles);
        assertEquals(1, extractedBundles.size());
        assertEquals("", extractedBundles.get("bundleTimestamp"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    public void testExtractDefaultBundleInfoFromBundleJson_bundleNullOrBlank(String json) {
        Map<String, String> extractedBundles =
                BundleJsonInfoExtractor.extractDefaultBundleInfoFromBundleJson(json);

        assertNotNull(extractedBundles);
        assertTrue(extractedBundles.isEmpty());
    }

    @Test
    public void testExtractDefaultBundleInfoFromBundleJson_bundleInvalidJson() {
        Map<String, String> extractedBundles =
                BundleJsonInfoExtractor.extractDefaultBundleInfoFromBundleJson("{invalid json}");

        assertNotNull(extractedBundles);
        assertTrue(extractedBundles.isEmpty());
    }

    @Test
    public void testExtractDefaultBundleInfoFromBundleJson_bundleInvalidMedicationData() {
        String json = """
                {
                    "timestamp": "2025-01-01T10:30:00Z",
                    "entry": [
                        {},
                        {},
                        {},
                        {
                               "resource": {
                                "name": [
                                    {
                                        "given": [
                                            "Name"
                                        ],
                                        "family": "Surname"
                                    }
                                ],
                                "birthDate": "2025-01-01"
                            }
                        }
                    ]
                }
                """;

        Map<String, String> extractedBundles =
                BundleJsonInfoExtractor.extractDefaultBundleInfoFromBundleJson(json);

        assertNotNull(extractedBundles);
        assertEquals(4, extractedBundles.size());
        assertEquals("2025-01-01T10:30:00Z", extractedBundles.get("bundleTimestamp"));
        assertEquals("Name", extractedBundles.get("patientFirstName"));
        assertEquals("Surname", extractedBundles.get("patientLastName"));
        assertEquals("2025-01-01", extractedBundles.get("patientBirthDate"));
    }

    @Test
    public void testExtractDefaultBundleInfoFromBundleJson_bundleInvalidPatientData() {
        String json = """
                {
                    "timestamp": "2025-01-01T10:30:00Z",
                    "entry": [
                        {},
                        {},
                        {
                            "resource": {
                                "code": {
                                    "text": "Test"
                                }
                            }
                        },
                        {}
                    ]
                }
                """;

        Map<String, String> extractedBundles =
                BundleJsonInfoExtractor.extractDefaultBundleInfoFromBundleJson(json);

        assertNotNull(extractedBundles);
        assertEquals(1, extractedBundles.size());
        assertEquals("2025-01-01T10:30:00Z", extractedBundles.get("bundleTimestamp"));
    }

    @Test
    public void testExtractDefaultBundleInfoFromBundleJson_bundlePartiallyInvalidPatientData() {
        String json = """
                {
                    "timestamp": "2025-01-01T10:30:00Z",
                    "entry": [
                        {},
                        {},
                        {
                            "resource": {}
                        },
                        {
                            "resource": {
                                "name": [
                                    {
                                        "given": [
                                            "Name"
                                        ]
                                    }
                                ]
                            }
                        }
                    ]
                }
                """;

        Map<String, String> extractedBundles =
                BundleJsonInfoExtractor.extractDefaultBundleInfoFromBundleJson(json);

        assertNotNull(extractedBundles);
        assertEquals(4, extractedBundles.size());
        assertEquals("2025-01-01T10:30:00Z", extractedBundles.get("bundleTimestamp"));
        assertEquals("Name", extractedBundles.get("patientFirstName"));
        assertEquals("", extractedBundles.get("patientLastName"));
        assertEquals("", extractedBundles.get("patientBirthDate"));
    }
}
