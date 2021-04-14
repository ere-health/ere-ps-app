package com.erehealth.ps.service.fhir.bundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.erehealth.ps.model.muster16.Muster16PrescriptionForm;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Arrays;

class PrescriptionBundleBuilderTest {
    @Test
    public void testSuccessful_Creation_of_FHIR_EPrescription_Bundle_From_Muster16_Model_Object()
            throws ParseException {
        Muster16PrescriptionForm muster16PrescriptionForm = new Muster16PrescriptionForm();

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
}