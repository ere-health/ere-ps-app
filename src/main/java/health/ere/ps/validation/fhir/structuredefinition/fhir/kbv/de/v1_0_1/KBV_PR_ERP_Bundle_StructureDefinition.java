package health.ere.ps.validation.fhir.structuredefinition.fhir.kbv.de.v1_0_1;

import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.StructureDefinition;

public class KBV_PR_ERP_Bundle_StructureDefinition extends StructureDefinition {

    public KBV_PR_ERP_Bundle_StructureDefinition() {
        setUrl("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle");
        setStatus(Enumerations.PublicationStatus.ACTIVE);
        setKind(StructureDefinition.StructureDefinitionKind.RESOURCE);
        setAbstract(false);
        setType("Bundle");
    }
}
