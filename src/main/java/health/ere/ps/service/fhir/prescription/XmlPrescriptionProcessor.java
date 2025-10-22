package health.ere.ps.service.fhir.prescription;

import ca.uhn.fhir.context.FhirContext;
import health.ere.ps.service.fhir.FHIRService;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Practitioner.PractitionerQualificationComponent;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class XmlPrescriptionProcessor {
    // Get <Bundle> tag including content

    private static final Pattern GET_UUID = Pattern.compile("^urn:uuid:(.*)");
    private static final FhirContext fhirContext = FHIRService.getFhirContext();

    protected abstract void adjustTypeEntries(Bundle bundle, Practitioner practitioner, Patient patient);

    public Bundle createFixedBundleFromString(String bundleXml) {
        bundleXml = bundleXml.replaceAll("\\|1.0.1", "|1.1.0");

        Bundle bundle = fhirContext.newXmlParser().parseResource(Bundle.class, bundleXml);
        fixFullUrls(bundle);
        fixReferencesInComposition(bundle);

        // Next issue ERROR - Bundle.entry[4].resource.ofType(Practitioner) - Practitioner.qualification: minimum required = 2, but only found 1 (from https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3)
        // Next issue ERROR - Bundle.entry[4].resource.ofType(Practitioner) - Practitioner.qualification:Berufsbezeichnung: minimum required = 1, but only found 0 (from https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3)

        Practitioner practitioner = getTypeFromBundle(Practitioner.class, bundle);

        if (!practitioner.getName().isEmpty()) {
            HumanName humanName = practitioner.getName().get(0);
            humanName.getFamilyElement().setValueAsString(humanName.getFamilyElement().getExtensionString("http://hl7.org/fhir/StructureDefinition/humanname-own-name"));
        }

        if (practitioner.getQualification().size() == 1) {
            PractitionerQualificationComponent qualification = new PractitionerQualificationComponent();
            CodeableConcept qualificationCodeableConcept = new CodeableConcept();
            qualificationCodeableConcept.setText("Arzt");

            qualification.setCode(qualificationCodeableConcept);
            practitioner.addQualification(qualification);
        }

        // Error while decoding XML: Missing Field (id=value, path=/Bundle/entry/resource/Patient/identifier)!
        Patient patient = getTypeFromBundle(Patient.class, bundle);

        // Remove first name from family name
        try {
            patient.getName().get(0).setFamily(patient.getName().get(0).getFamily().replace(patient.getName().get(0).getGiven().get(0).getValue() + " ", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (!patient.getIdentifier().isEmpty() && !patient.getIdentifier().get(0).hasValue()) {
            patient.getIdentifier().get(0).setValue("X999999999");
        }

        adjustTypeEntries(bundle, practitioner, patient);

        // Next issue WARNING - Bundle.entry[6].resource.ofType(Coverage).beneficiary - URN reference ist nicht lokal innerhalb des Bundles contained urn:uuid:91c0f8d8-8af1-467f-8d09-0c8a406b0127
        Coverage coverage = getTypeFromBundle(Coverage.class, bundle);
        coverage.getBeneficiary().setReference("Patient/" + patient.getIdElement().getIdPart());

        // addComposition(bundle);
        return bundle;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getTypeFromBundle(Class<T> clazz, Bundle bundle) {
        return (T) bundle.getEntry().stream()
            .filter(b -> b.getResource().getResourceType().name().equals(clazz.getSimpleName()))
            .findAny()
            .get()
            .getResource();
    }

    protected abstract void fixCompositionSections(Bundle bundle, Composition composition);

    private void fixReferencesInComposition(Bundle bundle) {
        String patientId = getIdFor(bundle, "Patient");
        String practitionerId = getIdFor(bundle, "Practitioner");
        String organizationId = getIdFor(bundle, "Organization");

        Composition composition = (Composition) bundle.getEntry().stream().findFirst().get().getResource();

        composition.getSubject().setReference("Patient/" + patientId);
        composition.getAuthor().get(0).setReference("Practitioner/" + practitionerId);
        composition.getCustodian().setReference("Organization/" + organizationId);

        fixCompositionSections(bundle, composition);
    }

    protected abstract String getFhirUrl();

    protected void fixFullUrls(Bundle bundle) {
        for (BundleEntryComponent bundleEntryComponent : bundle.getEntry()) {
            String fhirUrl = getFhirUrl();
            String resourceName = bundleEntryComponent.getResource().getResourceType().name();
            try {
                String fullUrl = bundleEntryComponent.getFullUrl();
                Matcher m = GET_UUID.matcher(fullUrl);
                if (m.matches()) {
                    String uuid = m.group(1);
                    String newFullUrl = fhirUrl + resourceName + "/" + uuid;
                    bundleEntryComponent.setFullUrl(newFullUrl);
                    bundleEntryComponent.getResource().setId(uuid);
                }
            } catch (Exception e) {
                String uuid = UUID.randomUUID().toString();
                String newFullUrl = fhirUrl + resourceName + "/" + uuid;
                bundleEntryComponent.setFullUrl(newFullUrl);
                bundleEntryComponent.getResource().setId(uuid);
                e.printStackTrace();
            }
        }
    }

    public static String getIdFor(Bundle bundle, String resource) {
        return bundle.getEntry().stream()
            .filter(e -> e.getResource().fhirType().equals(resource))
            .findAny()
            .get()
            .getResource().getId();
    }

    public static void addComposition(Bundle bundle) {

        String patientId = getIdFor(bundle, "Patient");

        String practitionerId = getIdFor(bundle, "Practitioner");

        String organizationId = getIdFor(bundle, "Coverage");

        String medicationRequestId = getIdFor(bundle, "MedicationRequest");

        String coverageId = getIdFor(bundle, "Coverage");

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
            .setValue("Y/400/1904/36/112");

        composition.setTitle("elektronische Arzneimittelverordnung");

        composition.addAttester()
            .setMode(Composition.CompositionAttestationMode.LEGAL)
            .getParty().setReference("Practitioner/" + practitionerId);

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

        BundleEntryComponent bundleEntryComponent = new BundleEntryComponent();
        bundleEntryComponent.setFullUrl("http://pvs.praxis.local/fhir/Composition/" + composition.getId());
        bundleEntryComponent.setResource(composition);
        bundle.addEntry(bundleEntryComponent);
        List<BundleEntryComponent> myList = bundle.getEntry();
        // make Composition the first element
        Collections.swap(myList, 0, myList.size() - 1);
    }
}