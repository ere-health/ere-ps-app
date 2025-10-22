package health.ere.ps.service.fhir.bundle;

import health.ere.ps.service.fhir.prescription.AppPrescriptionProcessor;
import health.ere.ps.service.fhir.prescription.MedicationPrescriptionProcessor;
import health.ere.ps.service.fhir.prescription.PrescriptionService;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XmlPrescriptionProcessorTest {

    @Test
    @Disabled("This test case needs unpublished data")
    public void test() {
        String xmlBundle;
        try {
            xmlBundle = new String(Files.readAllBytes(Paths.get("../secret-test-print-samples/CGM-Turbomed/XML/Kaiser_Bella_20210630113252.xml")));

            PrescriptionService prescriptionService = new PrescriptionService(
                new AppPrescriptionProcessor(),
                new MedicationPrescriptionProcessor()
            );
            Bundle[] bundle = prescriptionService.parseFromString(xmlBundle);
            assertEquals(3, bundle.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
