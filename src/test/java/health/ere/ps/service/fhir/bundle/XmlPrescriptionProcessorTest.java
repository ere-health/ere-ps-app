package health.ere.ps.service.fhir.bundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.nio.file.Files;

import health.ere.ps.service.fhir.XmlPrescriptionProcessor;

public class XmlPrescriptionProcessorTest {

    @Test
    @Disabled("This test case needs unpublished data")
    public void test() {
        String xmlBundle;
        try {
            xmlBundle = new String(Files.readAllBytes(Paths.get("../secret-test-print-samples/CGM-Turbomed/XML/Kaiser_Bella_20210630113252.xml")));
            
            Bundle[] bundle = XmlPrescriptionProcessor.parseFromString(xmlBundle);
            assertEquals(3, bundle.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
