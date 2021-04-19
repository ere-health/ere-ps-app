package health.ere.ps.validation.fhir.structuredefinition.fhir.kbv.de.extension;

import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.StructureDefinition;

public class KBV_EX_FOR_Legal_basis_StructureDefinition extends StructureDefinition {

    public KBV_EX_FOR_Legal_basis_StructureDefinition() {
        setUrl("https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis");
        setStatus(Enumerations.PublicationStatus.ACTIVE);
        setKind(StructureDefinitionKind.COMPLEXTYPE);
        setAbstract(false);
        setType("Extension");
    }
}
