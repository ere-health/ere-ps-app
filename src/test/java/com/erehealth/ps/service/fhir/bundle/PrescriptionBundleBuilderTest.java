package com.erehealth.ps.service.fhir.bundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.erehealth.ps.model.muster16.Muster16PrescriptionForm;

import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Arrays;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;

class PrescriptionBundleBuilderTest {
    private Muster16PrescriptionForm muster16PrescriptionForm;

    @BeforeEach
    public void initializeDefaultMuster16FormModelObject() {
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
    }

    @Test
    public void test_Successful_Creation_of_FHIR_EPrescription_Bundle_From_Muster16_Model_Object()
            throws ParseException {

        PrescriptionBundleBuilder prescriptionBundleBuilder =
                new PrescriptionBundleBuilder(muster16PrescriptionForm);

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

        PrescriptionBundleBuilder prescriptionBundleBuilder =
                new PrescriptionBundleBuilder(muster16PrescriptionForm);

        Bundle fhirEPrescriptionBundle = prescriptionBundleBuilder.createBundle();

        fhirEPrescriptionBundle.setId("sample-id-from-gematik-ti-123456");

        parser.setPrettyPrint(true);

        String serialized = parser.encodeResourceToString(fhirEPrescriptionBundle);

        System.out.println(serialized);
    }

    @Test
    public void test_Successful_JSON_Serialization_Of_An_FHIR_EPrescription_Bundle_Object()
            throws ParseException {
        FhirContext ctx = FhirContext.forR4();

        IParser parser = ctx.newJsonParser();

        PrescriptionBundleBuilder prescriptionBundleBuilder =
                new PrescriptionBundleBuilder(muster16PrescriptionForm);

        Bundle fhirEPrescriptionBundle = prescriptionBundleBuilder.createBundle();

        fhirEPrescriptionBundle.setId("sample-id-from-gematik-ti-123456");

        parser.setPrettyPrint(true);

        String serialized = parser.encodeResourceToString(fhirEPrescriptionBundle);

        System.out.println(serialized);
    }

    public void test_Successful_Validation_Of_XML_Serialization_FHIR_EPrescription_Bundle_Object()
            throws ParseException {
        FhirContext ctx = FhirContext.forR4();

        IParser parser = ctx.newJsonParser();

        PrescriptionBundleBuilder prescriptionBundleBuilder =
                new PrescriptionBundleBuilder(muster16PrescriptionForm);

        Bundle fhirEPrescriptionBundle = prescriptionBundleBuilder.createBundle();

        fhirEPrescriptionBundle.setId("sample-id-from-gematik-ti-123456");

        String resourceText = parser.encodeResourceToString(fhirEPrescriptionBundle);

        // Create a validation support chain
        ValidationSupportChain validationSupportChain = new ValidationSupportChain(
                new DefaultProfileValidationSupport(ctx),
                new InMemoryTerminologyServerValidationSupport(ctx),
                new CommonCodeSystemsTerminologyService(ctx)
        );

        // Create a FhirInstanceValidator and register it to a validator
        FhirValidator validator = ctx.newValidator();
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator(validationSupportChain);
        validator.registerValidatorModule(instanceValidator);

        ValidationResult validationResult = validator.validateWithResult(resourceText);

        if(!validationResult.isSuccessful()) {
            // Show the issues
            for (SingleValidationMessage next : validationResult.getMessages()) {
                System.out.println(" Next issue " + next.getSeverity() + " - " + next.getLocationString() + " - " + next.getMessage());
            }
        }

        //TODO: Verify Gematik FHIR E-Prescription Bundle spec and cross reference with standard
        // FHIR resource format requirements to fix validation errors.
        assertTrue(validationResult.isSuccessful());
    }
}