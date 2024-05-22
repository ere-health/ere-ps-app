package health.ere.ps.validation.fhir.bundle;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.refv.commons.validation.ValidationResult;
import health.ere.ps.profile.TitusTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
public class PrescriptionBundleValidatorTest {

    @Inject
    PrescriptionBundleValidator prescriptionBundleValidator;

    FhirContext fhirContext = FhirContext.forR4();

    @Test
    public void testKBV() throws IOException {

        ValidationResult validationResult =
                prescriptionBundleValidator.validateResource(Files.readString(Paths.get(
                "./src/test" +
                "/resources/examples-kbv-fhir-erp-v1-1-0/Beispiel_1.xml")), true);

        Assertions.assertTrue(validationResult.isValid(), "Sample simplifier.net bundle " +
                "has been successfully validated.");

        validationResult =
                prescriptionBundleValidator.validateResource(Files.readString(Paths.get(
                "./src/test" +
                "/resources/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml")), true);

        Assertions.assertTrue(validationResult.isValid(), "Sample simplifier.net bundle " +
                "has been successfully validated.");
    }

}
