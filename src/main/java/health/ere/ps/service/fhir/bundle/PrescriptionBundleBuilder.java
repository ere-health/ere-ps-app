package health.ere.ps.service.fhir.bundle;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;

import health.ere.ps.model.muster16.Muster16PrescriptionForm;

public class PrescriptionBundleBuilder {
    private static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy";
    private static final String DEFAULT_SHORT_DATE_FORMAT = "dd.MM.yy";
    private Muster16PrescriptionForm muster16PrescriptionForm;

    public PrescriptionBundleBuilder(Muster16PrescriptionForm muster16PrescriptionForm) {
        this.muster16PrescriptionForm = muster16PrescriptionForm;
    }

    public Bundle createBundle() throws ParseException {
        Bundle bundle = new Bundle();

        bundle.setId(""); // TODO: Get bundle ID from Gematik TI.

        bundle.getIdentifier()
                .setSystem("https://gematik.de/fhir/NamingSystem/PrescriptionID")
                .setValue("123456"); //TODO: Get actual unique signed ID.

        bundle.setType(Bundle.BundleType.DOCUMENT);

        // Add composition resource.
        Composition compositionResource = createComposition();

        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Composition/" + compositionResource.getId())
                .setResource(compositionResource);

        // Add medication request resource.
        MedicationRequest medicationRequestResource = createMedicationRequest();

        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/MedicationRequest/" +
                        medicationRequestResource.getId())
                .setResource(medicationRequestResource);

        // Add medication resource.
        Medication medicationResource = createMedicationResource();

        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Medication/" +
                        medicationResource.getId())
                .setResource(medicationResource);

        // Add patient resource.
        Patient patientResource = createPatientResource();

        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Patient/" +
                        patientResource.getId())
                .setResource(patientResource);

        // Add practitioner resource.
        Practitioner practitionerResource = createPractitionerResource();

        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Practitioner/" +
                        practitionerResource.getId())
                .setResource(practitionerResource);

        // Add organization resource.
        Organization organizationResource = createOrganizationResource();

        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Organization/" +
                        organizationResource.getId())
                .setResource(organizationResource);

        // Add coverage resource.
        Coverage coverageResource = createCoverageResource();

        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Coverage/" +
                        coverageResource.getId())
                .setResource(coverageResource);

        // All time related details should be registered after all resources have been created
        // and packaged for transmission.
        bundle.getMeta()
                .setLastUpdated(new Date())
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.1");

        bundle.setTimestamp(new Date());

        return bundle;
    }

    public static String getDateFormat(String date) {
        return date.length() == 8? DEFAULT_SHORT_DATE_FORMAT : DEFAULT_DATE_FORMAT;
    }

    public Patient createPatientResource() throws ParseException {
        Patient patient = new Patient();

        patient.setId(muster16PrescriptionForm.getPatientInsuranceId());
        patient.getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient");

        Identifier identifier = patient.addIdentifier();

        identifier.getType()
                .addCoding().setSystem("http://fhir.de/CodeSystem/identifier-type-de-basis");
        identifier.getType().addCoding().setCode("GKV");
        identifier.getSystemElement().setValue("http://fhir.de/NamingSystem/gkv/kvid-10");
//        identifier.setValue("M310119800"); //TODO: Source/Generate unique ID.

        patient.addName()
//                .setUse(HumanName.NameUse.fromCode("official"))
                .setFamily(muster16PrescriptionForm.getPatientLastName())
                .addGiven(muster16PrescriptionForm.getPatientFirstName());

        String patientDob = muster16PrescriptionForm.getPatientDateOfBirth();

        patient.setBirthDate(new SimpleDateFormat(getDateFormat(patientDob))
                    .parse(patientDob, new ParsePosition(0)));

        patient.addAddress()
                .setCity(muster16PrescriptionForm.getPatientCity())
                .setPostalCode(muster16PrescriptionForm.getPatientZipCode())
                .addLine(muster16PrescriptionForm.getPatientStreetName() + " " +
                        muster16PrescriptionForm.getPatientStreetNumber());
        return patient;
    }

    public Practitioner createPractitionerResource() {
        Practitioner practitioner = new Practitioner();

        practitioner.setId(muster16PrescriptionForm.getDoctorId())
                .getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3");

        Identifier identifier = practitioner.addIdentifier();

        CodeableConcept codeableConcept = identifier.getType();

        codeableConcept.addCoding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
                .setCode("LANR");

        identifier.setSystem("https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR");
        identifier.setValue("456456534"); //TODO: Generate/get unique ID value. Need to check this.

        //TODO: Ensure to set at least the doctor's family name which seems to be required.

        return practitioner;
    }

    public Organization createOrganizationResource() {
        Organization organization = new Organization();

        organization.setId(muster16PrescriptionForm.getClinicId()).getMeta().addProfile(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3");

        return organization;
    }

    public Coverage createCoverageResource() throws ParseException {
        Coverage coverage = new Coverage();

        coverage.setId(muster16PrescriptionForm.getInsuranceCompanyId());
//        coverage.getMeta()
//                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3");
        coverage.addPayor()
                .setDisplay(muster16PrescriptionForm.getInsuranceCompany())
                .getIdentifier()
                .setSystem("http://fhir.de/NamingSystem/arge-ik/iknr")
                .setValue("123456"); // TODO: Get actual unique value
        coverage.setStatus(Coverage.CoverageStatus.fromCode("active"));
        coverage.getBeneficiary().setReference(
                "Patient/" + muster16PrescriptionForm.getPatientInsuranceId());

        //TODO: Get actual insurance coverage period.
        String coveragePeriod = muster16PrescriptionForm.getPrescriptionDate();

        coverage.getPeriod().setEnd(new SimpleDateFormat(getDateFormat(coveragePeriod))
                .parse(coveragePeriod, new ParsePosition(0)));

        return coverage;
    }

    public Medication createMedicationResource() {
        Medication medication = new Medication();

        medication.setId("123456") // TODO: Get actual prescription ID.
                .getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.1");

        muster16PrescriptionForm.getPrescriptionList().stream().forEach(prescription -> {
            medication.getCode().addCoding()
                    .setSystem("http://fhir.de/CodeSystem/ifa/pzn")
                    .setCode("08585997");
            medication.getCode().setText(prescription);
        });

        return medication;
    }

    public MedicationRequest createMedicationRequest() throws ParseException {
        MedicationRequest medicationRequest = new MedicationRequest();

        medicationRequest.setId("123456"); //TODO: Get actual ID from Gematik TI

        medicationRequest.setStatus(
                MedicationRequest.MedicationRequestStatus.ACTIVE
        ).setIntent(
                MedicationRequest.MedicationRequestIntent.ORDER
        ).getMedicationReference().setReference(
                "Medication/" // TODO: Get actual medification reference ID
        );

        medicationRequest.getSubject().setReference(
                "Patient/" + muster16PrescriptionForm.getPatientInsuranceId());

        String prescriptionDate = muster16PrescriptionForm.getPrescriptionDate();

        medicationRequest.setAuthoredOn(new SimpleDateFormat(getDateFormat(prescriptionDate))
                .parse(prescriptionDate, new ParsePosition(0)));

        medicationRequest.getRequester().setReference(
                "Practitioner/" + muster16PrescriptionForm.getDoctorId());

        medicationRequest.addInsurance().setReference(
                "Coverage/" + muster16PrescriptionForm.getInsuranceCompanyId());

        muster16PrescriptionForm.getPrescriptionList().stream().forEach(prescription -> {
            medicationRequest.addDosageInstruction().setText(prescription).addExtension().setUrl(
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag"
            ).setValue(new BooleanType(true));
        });

        return medicationRequest;
    }

    public Composition createComposition() {
        Composition composition = new Composition();

        composition.setId("123456"); // TODO: Get ID from Gematik TI

        composition.getMeta().addProfile(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.0.1");

        composition.setStatus(Composition.CompositionStatus.PRELIMINARY)
                .getType()
                .addCoding()
                .setSystem("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_FORMULAR_ART")
                .setCode("e16A");

        composition.getSubject().setReference(
                "Patient/" + muster16PrescriptionForm.getPatientInsuranceId());

        composition.setDate(new Date());

        composition.addAuthor()
                .setReference("Practitioner/" + muster16PrescriptionForm.getDoctorId())
                .setType("Practitioner");

        composition.addAuthor()
                .setType("Device")
                .getIdentifier()
                    .setSystem("https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer")
                    .setValue("123456"); //TODO: Get actual KBV issued software certificate number.

        composition.setTitle("elektronische Arzneimittelverordnung");

        composition.addAttester()
                .setMode(Composition.CompositionAttestationMode.LEGAL)
                .getParty().setReference("Practitioner/" + muster16PrescriptionForm.getDoctorId());

        composition.getCustodian().setReference(
                "Organization/" + muster16PrescriptionForm.getClinicId());

        composition.addSection().getCode().addCoding()
                .setSystem("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type")
                .setCode("Prescription");

        Composition.SectionComponent sectionComponent = composition.addSection();

        sectionComponent.getCode().addCoding()
                    .setSystem("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type")
                    .setCode("Prescription");
        sectionComponent.addEntry()
                .setReference("MedicationRequest/"); // TODO: Get med request ID from Gematik TI

        sectionComponent = composition.addSection();

        sectionComponent.getCode().addCoding()
                .setSystem("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type")
                .setCode("Coverage");
        sectionComponent.addEntry()
                .setReference("Coverage/" + muster16PrescriptionForm.getInsuranceCompanyId());

        return composition;
    }
}
