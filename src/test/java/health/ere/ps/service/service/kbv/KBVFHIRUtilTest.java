package health.ere.ps.service.kbv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.junit.jupiter.api.Test;

import de.gematik.ws.fa.vsdm.vsd.v5.UCAllgemeineVersicherungsdatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCGeschuetzteVersichertendatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCPersoenlicheVersichertendatenXML;

public class KBVFHIRUtilTest {

    @Test
    public void testUCAllgemeineVersicherungsdatenXML2Coverage() {
        // Create test data
        UCAllgemeineVersicherungsdatenXML versicherung = new UCAllgemeineVersicherungsdatenXML();
        String patientId = "123";
        UCGeschuetzteVersichertendatenXML versichungKennzeichen = new UCGeschuetzteVersichertendatenXML();

        // Test the method
        Coverage coverage = KBVFHIRUtil.UCAllgemeineVersicherungsdatenXML2Coverage(versicherung, patientId, versichungKennzeichen);

        // Assertions for coverage object
        assertNotNull(coverage);
        assertNotNull(coverage.getMeta());
        assertEquals(1, coverage.getMeta().getProfile().size());
        assertEquals("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.1.0", coverage.getMeta().getProfile().get(0).getValue());
        assertNotNull(coverage.getExtension());
        assertEquals(3, coverage.getExtension().size());
        assertNotNull(coverage.getStatus());
        assertEquals("active", coverage.getStatus().toCode());
        assertNotNull(coverage.getType());
        assertEquals(1, coverage.getType().getCoding().size());
        assertEquals("http://fhir.de/CodeSystem/versicherungsart-de-basis", coverage.getType().getCoding().get(0).getSystem());
        assertEquals("GKV", coverage.getType().getCoding().get(0).getCode());
        assertNotNull(coverage.getBeneficiary());
        assertEquals("Patient/" + patientId, coverage.getBeneficiary().getReference());
    }

    @Test
    public void testUCPersoenlicheVersichertendatenXML2Patient() {
        // Create test data
        UCPersoenlicheVersichertendatenXML schaumberg = new UCPersoenlicheVersichertendatenXML();

        // Test the method
        Patient patient = KBVFHIRUtil.UCPersoenlicheVersichertendatenXML2Patient(schaumberg);

        // Assertions for patient object
        assertNotNull(patient);
        assertNotNull(patient.getMeta());
        assertEquals(1, patient.getMeta().getProfile().size());
        assertEquals("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.1.0", patient.getMeta().getProfile().get(0).getValue());
        assertNotNull(patient.getIdentifier());
        assertEquals(1, patient.getIdentifier().size());
        assertEquals("GKV", patient.getIdentifier().get(0).getType().getCoding().get(0).getCode());
    }

    @Test
    public void testAssembleBundle() {
        // Create test data
        Practitioner practitioner = new Practitioner();
        Organization organization = new Organization();
        Patient patient = new Patient();
        Coverage coverage = new Coverage();
        Medication medication = new Medication();
        MedicationRequest medicationRequest = new MedicationRequest();
        PractitionerRole practitionerRole = new PractitionerRole();
        Practitioner attester = new Practitioner();

        // Test the method
        Bundle bundle = KBVFHIRUtil.assembleBundle(practitioner, organization, patient, coverage, medication, medicationRequest, practitionerRole, attester);

        // Assertions for bundle object
        assertNotNull(bundle);
        assertNotNull(bundle.getIdentifier());
        assertNotNull(bundle.getType());
        assertEquals("DOCUMENT", bundle.getType().toCode());
        assertNotNull(bundle.getMeta());
        assertNotNull(bundle.getTimestamp());
    }
}
