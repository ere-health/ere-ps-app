package health.ere.ps.validation.fhir.codesystem;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Enumerations;

public class IdentifierTypeDeBasisCodeSystem extends CodeSystem {
    public IdentifierTypeDeBasisCodeSystem() {
        setId("identifier-type-de-basis");

        getMeta().addProfile("http://hl7.org/fhir/StructureDefinition/shareablecodesystem");

        setUrl("http://fhir.de/CodeSystem/identifier-type-de-basis");
        setStatus(Enumerations.PublicationStatus.ACTIVE);
        setExperimental(false);
        setCaseSensitive(true);
        setContent(CodeSystemContentMode.COMPLETE);

        addConcept().setCode("GKV");
    }
}
