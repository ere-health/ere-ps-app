package health.ere.ps.validation.fhir;

import health.ere.ps.service.fhir.XmlPrescriptionProcessor;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PrescriptionBundleValidatorTest {

    @Test
    @Disabled
    public void test() throws IOException {
        PrescriptionBundleValidator prescriptionBundleValidator = new PrescriptionBundleValidator();

        Bundle bundle = XmlPrescriptionProcessor.createFixedBundleFromString(Files.readString(Paths.get("/home/manuel/git/secret-test-print-samples/CGM-Turbomed/XML/Bundle1.xml")));
        prescriptionBundleValidator.validateResource(bundle, true);
    }

    @Test
    public void testKBV() throws IOException {
        PrescriptionBundleValidator prescriptionBundleValidator = new PrescriptionBundleValidator();

        prescriptionBundleValidator.validateResource(Files.readString(Paths.get("src/test/resources/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml")), true);
    }

}
