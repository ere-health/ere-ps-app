package health.ere.ps.validation.fhir.codesystem.v1_0_1;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Enumerations;

public class KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN_CodeSystem extends CodeSystem {

    public KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN_CodeSystem() {
        setUrl("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN");

        addIdentifier()
                .setSystem("urn:ietf:rfc:3986")
                .setSystem("urn:oid:1.2.276.0.76.5.484");

        setStatus(Enumerations.PublicationStatus.ACTIVE);
    }
}
