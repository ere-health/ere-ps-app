package health.ere.ps.validation.fhir.structuredefinition.fhir.kbv.de.v1_0_1.extension;

public class KBV_EX_ERP_Medication_Category {
    public final static String EXTENSION_STRUCTURE_DEFINITION_XML = "<StructureDefinition xmlns=\"http://hl7.org/fhir\">\n" +
            "    <url value=\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category\" />\n" +
            "    <version value=\"1.0.1\" />\n" +
            "    <name value=\"KBV_EX_ERP_Medication_Category\" />\n" +
            "    <status value=\"active\" />\n" +
            "    <date value=\"2021-02-23\" />\n" +
            "    <publisher value=\"Kassen&#228;rztliche Bundesvereinigung\" />\n" +
            "    <fhirVersion value=\"4.0.1\" />\n" +
            "    <mapping>\n" +
            "        <identity value=\"rim\" />\n" +
            "        <uri value=\"http://hl7.org/v3\" />\n" +
            "        <name value=\"RIM Mapping\" />\n" +
            "    </mapping>\n" +
            "    <kind value=\"complex-type\" />\n" +
            "    <abstract value=\"false\" />\n" +
            "    <context>\n" +
            "        <type value=\"element\" />\n" +
            "        <expression value=\"Medication\" />\n" +
            "    </context>\n" +
            "    <type value=\"Extension\" />\n" +
            "    <baseDefinition value=\"http://hl7.org/fhir/StructureDefinition/Extension\" />\n" +
            "    <derivation value=\"constraint\" />\n" +
            "    <differential>\n" +
            "        <element id=\"Extension\">\n" +
            "            <path value=\"Extension\" />\n" +
            "            <short value=\"Kategorie\" />\n" +
            "            <definition value=\"Extension zur Kennzeichnung der Kategorie einer Verordnung\" />\n" +
            "        </element>\n" +
            "        <element id=\"Extension.url\">\n" +
            "            <path value=\"Extension.url\" />\n" +
            "            <fixedUri value=\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category\" />\n" +
            "        </element>\n" +
            "        <element id=\"Extension.value[x]\">\n" +
            "            <path value=\"Extension.value[x]\" />\n" +
            "            <slicing>\n" +
            "                <discriminator>\n" +
            "                    <type value=\"type\" />\n" +
            "                    <path value=\"$this\" />\n" +
            "                </discriminator>\n" +
            "                <rules value=\"closed\" />\n" +
            "            </slicing>\n" +
            "            <min value=\"1\" />\n" +
            "            <type>\n" +
            "                <code value=\"Coding\" />\n" +
            "            </type>\n" +
            "        </element>\n" +
            "        <element id=\"Extension.value[x]:valueCoding\">\n" +
            "            <path value=\"Extension.value[x]\" />\n" +
            "            <sliceName value=\"valueCoding\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <type>\n" +
            "                <code value=\"Coding\" />\n" +
            "            </type>\n" +
            "            <binding>\n" +
            "                <strength value=\"required\" />\n" +
            "                <valueSet value=\"https://fhir.kbv.de/ValueSet/KBV_VS_ERP_Medication_Category\" />\n" +
            "            </binding>\n" +
            "        </element>\n" +
            "        <element id=\"Extension.value[x]:valueCoding.system\">\n" +
            "            <path value=\"Extension.value[x].system\" />\n" +
            "            <min value=\"1\" />\n" +
            "        </element>\n" +
            "        <element id=\"Extension.value[x]:valueCoding.version\">\n" +
            "            <path value=\"Extension.value[x].version\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Extension.value[x]:valueCoding.code\">\n" +
            "            <path value=\"Extension.value[x].code\" />\n" +
            "            <short value=\"Kategorie\" />\n" +
            "            <definition value=\"Kennzeichnung der Verordnungskategorie\" />\n" +
            "            <min value=\"1\" />\n" +
            "        </element>\n" +
            "        <element id=\"Extension.value[x]:valueCoding.display\">\n" +
            "            <extension url=\"http://hl7.org/fhir/StructureDefinition/elementdefinition-translatable\">\n" +
            "                <valueBoolean value=\"true\" />\n" +
            "            </extension>\n" +
            "            <path value=\"Extension.value[x].display\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Extension.value[x]:valueCoding.userSelected\">\n" +
            "            <path value=\"Extension.value[x].userSelected\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "    </differential>\n" +
            "</StructureDefinition>";
}
