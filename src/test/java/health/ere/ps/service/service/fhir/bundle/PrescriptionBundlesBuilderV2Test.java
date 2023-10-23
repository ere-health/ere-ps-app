package health.ere.ps.service.fhir.bundle;

import java.util.List;

import org.hl7.fhir.dstu2.model.Bundle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import health.ere.ps.model.muster16.MedicationString;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class PrescriptionBundlesBuilderV2Test {

    private PrescriptionBundlesBuilderV2 builder;
    private Muster16PrescriptionForm prescriptionForm;
    private MedicationString medicationString;

    @BeforeAll
    public void setUp() {
        prescriptionForm = new Muster16PrescriptionForm();
        medicationString = new MedicationString(null, null, null, null, null, null);
        builder = new PrescriptionBundlesBuilderV2(prescriptionForm);
    }

    @Test
    public void testCreateBundles() {
        List<org.hl7.fhir.r4.model.Bundle> bundles = builder.createBundles();

        assertNotNull(bundles);
        assertEquals(0, bundles.size()); // No prescription, so no bundles should be created
    }

    @Test
    public void testCreateBundleForMedication() {
        prescriptionForm.getPrescriptionList().add(medicationString);

        org.hl7.fhir.r4.model.Bundle bundle = builder.createBundleForMedication(medicationString);

        assertNotNull(bundle);
        assertEquals(1, bundle.getEntry().size()); // Add specific assertions based on your expectations
    }

    @Test
    public void testUpdateBundleResourceSection() {
        builder.updateBundleResourceSection();

        assertNotNull(builder.getTemplateKeyMapper().get(PrescriptionBundlesBuilderV2.$BUNDLE_ID));
        assertNotNull(builder.getTemplateKeyMapper().get(PrescriptionBundlesBuilderV2.$PRESCRIPTION_ID));

    }

    @Test
    public void testUpdateCompositionSection() {
        builder.updateCompositionSection();

        assertNotNull(builder.getTemplateKeyMapper().get(PrescriptionBundlesBuilderV2.$COMPOSITION_ID));

    }

    @Test
    public void testUpdateMedicationRequestSection() {
        builder.updateMedicationRequestSection();

        assertNotNull(builder.getTemplateKeyMapper().get(PrescriptionBundlesBuilderV2.$MEDICATION_REQUEST_ID));

    }

    @Test
    public void testUpdateMedicationResourceSection() {
        builder.updateMedicationResourceSection(medicationString);

        assertNotNull(builder.getTemplateKeyMapper().get(PrescriptionBundlesBuilderV2.$MEDICATION_ID));

    }

    @Test
    public void testUpdatePatientResourceSection() {
        builder.updatePatientResourceSection();

        assertNotNull(builder.getTemplateKeyMapper().get(PrescriptionBundlesBuilderV2.$PATIENT_ID));

    }

    @Test
    public void testUpdatePractitionerResourceSection() {
        builder.updatePractitionerResourceSection();

        assertNotNull(builder.getTemplateKeyMapper().get(PrescriptionBundlesBuilderV2.$PRACTITIONER_ID));

    }

    @Test
    public void testUpdateOrganizationResourceSection() {
        builder.updateOrganizationResourceSection();

        assertNotNull(builder.getTemplateKeyMapper().get(PrescriptionBundlesBuilderV2.$ORGANIZATION_ID));

    }

    @Test
    public void testUpdateCoverageResourceSection() {
        builder.updateCoverageResourceSection();

        assertNotNull(builder.getTemplateKeyMapper().get(PrescriptionBundlesBuilderV2.$COVERAGE_ID));

    }
}
