package health.ere.ps.service.fhir.bundle;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import health.ere.ps.model.muster16.MedicationString;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

public class IBundlesBuilderTest {

    private YourBundlesBuilderImplementation bundlesBuilder;

    @BeforeAll
    public void setUp() {
        // Initialize the IBundlesBuilder implementation (you need to provide the implementation)
        bundlesBuilder = new YourBundlesBuilderImplementation();
    }

    @Test
    public void testCreateBundles() {
        List<Bundle> bundles = bundlesBuilder.createBundles();
        assertNotNull(bundles);
        // Add more assertions based on your implementation and expected behavior
    }

    @Test
    public void testCreateBundleForMedication() {
        MedicationString medication = new MedicationString(null, null, null, null, null, null); // Create a MedicationString object
        Bundle bundle = bundlesBuilder.createBundleForMedication(medication);
        assertNotNull(bundle);
        // Add more assertions based on your implementation and expected behavior
    }
}
