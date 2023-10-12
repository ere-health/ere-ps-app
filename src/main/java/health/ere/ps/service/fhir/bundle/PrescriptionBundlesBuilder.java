package health.ere.ps.service.fhir.bundle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Address.AddressType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestDispenseRequestComponent;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestSubstitutionComponent;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Practitioner.PractitionerQualificationComponent;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.StringType;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import health.ere.ps.model.muster16.MedicationString;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;

public class PrescriptionBundlesBuilder implements IBundlesBuilder {
    protected static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    protected static final String DEFAULT_SHORT_DATE_FORMAT = "dd.MM.yy";

    protected final Muster16PrescriptionForm muster16PrescriptionForm;

    private static final Logger log = Logger.getLogger(PrescriptionBundlesBuilder.class.getName());

    protected String pruefnummer;

    public PrescriptionBundlesBuilder(Muster16PrescriptionForm muster16PrescriptionForm) {
        this.muster16PrescriptionForm = muster16PrescriptionForm;
        // if this is not provided use the Prüfnummer from Gematik
        this.pruefnummer = "VfS_BestKonfPS_TI_79";
    }

    public PrescriptionBundlesBuilder(Muster16PrescriptionForm muster16PrescriptionForm, String pruefnummer) {
        this.muster16PrescriptionForm = muster16PrescriptionForm;
        this.pruefnummer = pruefnummer;
    }

    protected static String getDateFormat(String date) {
        return StringUtils.isBlank(date) || date.length() == 8 ? DEFAULT_SHORT_DATE_FORMAT :
                DEFAULT_DATE_FORMAT;
    }

    @Override
    public List<Bundle> createBundles() {
        List<Bundle> bundles = new ArrayList<>();

        muster16PrescriptionForm.getPrescriptionList().forEach(medicationString ->
                bundles.add(createBundleForMedication(medicationString)));

        return bundles;
    }

    @Override
    public Bundle createBundleForMedication(MedicationString medication) {
        Bundle bundle = new Bundle();

        bundle.setId(UUID.randomUUID().toString());

        bundle.getIdentifier().setSystem("https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId");

        generateIdentifier(bundle);

        // This will be set by the erezept workflow
        bundle.setType(Bundle.BundleType.DOCUMENT);

        // Add patient resource.
        Patient patientResource = createPatientResource();

        // Add practitioner resource.
        Practitioner practitionerResource = createPractitionerResource();

        // Add medication resource.
        Medication medicationResource = createMedicationResource(medication);
        
        // Add coverage resource.
        Coverage coverageResource = createCoverageResource(patientResource.getId());

        // Add medication request resource.
        MedicationRequest medicationRequestResource = createMedicationRequest(
                medicationResource.getId(), patientResource.getId(), practitionerResource.getId(), coverageResource.getId());
        
        // Add organization resource.
        Organization organizationResource = createOrganizationResource();

        // Add composition resource.
        Composition compositionResource = createComposition(medicationRequestResource.getId(), patientResource.getId(),
                practitionerResource.getId(), organizationResource.getId(), coverageResource.getId());

        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Composition/" + compositionResource.getId())
                .setResource(compositionResource);

        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Medication/" +
                        medicationResource.getId())
                .setResource(medicationResource);


        
        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/MedicationRequest/" +
                        medicationRequestResource.getId())
                .setResource(medicationRequestResource);

        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Patient/" +
                        patientResource.getId())
                .setResource(patientResource);

        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Practitioner/" +
                        practitionerResource.getId())
                .setResource(practitionerResource);

        
        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Organization/" +
                        organizationResource.getId())
                .setResource(organizationResource);


        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Coverage/" +
                        coverageResource.getId())
                .setResource(coverageResource);

        // All time related details should be registered after all resources have been created
        // and packaged for transmission.
        bundle.getMeta()
                .setLastUpdated(new Date())
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.1.0");

        bundle.setTimestamp(new Date());

        return bundle;
    }

    private void generateIdentifier(Bundle bundle) {
        long part1 = Math.round(100+Math.random()*900);
        long part2 = Math.round(100+Math.random()*900);
        long part3 = Math.round(100+Math.random()*900);
        long part4 = Math.round(100+Math.random()*900);

        long fullNumber = 160*1000000000000l+(part1*1000000000l)+(part2*1000000l)+(part3*1000l)+part4;
        long lastDigits = 98-(fullNumber % 97);

        bundle.getIdentifier().setValue("160."+part1+"."+part2+"."+part3+"."+part4+"."+(lastDigits<10? "0" : "")+lastDigits);
    }

    Patient createPatientResource() {
        Patient patient = new Patient();

        patient.setId(UUID.randomUUID().toString());
        patient.getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.1.0");

        Identifier identifier = patient.addIdentifier();

        Coding typeDeBasis = identifier.getType()
                .addCoding();
        typeDeBasis.setSystem("http://fhir.de/CodeSystem/identifier-type-de-basis");
        typeDeBasis.setCode("GKV");
        identifier.getSystemElement().setValue("http://fhir.de/sid/gkv/kvid-10");
        identifier.setValue(muster16PrescriptionForm.getPatientInsuranceId());

        List<StringType> prefixList = new ArrayList<StringType>();
        
        if(muster16PrescriptionForm.getPatientNamePrefix().size() > 0) {
            StringType prefix = new StringType(muster16PrescriptionForm.getPatientNamePrefix().get(0));
            Extension extension = new Extension("http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier", new CodeType("AC"));
            prefix.addExtension(extension);
            prefixList.add(prefix);
        }

        HumanName humanName = patient.addName();

        humanName
                .setUse(NameUse.OFFICIAL)
                .setPrefix(prefixList)
                .addGiven(muster16PrescriptionForm.getPatientFirstName());
        
        StringType familyElement = humanName.getFamilyElement();
        List<String> nameParts = new ArrayList<>();

        if(muster16PrescriptionForm.getPatientLastName() != null && !"".equals(muster16PrescriptionForm.getPatientLastName())) {
                Extension extension = new Extension("http://hl7.org/fhir/StructureDefinition/humanname-own-name", new StringType(muster16PrescriptionForm.getPatientLastName()));
                familyElement.addExtension(extension);
                nameParts.add(muster16PrescriptionForm.getPatientLastName());
        }
        familyElement.setValue(nameParts.stream().collect(Collectors.joining(" ")));

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

        StringType line = patient.getAddress().get(0).getLine().get(0);
        line.addExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName", new StringType(muster16PrescriptionForm.getPatientStreetName()));
        line.addExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber", new StringType(muster16PrescriptionForm.getPatientStreetNumber()));
        return patient;
    }

    private Practitioner createPractitionerResource() {
        Practitioner practitioner = new Practitioner();

        practitioner.setId(UUID.randomUUID().toString())
                .getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.1.0");

        Identifier identifier = practitioner.addIdentifier();
        CodeableConcept identifierCodeableConcept = identifier.getType();
        identifierCodeableConcept.addCoding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
                .setCode("LANR");

        identifier.setSystem("https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR");
        identifier.setValue(muster16PrescriptionForm.getPractitionerId());

        List<StringType> prefixList = new ArrayList<StringType>();
        if(muster16PrescriptionForm.getPractitionerNamePrefix() != null && !"".equals(muster16PrescriptionForm.getPractitionerNamePrefix())) {
            StringType prefix = new StringType(muster16PrescriptionForm.getPractitionerNamePrefix());
            Extension extension = new Extension("http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier", new CodeType("AC"));
            prefix.addExtension(extension);
            prefixList.add(prefix);
        }

        HumanName humanName = practitioner.addName();
        if(muster16PrescriptionForm.getPractitionerFirstName() == null || "".equals(muster16PrescriptionForm.getPractitionerFirstName())) {
                muster16PrescriptionForm.setPractitionerFirstName("Unbekannt");
        }
        if(muster16PrescriptionForm.getPractitionerLastName() == null || "".equals(muster16PrescriptionForm.getPractitionerLastName())) {
                muster16PrescriptionForm.setPractitionerLastName("Unbekannt");
        }

        humanName
                .setUse(NameUse.OFFICIAL)
                .setPrefix(prefixList)
                .addGiven(muster16PrescriptionForm.getPractitionerFirstName());
        
        StringType familyElement = humanName.getFamilyElement();
        List<String> nameParts = new ArrayList<>();

        if(muster16PrescriptionForm.getPractitionerLastName() != null) {
            Extension extension = new Extension("http://hl7.org/fhir/StructureDefinition/humanname-own-name", new StringType(muster16PrescriptionForm.getPractitionerLastName()));
            familyElement.addExtension(extension);
            nameParts.add(muster16PrescriptionForm.getPractitionerLastName());
        }
        familyElement.setValue(nameParts.stream().collect(Collectors.joining(" ")));

        PractitionerQualificationComponent qualification = new PractitionerQualificationComponent();
        CodeableConcept qualificationCodeableConcept = new CodeableConcept();
        Coding practitionerQualificationCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Qualification_Type",
                (muster16PrescriptionForm.getPractitionerQualification() == null ||
                        muster16PrescriptionForm.getPractitionerQualification().equals("")) ? "00" : muster16PrescriptionForm.getPractitionerQualification(), null);
        qualificationCodeableConcept.addCoding(practitionerQualificationCoding);

        qualification.setCode(qualificationCodeableConcept);
        practitioner.addQualification(qualification);
        qualification = practitioner.addQualification();
        CodeableConcept code = qualification.getCode();
        code.setText("Arzt");
        practitionerQualificationCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Berufsbezeichnung", "Berufsbezeichnung", null);
        code.getCoding().add(practitionerQualificationCoding);

        return practitioner;
    }

    private Organization createOrganizationResource() {
        Organization organization = new Organization();

        organization.setId(UUID.randomUUID().toString()).getMeta().addProfile(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.1.0");


        Identifier identifier = organization.addIdentifier();

        CodeableConcept codeableConcept = identifier.getType();

        codeableConcept.addCoding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
                .setCode("BSNR");

        identifier.setSystem("https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR");
        identifier.setValue(muster16PrescriptionForm.getClinicId());

        String organizationName = "Praxis";
        if(!"".equals(muster16PrescriptionForm.getPractitionerFirstName())) {
            organizationName += " "+muster16PrescriptionForm.getPractitionerFirstName();
        }
        if(!"".equals(muster16PrescriptionForm.getPractitionerLastName())) {
            organizationName += " "+muster16PrescriptionForm.getPractitionerLastName();
        }

        organization.setName(organizationName);

        organization.addTelecom().setSystem(ContactPointSystem.PHONE)
                .setValue(muster16PrescriptionForm.getPractitionerPhone());

        if(muster16PrescriptionForm.getPractitionerFax() != null && !"".equals(muster16PrescriptionForm.getPractitionerFax())) {
                ContactPoint faxContact = new ContactPoint()
                        .setSystem(ContactPointSystem.FAX)
                        .setValue(muster16PrescriptionForm.getPractitionerFax());
                organization.getTelecom().add(faxContact);
        }

        organization.addAddress()
                .setType(AddressType.BOTH)
                .setCity(muster16PrescriptionForm.getPractitionerCity())
                .setPostalCode(muster16PrescriptionForm.getPractitionerZipCode())
                .addLine(muster16PrescriptionForm.getPractitionerStreetName() + " " +
                        muster16PrescriptionForm.getPractitionerStreetNumber())
                .setCountry("D");
        
        StringType line = organization.getAddress().get(0).getLine().get(0);
        line.addExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName", new StringType(muster16PrescriptionForm.getPractitionerStreetName()));
        line.addExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber", new StringType(muster16PrescriptionForm.getPractitionerStreetNumber()));

        return organization;
    }

    Coverage createCoverageResource(String patientId) {
        Coverage coverage = new Coverage();

        coverage.setId(UUID.randomUUID().toString());
        coverage.getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.1.0");

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
        Coding versichertenart = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS", patientStatus, null);
        Extension versichertenartEx = new Extension("http://fhir.de/StructureDefinition/gkv/versichertenart", versichertenart);

//        Coding versichertenart = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP", patientStatus, null);
//        Extension versichertenartEx = new Extension("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS", versichertenart);

        coverage.addExtension(versichertenartEx);

        coverage.setStatus(Coverage.CoverageStatus.fromCode("active"));

        coverage.setType(new CodeableConcept().addCoding(new Coding("http://fhir" +
                ".de/CodeSystem/versicherungsart-de-basis", "GKV", "")));

        coverage.getBeneficiary().setReference("Patient/" + patientId);

        String payorIdentifier = "UNKNOWN";
        if (muster16PrescriptionForm.getInsuranceCompanyId() != null && !("".equals(muster16PrescriptionForm.getInsuranceCompanyId()))) {
            payorIdentifier = muster16PrescriptionForm.getInsuranceCompanyId();
        }

        coverage.addPayor()
                .setDisplay(muster16PrescriptionForm.getInsuranceCompany())
                .getIdentifier()
                .setSystem("http://fhir.de/sid/arge-ik/iknr")
                .setValue(payorIdentifier);

        return coverage;
    }

    private Medication createMedicationResource(MedicationString medicationString) {
        Medication medication = new Medication();

        medication.setId(UUID.randomUUID().toString())
                .getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.1.0");
        Coding medicationType = new Coding("http://snomed.info/sct", "763158003", "Medicinal product (product)");
        medicationType.setVersion("http://snomed.info/sct/900000000000207008/version/20220331");
        CodeableConcept codeableConcept = new CodeableConcept(medicationType);
        Extension medicationTypeEx = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_Base_Medication_Type", codeableConcept);
        medication.addExtension(medicationTypeEx);


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

    private MedicationRequest createMedicationRequest(String medicationId, String patientId, String practitionerId, String coverageId) {
        MedicationRequest medicationRequest = new MedicationRequest();

        medicationRequest.setId(UUID.randomUUID().toString());

        medicationRequest.getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.1.0");

        Coding valueCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_StatusCoPayment",
                muster16PrescriptionForm.getWithPayment() ? "0" : "1", null);
        Extension coPayment = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_StatusCoPayment", valueCoding);
        medicationRequest.addExtension(coPayment);

        Extension emergencyServicesFee = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee", new BooleanType(false));
        medicationRequest.addExtension(emergencyServicesFee);

        Extension bvg = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG", new BooleanType(false));
        medicationRequest.addExtension(bvg);

        Extension multiplePrescription = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription");
        multiplePrescription.addExtension(new Extension("Kennzeichen", new BooleanType(false)));
        medicationRequest.addExtension(multiplePrescription);

        medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE)
                .setIntent(MedicationRequest.MedicationRequestIntent.ORDER)
                .getMedicationReference().setReference("Medication/" + medicationId);

        medicationRequest.getSubject().setReference("Patient/" + patientId);

        String prescriptionDate = muster16PrescriptionForm.getPrescriptionDate();

        try {
            medicationRequest.setAuthoredOn(new SimpleDateFormat(getDateFormat(prescriptionDate), Locale.GERMANY)
                    .parse(prescriptionDate));
        } catch (ParseException e) {
            log.warning("Could not set AuthoredOn Date when creating the bundle:" + prescriptionDate);
            medicationRequest.setAuthoredOn(new Date());
        }
        medicationRequest.getAuthoredOnElement().setPrecision(TemporalPrecisionEnum.DAY);


        medicationRequest.getRequester().setReference(
                "Practitioner/" + practitionerId);

        medicationRequest.addInsurance().setReference(
                "Coverage/" + coverageId);

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

    private Composition createComposition(String medicationRequestId, String patientId, String practitionerId, String organizationId, String coverageId) {
        Composition composition = new Composition();

        composition.setId(UUID.randomUUID().toString());

        composition.getMeta().addProfile(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.1.0");

        Coding valueCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN", "04", null);
        Extension legalBasis = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis", valueCoding);
        composition.addExtension(legalBasis);

        composition.setStatus(Composition.CompositionStatus.FINAL)
                .getType()
                .addCoding()
                .setSystem("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_FORMULAR_ART")
                .setCode("e16A");

        composition.getSubject().setReference("Patient/" + patientId);

        composition.setDate(new Date());

        composition.addAuthor()
                .setReference("Practitioner/" + practitionerId)
                .setType("Practitioner");

        composition.addAuthor()
                .setType("Device")
                .getIdentifier()
                .setSystem("https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer")
                .setValue(pruefnummer);

        composition.setTitle("elektronische Arzneimittelverordnung");

        //composition.addAttester()
        //        .setMode(Composition.CompositionAttestationMode.LEGAL)
        //        .getParty().setReference("Practitioner/" + practitionerId);

        composition.getCustodian().setReference(
                "Organization/" + organizationId);

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
                .setReference("Coverage/" + coverageId);

        return composition;
    }
}
