package health.ere.ps.service.fhir.prescription;

import jakarta.enterprise.context.ApplicationScoped;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;

import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.DAY;

@ApplicationScoped
public class AppPrescriptionProcessor extends XmlPrescriptionProcessor {

    @Override
    protected String getFhirUrl() {
        return "http://pvs.praxis-topp-gluecklich.local/fhir/";
    }

    @Override
    protected void fixCompositionSections(Bundle bundle, Composition composition) {
        String deviceRequestId = getIdFor(bundle, "DeviceRequest");
        String coverageId = getIdFor(bundle, "Coverage");

        if (!composition.getSection().isEmpty()) {
            composition.getSection().get(0).getEntry().get(0).setReference("DeviceRequest/" + deviceRequestId);
        }
        if (composition.getSection().size() > 1) {
            composition.getSection().get(1).getEntry().get(0).setReference("Coverage/" + coverageId);
        }
    }

    @Override
    protected void adjustTypeEntries(Bundle bundle, Practitioner practitioner, Patient patient) {
        DeviceRequest deviceRequest = getTypeFromBundle(DeviceRequest.class, bundle);
        deviceRequest.getAuthoredOnElement().setPrecision(DAY);

        deviceRequest.getRequester().setReference("Practitioner/" + practitioner.getIdElement().getIdPart());
        deviceRequest.getSubject().setReference("Patient/" + patient.getIdElement().getIdPart());
    }
}
