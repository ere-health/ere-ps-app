package health.ere.ps.service.fhir.bundle;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.ValidationResult;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrescriptionBundleBuilderTest {
    private PrescriptionBundleValidator prescriptionBundleValidator;
    private Muster16PrescriptionForm muster16PrescriptionForm;
    private PrescriptionBundleBuilder prescriptionBundleBuilder;

    @BeforeEach
    public void initialize() {
        muster16PrescriptionForm = new Muster16PrescriptionForm();

        muster16PrescriptionForm.setClinicId("BS12345678");

        muster16PrescriptionForm.setPrescriptionDate("05.04.2021");
        muster16PrescriptionForm.setPrescriptionList(Arrays.asList("Amoxicillin 1000mg N2",
                "3x täglich alle 8 Std"));

        muster16PrescriptionForm.setDoctorId("LANR1234");

        muster16PrescriptionForm.setInsuranceCompany("Test Insurance Company, Gmbh");

        muster16PrescriptionForm.setPatientDateOfBirth("16.07.1986");
        muster16PrescriptionForm.setPatientFirstName("John");
        muster16PrescriptionForm.setPatientLastName("Doe");
        muster16PrescriptionForm.setPatientStreetName("Droysenstr.");
        muster16PrescriptionForm.setPatientStreetNumber("7");
        muster16PrescriptionForm.setPatientZipCode("10629");
        muster16PrescriptionForm.setPatientCity("Berlin");
        muster16PrescriptionForm.setPatientInsuranceId("123456789");

        muster16PrescriptionForm.setInsuranceCompanyId("100038825");

        prescriptionBundleBuilder =
                new PrescriptionBundleBuilder(muster16PrescriptionForm);

        prescriptionBundleValidator = new PrescriptionBundleValidator();
    }

    @Test
    public void test_Successful_Creation_of_FHIR_EPrescription_Bundle_From_Muster16_Model_Object()
            throws ParseException {

        Bundle fhirEPrescriptionBundle = prescriptionBundleBuilder.createBundle();

        // Expecting the creation of 7 resources
        // 1. composition resource
        // 2. medication request resource
        // 3. medication resource.
        // 4. patient resource.
        // 5. practitioner resource.
        // 6. organization resource.
        // 7. coverage resource.
        assertEquals(7, fhirEPrescriptionBundle.getEntry().size());
    }

    @Test
    public void test_Successful_XML_Serialization_Of_An_FHIR_EPrescription_Bundle_Object()
            throws ParseException {
        FhirContext ctx = FhirContext.forR4();

        IParser parser = ctx.newXmlParser();

        Bundle fhirEPrescriptionBundle = prescriptionBundleBuilder.createBundle();

        fhirEPrescriptionBundle.setId("sample-id-from-gematik-ti-123456");

        parser.setPrettyPrint(true);

        String serialized = parser.encodeResourceToString(fhirEPrescriptionBundle);
    }

    @Test
    public void test_Successful_JSON_Serialization_Of_An_FHIR_EPrescription_Bundle_Object()
            throws ParseException {
        FhirContext ctx = FhirContext.forR4();

        IParser parser = ctx.newJsonParser();

        Bundle fhirEPrescriptionBundle = prescriptionBundleBuilder.createBundle();

        fhirEPrescriptionBundle.setId("sample-id-from-gematik-ti-123456");

        parser.setPrettyPrint(true);

        String serialized = parser.encodeResourceToString(fhirEPrescriptionBundle);
    }

    @Test
    public void test_Successful_Validation_Of_An_FHIR_Patient_Resource()
            throws ParseException {
        Patient patientResource = prescriptionBundleBuilder.createPatientResource();

        ValidationResult validationResult =
                prescriptionBundleValidator.validateResource(patientResource, true);
        assertTrue(validationResult.isSuccessful());
    }

    @Test
    public void test_Validation_Failure_Of_FHIR_Patient_Resource_With_Missing_Content() {
        Patient patient = new Patient();

        ValidationResult validationResult =
                prescriptionBundleValidator.validateResource(patient, true);
        assertFalse(validationResult.isSuccessful());
    }

    @Disabled
    @Test
    public void test_Successful_Validation_Of_An_FHIR_Coverage_Resource()
            throws ParseException {
        Coverage coverageResource = prescriptionBundleBuilder.createCoverageResource();

        ValidationResult validationResult =
                prescriptionBundleValidator.validateResource(coverageResource, true);
        assertTrue(validationResult.isSuccessful());
    }

    @Disabled
    @Test
    public void test_Successful_Validation_Of_XML_Serialization_Of_FHIR_EPrescription_Bundle_Object()
            throws ParseException {
        Bundle prescriptionBundle = prescriptionBundleBuilder.createBundle();

        ValidationResult validationResult =
                prescriptionBundleValidator.validateResource(prescriptionBundle, true);
        assertTrue(validationResult.isSuccessful());
    }
}