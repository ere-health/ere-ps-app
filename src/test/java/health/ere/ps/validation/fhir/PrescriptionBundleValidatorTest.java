package health.ere.ps.validation.fhir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ValidationResult;
import health.ere.ps.profile.TitusTestProfile;
import health.ere.ps.service.fhir.XmlPrescriptionProcessor;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
// @Disabled("KBV Validator needs additional configuration to ensure proper validation")
public class PrescriptionBundleValidatorTest {

    FhirContext fhirContext = FhirContext.forR4();

    @Disabled
    @Test
    public void test() throws IOException {
        PrescriptionBundleValidator prescriptionBundleValidator = new PrescriptionBundleValidator();

        Bundle bundle = XmlPrescriptionProcessor.createFixedBundleFromString(Files.readString(Paths.get("/home/manuel/git/secret-test-print-samples/CGM-Turbomed/XML/Bundle1.xml")));

        ValidationResult validationResult = prescriptionBundleValidator.validateResource(bundle,
                true);

        Assertions.assertTrue(validationResult.isSuccessful(), "Ere Health configured sample " +
                "bundle has been successfully validated.");
    }

    @Test
    public void testKBV() throws IOException {
        PrescriptionBundleValidator prescriptionBundleValidator = new PrescriptionBundleValidator();

        List<ValidationResult> validationResults = Files.list(Paths.get("src/test/resources/simplifier_erezept/"))
            .filter(Files::isRegularFile)
            .filter(f -> f.toString().endsWith("0428d416-149e-48a4-977c-394887b3d85c.xml"))
            .map(f -> {
                try {
                    return prescriptionBundleValidator.validateResource(Files.readString(f), true);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            })
            .collect(Collectors.toList());
        Assertions.assertTrue(validationResults.stream().filter(o -> !o.isSuccessful()).count() == 0, "Sample simplifier.net bundle " +
                "has been successfully validated.");
    }

}