package health.ere.ps.service.fhir.prescription;

import jakarta.enterprise.context.ApplicationScoped;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Quantity;

import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.DAY;

@ApplicationScoped
public class MedicationPrescriptionProcessor extends XmlPrescriptionProcessor {

    @Override
    protected String getFhirUrl() {
        return "http://pvs.praxis.local/fhir/";
    }

    @Override
    protected void fixCompositionSections(Bundle bundle, Composition composition) {
        String medicationRequestId = getIdFor(bundle, "MedicationRequest");
        // String medicationId = getIdFor(bundle, "Medication");
        String coverageId = getIdFor(bundle, "Coverage");

        if (!composition.getSection().isEmpty()) {
            composition.getSection().get(0).getEntry().get(0).setReference("MedicationRequest/" + medicationRequestId);
        }
        if (composition.getSection().size() > 1) {
            composition.getSection().get(1).getEntry().get(0).setReference("Coverage/" + coverageId);
        }
    }

    @Override
    protected void adjustTypeEntries(Bundle bundle, Practitioner practitioner, Patient patient) {
        // Next issue ERROR - Bundle.entry[1].resource.ofType(MedicationRequest).dispenseRequest - MedicationRequest.dispenseRequest.quantity: minimum required = 1, but only found 0 (from https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.1.0)

        MedicationRequest medicationRequest = getTypeFromBundle(MedicationRequest.class, bundle);
        medicationRequest.getAuthoredOnElement().setPrecision(DAY);

        if (medicationRequest.getDispenseRequest().getQuantity().getValue() == null) {
            Quantity quantity = new Quantity();
            quantity.setValue(1);
            quantity.setSystem("http://unitsofmeasure.org");
            quantity.setCode("{Package}");
            medicationRequest.getDispenseRequest().setQuantity(quantity);
        }

        // Next issue ERROR - Bundle.entry[2].resource.ofType(Medication) - -erp-NormgroesseOderMenge: 'Packungsgröße oder Normgröße müssen mindestens angegeben sein' Rule 'Packungsgröße oder Normgröße müssen mindestens angegeben sein' Failed

        Medication medication = getTypeFromBundle(Medication.class, bundle);

        // Next issue WARNING - Bundle.entry[1].resource.ofType(MedicationRequest).medication.ofType(Reference) - URN reference ist nicht lokal innerhalb des Bundles contained urn:uuid:79804138-e125-4a76-87e7-5ebad33d4a70
        medicationRequest.getMedicationReference().setReference("Medication/" + medication.getIdElement().getIdPart());

        // Delete validity period
        medicationRequest.getDispenseRequest().setValidityPeriod(null);

        if (medication.getExtensionByUrl("http://fhir.de/StructureDefinition/normgroesse") == null) {
            Extension normgroesse = new Extension("http://fhir.de/StructureDefinition/normgroesse", new CodeType("N1"));
            medication.addExtension(normgroesse);
        }

        // Next issue WARNING - Bundle.entry[1].resource.ofType(MedicationRequest).requester - URN reference ist nicht lokal innerhalb des Bundles contained urn:uuid:7d8c6815-896d-45f7-a264-007bfe54623e
        medicationRequest.getRequester().setReference("Practitioner/" + practitioner.getIdElement().getIdPart());

        // Next issue WARNING - Bundle.entry[1].resource.ofType(MedicationRequest).subject - URN reference ist nicht lokal innerhalb des Bundles contained urn:uuid:91c0f8d8-8af1-467f-8d09-0c8a406b0127
        medicationRequest.getSubject().setReference("Patient/" + patient.getIdElement().getIdPart());
    }
}
