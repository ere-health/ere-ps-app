package health.ere.ps.service.gematik;

import org.hl7.fhir.r4.model.Bundle;

public record PrescriptionContext(Bundle bundle, String eventId, String readVSDResponse) {
}
