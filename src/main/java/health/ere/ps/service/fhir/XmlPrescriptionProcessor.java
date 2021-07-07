package health.ere.ps.service.fhir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Practitioner.PractitionerQualificationComponent;

import ca.uhn.fhir.context.FhirContext;

public class XmlPrescriptionProcessor {
    static FhirContext fhirContext = FhirContext.forR4();

    // Get <Bundle> tag including content
    private static Pattern GET_BUNDLE = Pattern.compile("(<Bundle[^>]*>.*?</Bundle>)", Pattern.DOTALL);

    private static Pattern GET_UUID = Pattern.compile("^urn:uuid:(.*)");

    public static Bundle[] parseFromString(String xml) {
        List<Bundle> bundles = new ArrayList<>();
        Matcher m = GET_BUNDLE.matcher(xml);
        boolean found = false;
        while(m.find()) {
            found = true;
            String bundleXml = m.group(1);
            Bundle bundle = createFixedBundleFromString(bundleXml);
            bundles.add(bundle);
        }
        if (!found) {
            throw new WebApplicationException("Could not extract inner text", Status.NOT_ACCEPTABLE);
        }
        return bundles.toArray(new Bundle[] {});
        
    }

    public static Bundle createFixedBundleFromString(String bundleXml) {
        Bundle bundle = fhirContext.newXmlParser().parseResource(Bundle.class, bundleXml);
        fixFullUrls(bundle);
        fixRefencesInComposition(bundle);

        // INFO:  Next issue ERROR - Bundle.entry[1].resource.ofType(MedicationRequest).dispenseRequest - MedicationRequest.dispenseRequest.quantity: minimum required = 1, but only found 0 (from https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.1)
        
        MedicationRequest medicationRequest = getTypeFromBundle(MedicationRequest.class, bundle);

        if(medicationRequest.getDispenseRequest().getQuantity().getValue() == null) {
            Quantity quantity = new Quantity();
            quantity.setValue(1);
            quantity.setSystem("http://unitsofmeasure.org");
            quantity.setCode("{Package}");
            medicationRequest.getDispenseRequest().setQuantity(quantity);
        }
        
        // INFO:  Next issue ERROR - Bundle.entry[2].resource.ofType(Medication) - -erp-NormgroesseOderMenge: 'Packungsgröße oder Normgröße müssen mindestens angegeben sein' Rule 'Packungsgröße oder Normgröße müssen mindestens angegeben sein' Failed

        Medication medication = getTypeFromBundle(Medication.class, bundle);

        if(medication.getExtensionByUrl("http://fhir.de/StructureDefinition/normgroesse") == null) {
            Extension normgroesse = new Extension("http://fhir.de/StructureDefinition/normgroesse", new CodeType("N1"));
            medication.addExtension(normgroesse);
        }

        // INFO:  Next issue ERROR - Bundle.entry[4].resource.ofType(Practitioner) - Practitioner.qualification: minimum required = 2, but only found 1 (from https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3)
        // INFO:  Next issue ERROR - Bundle.entry[4].resource.ofType(Practitioner) - Practitioner.qualification:Berufsbezeichnung: minimum required = 1, but only found 0 (from https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3)

        Practitioner practitioner = getTypeFromBundle(Practitioner.class, bundle);

        if(practitioner.getQualification().size() == 1) {
            PractitionerQualificationComponent qualification = new PractitionerQualificationComponent();
            CodeableConcept qualificationCodeableConcept = new CodeableConcept();
            qualificationCodeableConcept.setText("Arzt");

            qualification.setCode(qualificationCodeableConcept);
            practitioner.addQualification(qualification);
        }

        // Error while decoding XML: Missing Field (id=value, path=/Bundle/entry/resource/Patient/identifier)!
        Patient patient = getTypeFromBundle(Patient.class, bundle);

        if(patient.getIdentifier().size() > 0 && !patient.getIdentifier().get(0).hasValue()) {
            patient.getIdentifier().get(0).setValue("X999999999");
        }


        // addComposition(bundle);
        return bundle;
    }

    private static <T> T getTypeFromBundle(Class<T> class1, Bundle bundle) {
        return (T) bundle.getEntry().stream().filter(b -> b.getResource().getResourceType().name().equals(class1.getSimpleName())).findAny().get().getResource();
    }

    static void fixRefencesInComposition(Bundle bundle) {
        String patientId = getIdFor(bundle, "Patient");
        String practitionerId = getIdFor(bundle, "Practitioner");
        String organizationId = getIdFor(bundle, "Organization");
        String medicationRequestId = getIdFor(bundle, "MedicationRequest");
        String coverageId = getIdFor(bundle, "Coverage");

        Composition composition = (Composition)bundle.getEntry().stream().findFirst().get().getResource();
        
        composition.getSubject().setReference("Patient/"+patientId);
        composition.getAuthor().get(0).setReference("Practitioner/"+practitionerId);
        composition.getCustodian().setReference("Organization/"+organizationId);
        composition.getSection().get(0).getEntry().get(0).setReference("MedicationRequest/"+medicationRequestId);
        composition.getSection().get(1).getEntry().get(0).setReference("Coverage/"+coverageId);
    }

    static void fixFullUrls(Bundle bundle) {
        for(BundleEntryComponent bundleEntryComponent : bundle.getEntry()) {
            String fullUrl = bundleEntryComponent.getFullUrl();
            Matcher m = GET_UUID.matcher(fullUrl);
            if(m.matches()) {
                String uuid = m.group(1);
                String newFullUrl = "http://pvs.praxis.local/fhir/"+bundleEntryComponent.getResource().getResourceType().name()+"/"+uuid;
                bundleEntryComponent.setFullUrl(newFullUrl);
                bundleEntryComponent.getResource().setId(uuid);
            }
        }
    }

    public static String getIdFor(Bundle bundle, String resource) {
        return bundle.getEntry().stream().filter(e -> e.getResource().fhirType().equals(resource)).findAny().get().getResource().getId();
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
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.0.1");

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
                .setValue("Y/1/1807/36/112");

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
        bundleEntryComponent.setFullUrl("http://pvs.praxis.local/fhir/Composition/"+composition.getId());
        bundleEntryComponent.setResource(composition);
        bundle.addEntry(bundleEntryComponent);
        List<BundleEntryComponent> myList = bundle.getEntry();
        // make Composition the first element
        Collections.swap(myList, 0, myList.size()-1);

    }
}