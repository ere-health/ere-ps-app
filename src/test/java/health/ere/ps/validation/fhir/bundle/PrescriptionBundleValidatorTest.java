package health.ere.ps.validation.fhir.bundle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import jakarta.json.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.refv.commons.validation.ValidationResult;
import health.ere.ps.profile.TitusTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
public class PrescriptionBundleValidatorTest {

    @Inject
    PrescriptionBundleValidator prescriptionBundleValidator;

    FhirContext fhirContext = FhirContext.forR4();

    private String getValidPrescription1() throws IOException {
        return Files.readString(Paths.get(
                "src/test" +
                        "/resources/examples-kbv-fhir-erp-v1-1-0/Beispiel_1.xml"));
    }

    private String getValidPrescription2() throws IOException {
        return Files.readString(Paths.get(
                "src/test" +
                        "/resources/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml"));
    }

    @Test
    public void testKBV() throws IOException {

        ValidationResult validationResult =
                prescriptionBundleValidator.validateResource(getValidPrescription1(), true);

        Assertions.assertTrue(validationResult.isValid(), "Sample simplifier.net bundle " +
                "has been successfully validated.");

        validationResult =
                prescriptionBundleValidator.validateResource(getValidPrescription2(), true);

        Assertions.assertTrue(validationResult.isValid(), "Sample simplifier.net bundle " +
                "has been successfully validated.");
    }

    @Test
    public void testBundlesValidation() throws IOException {
        String messageId = "message123";
        JsonArrayBuilder bundleBuilder = Json.createArrayBuilder();
        String validResource = getValidPrescription1();
        String invalidResource = "I'm invalid";
        bundleBuilder.add(validResource);
        bundleBuilder.add(invalidResource);

        JsonObject bundlePayload = Json.createObjectBuilder()
                .add("payload", Json.createArrayBuilder().add(bundleBuilder.build()))
                .add("id", "message123").build();

        JsonObject validationResult = prescriptionBundleValidator.bundlesValidationResult(bundlePayload);
        Assertions.assertEquals("BundlesValidationResult", validationResult.getString("type"));
        Assertions.assertEquals(messageId, validationResult.getString("replyToMessageId"));

        JsonArray parsedPayload = validationResult.getJsonArray("payload");
        Assertions.assertEquals(2,parsedPayload.size());

        JsonObject validPayloadItem = parsedPayload.getJsonObject(0);
        Assertions.assertTrue(validPayloadItem.getBoolean("valid"));
        Assertions.assertNull(validPayloadItem.getOrDefault("errors", null));

        JsonObject invalidPayloadItem = parsedPayload.getJsonObject(1);
        Assertions.assertFalse(invalidPayloadItem.getBoolean("valid"));
        Assertions.assertNotNull(invalidPayloadItem.getOrDefault("errors", null));

    }

}
