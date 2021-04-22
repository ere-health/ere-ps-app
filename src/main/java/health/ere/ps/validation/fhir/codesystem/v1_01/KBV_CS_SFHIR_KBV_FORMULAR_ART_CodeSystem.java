package health.ere.ps.validation.fhir.codesystem.v1_01;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Enumerations;

public class KBV_CS_SFHIR_KBV_FORMULAR_ART_CodeSystem extends CodeSystem {

    public KBV_CS_SFHIR_KBV_FORMULAR_ART_CodeSystem() {
        setUrl("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_FORMULAR_ART");

        addIdentifier()
                .setSystem("urn:ietf:rfc:3986")
                .setValue("urn:oid:1.2.276.0.76.3.1.1.5.2.58");
        setStatus(Enumerations.PublicationStatus.ACTIVE);
        setCaseSensitive(true);
        setContent(CodeSystemContentMode.COMPLETE);
        addConcept().setCode("e16A");
    }
}
