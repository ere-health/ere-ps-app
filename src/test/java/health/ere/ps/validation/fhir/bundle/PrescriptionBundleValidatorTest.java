package health.ere.ps.validation.fhir.bundle;

import de.gematik.refv.commons.validation.ValidationResult;
import health.ere.ps.profile.TitusTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
public class PrescriptionBundleValidatorTest {

    @Inject
    PrescriptionBundleValidator prescriptionBundleValidator;

    @Test
    public void testKBV() throws Exception {
        assertValidBundle("./src/test/resources/examples-kbv-fhir-erp-v1-1-0/Beispiel_1.xml");
        assertValidBundle("./src/test/resources/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml");
        assertValidBundle("./src/test/resources/examples-kbv-fhir-erp-v1-3-2/erp111.xml");
    }

    private void assertValidBundle(String fileName) throws Exception {
        String fileContent = Files.readString(Paths.get(fileName));
        ValidationResult validationResult = prescriptionBundleValidator.validateResource(fileContent, true, new ArrayList<>());
        Assertions.assertTrue(validationResult.isValid(), "Sample simplifier.net bundle has been successfully validated");
    }
}
