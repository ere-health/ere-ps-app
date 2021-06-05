package health.ere.ps.service.fhir.bundle;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Patient;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.inject.Inject;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.ValidationResult;
import health.ere.ps.model.muster16.MedicationString;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class PrescriptionBundleBuilderTest {
    @Inject
    Logger logger;
    private PrescriptionBundleValidator prescriptionBundleValidator;
    private PrescriptionBundleBuilder prescriptionBundleBuilder;

    public static PrescriptionBundleBuilder getPrescriptionBundleBuilder() {
        Muster16PrescriptionForm muster16PrescriptionForm;
        muster16PrescriptionForm = new Muster16PrescriptionForm();

        muster16PrescriptionForm.setClinicId("BS12345678");

        muster16PrescriptionForm.setPrescriptionDate("05.04.2021");
        MedicationString medicationString = new MedicationString("Amoxicillin 1000mg N2", null, null, "3x täglich alle 8 Std", null, "2394428");

        muster16PrescriptionForm.setPrescriptionList(Arrays.asList(medicationString));

        muster16PrescriptionForm.setDoctorId("LANR1234");

        muster16PrescriptionForm.setInsuranceCompany("Test Insurance Company, Gmbh");

        muster16PrescriptionForm.setPatientDateOfBirth("16.07.1986");
        muster16PrescriptionForm.setPatientFirstName("John");
        muster16PrescriptionForm.setPatientLastName("Doe");
        muster16PrescriptionForm.setPatientStreetName("Droysenstr.");
        muster16PrescriptionForm.setPatientStreetNumber("7");
        muster16PrescriptionForm.setPatientZipCode("10629");
        muster16PrescriptionForm.setPatientCity("Berlin");
        muster16PrescriptionForm.setPatientInsuranceId("M310119800");

        muster16PrescriptionForm.setDoctorNamePrefix("Dr.");
        muster16PrescriptionForm.setDoctorFirstName("Testarzt");
        muster16PrescriptionForm.setDoctorLastName("E-Rezept");
        muster16PrescriptionForm.setDoctorPhone("123456789");

        muster16PrescriptionForm.setDoctorStreetName("Doc Droysenstr.");
        muster16PrescriptionForm.setDoctorStreetNumber("7a");
        muster16PrescriptionForm.setDoctorZipCode("10630");
        muster16PrescriptionForm.setDoctorCity("Berlinn");

        muster16PrescriptionForm.setDoctorPhone("030/123456");

        muster16PrescriptionForm.setInsuranceCompanyId("100038825");

        return
                new PrescriptionBundleBuilder(muster16PrescriptionForm);
    }

    @BeforeEach
    public void initialize() throws URISyntaxException {
        prescriptionBundleBuilder = getPrescriptionBundleBuilder();
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

        logger.info(serialized);
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

        logger.info(serialized);
    }

    @Disabled
    @Test
    public void test_Successful_Validation_Of_An_FHIR_Patient_Resource()
            throws ParseException {
        Patient patientResource = prescriptionBundleBuilder.createPatientResource();

        ValidationResult validationResult =
                prescriptionBundleValidator.validateResource(patientResource, true);
        System.out.println(validationResult.getMessages().stream().map(m -> m.getMessage()).collect(Collectors.joining("\n")));

        // Solutions for configuring HAPI validator can be found in a gematik presentation
        // https://gematik.atlassian.net/plugins/servlet/servicedesk/customer/confluence/shim/download/attachments/620855297/20210517%20-%20Sprechstunde%20eRP.pptx?version=1&modificationDate=1621431687594&cacheVersion=1&api=v2
        /*
        https://hapifhir.io/hapi-fhir/docs/tools/hapi_fhir_cli.html

        internalValidator = new FhirInstanceValidator(fhirContext);
        ValidationSupportChain support = new ValidationSupportChain(
                new DefaultProfileValidationSupport(fhirContext),
                new InMemoryTerminologyServerValidationSupport(fhirContext),
                new SnapshotGeneratingValidationSupport(fhirContext),
                new FhirSupport()
        );
        internalValidator.setValidationSupport(support);
        internalValidator.setNoTerminologyChecks(false);
        internalValidator.setAssumeValidRestReferences(false);
        internalValidator.setBestPracticeWarningLevel(IResourceValidator.BestPracticeWarningLevel.Hint);
        validator.registerValidatorModule(internalValidator);
        */
        // TODO: Next issue WARNING - Patient.identifier[0].type - None of the codes provided are in the value set http://hl7.org/fhir/ValueSet/identifier-type (http://hl7.org/fhir/ValueSet/identifier-type), and a code should come from this value set unless it has no suitable code and the validator cannot judge what is suitable) (codes = http://fhir.de/CodeSystem/identifier-type-de-basis#GKV)
        // TODO: Next issue ERROR - Patient.meta.profile[0] - Profile reference 'https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3' has not been checked because it is unknown
        // TODO: None of the codes provided are in the value set http://hl7.org/fhir/ValueSet/identifier-type (http://hl7.org/fhir/ValueSet/identifier-type), and a code should come from this value set unless it has no suitable code and the validator cannot judge what is suitable) (codes = http://fhir.de/CodeSystem/identifier-type-de-basis#GKV)
        // TODO: Profile reference 'https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3' has not been checked because it is unknown


        // assertTrue(validationResult.isSuccessful());
    }

    @Disabled
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
