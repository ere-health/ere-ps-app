package health.ere.ps.service.fhir.bundle;

import health.ere.ps.model.muster16.MedicationString;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import org.hl7.fhir.r4.model.Address.AddressType;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestDispenseRequestComponent;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestSubstitutionComponent;
import org.hl7.fhir.r4.model.Practitioner.PractitionerQualificationComponent;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PrescriptionBundlesBuilder {
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_SHORT_DATE_FORMAT = "dd.MM.yy";
    private static final Logger log = Logger.getLogger(PrescriptionBundlesBuilder.class.getName());

    private final Muster16PrescriptionForm muster16PrescriptionForm;

    public PrescriptionBundlesBuilder(Muster16PrescriptionForm muster16PrescriptionForm) {
        this.muster16PrescriptionForm = muster16PrescriptionForm;
    }

    private static String getDateFormat(String date) {
        return date.length() == 8 ? DEFAULT_SHORT_DATE_FORMAT : DEFAULT_DATE_FORMAT;
    }

    public List<Bundle> createBundles() throws ParseException {
        List<Bundle> bundles = new ArrayList<>();

        muster16PrescriptionForm.getPrescriptionList().forEach(medicationString ->
                bundles.add(createBundleForMedication(medicationString)));

        return bundles;
    }

    private Bundle createBundleForMedication(MedicationString medication) {
        Bundle bundle = new Bundle();

        bundle.setId(UUID.randomUUID().toString());

        // This will be set by the erezept workflow
        bundle.setType(Bundle.BundleType.DOCUMENT);

        // Add medication resource.
        Medication medicationResource = createMedicationResource(medication);
        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Medication/" +
                        medicationResource.getId())
                .setResource(medicationResource);

        // Add medication request resource.
        MedicationRequest medicationRequestResource = createMedicationRequest(medicationResource.getId());
        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/MedicationRequest/" +
                        medicationRequestResource.getId())
                .setResource(medicationRequestResource);

        // Add composition resource.
        Composition compositionResource = createComposition(medicationRequestResource.getId());
        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Composition/" + compositionResource.getId())
                .setResource(compositionResource);

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

    Patient createPatientResource() {
        Patient patient = new Patient();

        patient.setId(muster16PrescriptionForm.getPatientInsuranceId());
        patient.getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3");

        Identifier identifier = patient.addIdentifier();

        Coding typeDeBasis = identifier.getType()
                .addCoding();
        typeDeBasis.setSystem("http://fhir.de/CodeSystem/identifier-type-de-basis");
        typeDeBasis.setCode("GKV");
        identifier.getSystemElement().setValue("http://fhir.de/NamingSystem/gkv/kvid-10");
        identifier.setValue(muster16PrescriptionForm.getPatientInsuranceId());

        patient.addName()
                .setUse(NameUse.OFFICIAL)
                .setPrefix(muster16PrescriptionForm.getPatientNamePrefix().stream()
                        .map(StringType::new).collect(Collectors.toList()))
                .setFamily(muster16PrescriptionForm.getPatientLastName())
                .addGiven(muster16PrescriptionForm.getPatientFirstName());

        String patientBirthDate = muster16PrescriptionForm.getPatientDateOfBirth();

        try {
            patient.setBirthDate(new SimpleDateFormat(getDateFormat(patientBirthDate), Locale.GERMANY)
                    .parse(patientBirthDate));
        } catch (ParseException e) {
            log.warning("Could not parse this birthdate when creating the bundle:" + patientBirthDate);
        }

        patient.addAddress()
                .setType(AddressType.BOTH)
                .setCountry("D")
                .setCity(muster16PrescriptionForm.getPatientCity())
                .setPostalCode(muster16PrescriptionForm.getPatientZipCode())
                .addLine(muster16PrescriptionForm.getPatientStreetName() + " " +
                        muster16PrescriptionForm.getPatientStreetNumber());
        return patient;
    }

    private Practitioner createPractitionerResource() {
        Practitioner practitioner = new Practitioner();

        practitioner.setId(muster16PrescriptionForm.getPractitionerId())
                .getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3");

        Identifier identifier = practitioner.addIdentifier();
        CodeableConcept identifierCodeableConcept = identifier.getType();

        identifierCodeableConcept.addCoding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
                .setCode("BSNR");

        identifier.setSystem("https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR");
        identifier.setValue(muster16PrescriptionForm.getPractitionerId()); //TODO: Generate/get unique ID value. Need to check this.

        practitioner.addName()
                .setUse(NameUse.OFFICIAL)
                .setFamily(muster16PrescriptionForm.getPractitionerLastName())
                .addGiven(muster16PrescriptionForm.getPractitionerFirstName())
                .addPrefix(muster16PrescriptionForm.getPractitionerNamePrefix())
                .addExtension(new Extension("http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier", new StringType("AC")));

        PractitionerQualificationComponent qualification = new PractitionerQualificationComponent();
        CodeableConcept qualificationCodeableConcept = new CodeableConcept();
        Coding hausarztCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Qualification_Type", "00", "Arzt-Hausarzt");
        qualificationCodeableConcept.setText("Arzt-Hausarzt");
        qualificationCodeableConcept.addCoding(hausarztCoding);

        qualification.setCode(qualificationCodeableConcept);
        practitioner.addQualification(qualification);

        practitioner.addAddress()
                .setType(AddressType.BOTH)
                .setCountry("D")
                .setCity(muster16PrescriptionForm.getPractitionerCity())
                .setPostalCode(muster16PrescriptionForm.getPractitionerZipCode())
                .addLine(muster16PrescriptionForm.getPractitionerStreetName() + " " +
                        muster16PrescriptionForm.getPractitionerStreetNumber());

        ContactPoint phoneContact = new ContactPoint()
                .setSystem(ContactPointSystem.PHONE)
                .setValue(muster16PrescriptionForm.getPractitionerPhone());
        practitioner.getTelecom().add(phoneContact);

        ContactPoint faxContact = new ContactPoint()
                .setSystem(ContactPointSystem.FAX)
                .setValue(muster16PrescriptionForm.getPractitionerFax());
        practitioner.getTelecom().add(faxContact);

        return practitioner;
    }

    private Organization createOrganizationResource() {
        Organization organization = new Organization();

        organization.setId(muster16PrescriptionForm.getClinicId()).getMeta().addProfile(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3");

        organization.getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3");

        Identifier identifier = organization.addIdentifier();

        CodeableConcept codeableConcept = identifier.getType();

        codeableConcept.addCoding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
                .setCode("BSNR");

        identifier.setSystem("https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR");
        identifier.setValue(muster16PrescriptionForm.getClinicId());

        organization.setName((muster16PrescriptionForm.getPractitionerNamePrefix() + " "
                + muster16PrescriptionForm.getPractitionerFirstName() + " "
                + muster16PrescriptionForm.getPractitionerLastName()).trim());

        organization.addTelecom().setSystem(ContactPointSystem.PHONE)
                .setValue(muster16PrescriptionForm.getPractitionerPhone());
        organization.addAddress()
                .setType(AddressType.BOTH)
                .setCity(muster16PrescriptionForm.getPractitionerCity())
                .setPostalCode(muster16PrescriptionForm.getPractitionerZipCode())
                .addLine(muster16PrescriptionForm.getPractitionerStreetName() + " " +
                        muster16PrescriptionForm.getPractitionerStreetNumber())
                .setCountry("D");

        return organization;
    }

    Coverage createCoverageResource() {
        Coverage coverage = new Coverage();

        coverage.setId(muster16PrescriptionForm.getInsuranceCompanyId());
        coverage.getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3");

        Coding besonderePersonengruppe = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE", "00", null);
        Extension besonderePersonengruppeEx = new Extension("http://fhir.de/StructureDefinition/gkv/besondere-personengruppe", besonderePersonengruppe);
        coverage.addExtension(besonderePersonengruppeEx);

        Coding dmp = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP", "00", null);
        Extension dmpEx = new Extension("http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen", dmp);
        coverage.addExtension(dmpEx);

        Coding wop = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP", "72", null);
        Extension wopEx = new Extension("http://fhir.de/StructureDefinition/gkv/wop", wop);
        coverage.addExtension(wopEx);

        String patientStatus = muster16PrescriptionForm.getPatientStatus().replaceAll("0", "");
        Coding versichertenart = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP", patientStatus, null);
        Extension versichertenartEx = new Extension("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS", versichertenart);
        coverage.addExtension(versichertenartEx);

        coverage.setStatus(Coverage.CoverageStatus.fromCode("active"));

        coverage.setType(new CodeableConcept().addCoding(new Coding("http://fhir.de/CodeSystem/versicherungsart-de-basis", "GKV", "")));

        coverage.getBeneficiary().setReference(
                "Patient/" + muster16PrescriptionForm.getPatientInsuranceId());

        //TODO: Get actual insurance coverage period.
        String coveragePeriod = muster16PrescriptionForm.getPrescriptionDate();

        try {
            coverage.getPeriod().setEnd(new SimpleDateFormat(getDateFormat(coveragePeriod))
                    .parse(coveragePeriod));
        } catch (ParseException e) {
            log.warning("Could not parse this coverage end period date when creating the bundle:" + coveragePeriod);
        }

        String payorIdentifier = "UNKNOWN";
        if(muster16PrescriptionForm.getInsuranceCompanyId() != null && !("".equals(muster16PrescriptionForm.getInsuranceCompanyId()))) {
            payorIdentifier = muster16PrescriptionForm.getInsuranceCompanyId();
        }

        coverage.addPayor()
                .setDisplay(muster16PrescriptionForm.getInsuranceCompany())
                .getIdentifier()
                .setSystem("http://fhir.de/NamingSystem/arge-ik/iknr")
                .setValue(payorIdentifier);

        return coverage;
    }

    private Medication createMedicationResource(MedicationString medicationString) {
        Medication medication = new Medication();

        medication.setId(UUID.randomUUID().toString()) // TODO: Get actual prescription ID.
                .getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.1");


        Coding medicationCategory = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category", "00", null);
        Extension medicationCategoryEx = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category", medicationCategory);
        medication.addExtension(medicationCategoryEx);

        Extension medicationVaccine = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine", new BooleanType(false));
        medication.addExtension(medicationVaccine);

        Extension normgroesse = new Extension("http://fhir.de/StructureDefinition/normgroesse", new CodeType("N1"));
        medication.addExtension(normgroesse);

        medication.getCode().addCoding().setSystem("http://fhir.de/CodeSystem/ifa/pzn").setCode(medicationString.getPzn());
        medication.getCode().setText(medicationString.getName());

        Coding formCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM", "FLE", "");
        medication.setForm(new CodeableConcept().addCoding(formCoding));

        return medication;
    }

    private MedicationRequest createMedicationRequest(String medicationId) {
        MedicationRequest medicationRequest = new MedicationRequest();

        medicationRequest.setId(UUID.randomUUID().toString());

        medicationRequest.getMeta()
                .setLastUpdated(new Date())
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.1");

        Coding valueCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_StatusCoPayment", muster16PrescriptionForm.getWithPayment() ? "0" : "1", null);
        Extension coPayment = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment", valueCoding);
        medicationRequest.addExtension(coPayment);

        Extension emergencyServicesFee = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee", new BooleanType(false));
        medicationRequest.addExtension(emergencyServicesFee);

        Extension bvg = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG", new BooleanType(false));
        medicationRequest.addExtension(bvg);

        Extension multiplePrescription = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription");
        multiplePrescription.addExtension(new Extension("Kennzeichen", new BooleanType(false)));
        medicationRequest.addExtension(multiplePrescription);

        medicationRequest.setStatus(
                MedicationRequest.MedicationRequestStatus.ACTIVE
        ).setIntent(
                MedicationRequest.MedicationRequestIntent.ORDER
        ).getMedicationReference().setReference(
                "Medication/" + medicationId
        );

        medicationRequest.getSubject().setReference(
                "Patient/" + muster16PrescriptionForm.getPatientInsuranceId());

        String prescriptionDate = muster16PrescriptionForm.getPrescriptionDate();

        try {
            medicationRequest.setAuthoredOn(new SimpleDateFormat(getDateFormat(prescriptionDate))
                    .parse(prescriptionDate));
        } catch (ParseException e) {
            log.warning("Could not set AuthoredOn Date when creating the bundle:" + prescriptionDate);
        }

        medicationRequest.getRequester().setReference(
                "Practitioner/" + muster16PrescriptionForm.getPractitionerId());

        medicationRequest.addInsurance().setReference(
                "Coverage/" + muster16PrescriptionForm.getInsuranceCompanyId());

        if (muster16PrescriptionForm.getPrescriptionList().size() > 0) {
            MedicationString prescription = muster16PrescriptionForm.getPrescriptionList().get(0);

            medicationRequest.addDosageInstruction().setText(prescription.getDosage()).addExtension().setUrl(
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag"
            ).setValue(new BooleanType(true));
        }

        MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = new MedicationRequestDispenseRequestComponent();
        Quantity quantity = new Quantity();
        quantity.setValue(1);
        quantity.setSystem("http://unitsofmeasure.org");
        quantity.setCode("{Package}");
        dispenseRequest.setQuantity(quantity);
        medicationRequest.setDispenseRequest(dispenseRequest);
        MedicationRequestSubstitutionComponent substitution = new MedicationRequestSubstitutionComponent();
        substitution.setAllowed(new BooleanType(true));
        medicationRequest.setSubstitution(substitution);

        return medicationRequest;
    }

    private Composition createComposition(String medicationRequestId) {
        Composition composition = new Composition();

        composition.setId(UUID.randomUUID().toString()); // TODO: Get ID from Gematik TI

        composition.getMeta().addProfile(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.0.1");

        //        <extension url="https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis">
        //            <valueCoding>
        //                <system value="https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN" />
        //                <code value="04" />
        //            </valueCoding>
        //        </extension>
        Coding valueCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN", "04", null);
        Extension legalBasis = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis", valueCoding);
        composition.addExtension(legalBasis);

        composition.setStatus(Composition.CompositionStatus.FINAL)
                .getType()
                .addCoding()
                .setSystem("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_FORMULAR_ART")
                .setCode("e16A");

        composition.getSubject().setReference(
                "Patient/" + muster16PrescriptionForm.getPatientInsuranceId());

        composition.setDate(new Date());

        composition.addAuthor()
                .setReference("Practitioner/" + muster16PrescriptionForm.getPractitionerId())
                .setType("Practitioner");

        composition.addAuthor()
                .setType("Device")
                .getIdentifier()
                .setSystem("https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer")
                .setValue("123456"); //TODO: Get actual KBV issued software certificate number.

        composition.setTitle("elektronische Arzneimittelverordnung");

        composition.addAttester()
                .setMode(Composition.CompositionAttestationMode.LEGAL)
                .getParty().setReference("Practitioner/" + muster16PrescriptionForm.getPractitionerId());

        composition.getCustodian().setReference(
                "Organization/" + muster16PrescriptionForm.getClinicId());

        Composition.SectionComponent sectionComponent = composition.addSection();

        sectionComponent.getCode().addCoding()
                .setSystem("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type")
                .setCode("Prescription");
        sectionComponent.addEntry()
                .setReference("MedicationRequest/" + medicationRequestId);

        sectionComponent = composition.addSection();

        sectionComponent.getCode().addCoding()
                .setSystem("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type")
                .setCode("Coverage");
        sectionComponent.addEntry()
                .setReference("Coverage/" + muster16PrescriptionForm.getInsuranceCompanyId());

        return composition;
    }
}
