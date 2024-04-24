package health.ere.ps.validation.fhir.bundle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import jakarta.inject.Inject;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ValidationResult;
import health.ere.ps.profile.TitusTestProfile;
import health.ere.ps.service.fhir.XmlPrescriptionProcessor;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
public class PrescriptionBundleValidatorTest {

    @Inject
    PrescriptionBundleValidator prescriptionBundleValidator;

    FhirContext fhirContext = FhirContext.forR4();

    @Disabled
    @Test
    public void test() throws IOException {

        Bundle bundle = XmlPrescriptionProcessor.createFixedBundleFromString(Files.readString(Paths.get("/home/manuel/git/secret-test-print-samples/CGM-Turbomed/XML/Bundle1.xml")));

        ValidationResult validationResult = prescriptionBundleValidator.validateResource(bundle,
                true);

        Assertions.assertTrue(validationResult.isSuccessful(), "Ere Health configured sample " +
                "bundle has been successfully validated.");
    }

    @Test
    public void testKBV() throws IOException {

        ValidationResult validationResult =
                prescriptionBundleValidator.validateResource(Files.readString(Paths.get(
                "src/test" +
                "/resources/examples-kbv-fhir-erp-v1-1-0/Beispiel_1.xml")), true);

        Assertions.assertTrue(validationResult.isSuccessful(), "Sample simplifier.net bundle " +
                "has been successfully validated.");

        validationResult =
                prescriptionBundleValidator.validateResource(Files.readString(Paths.get(
                "src/test" +
                "/resources/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml")), true);

        Assertions.assertTrue(validationResult.isSuccessful(), "Sample simplifier.net bundle " +
                "has been successfully validated.");
    }

}
