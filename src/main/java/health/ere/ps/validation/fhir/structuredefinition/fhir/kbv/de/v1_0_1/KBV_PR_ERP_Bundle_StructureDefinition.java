package health.ere.ps.validation.fhir.structuredefinition.fhir.kbv.de.v1_0_1;

import org.hl7.fhir.r4.model.StructureDefinition;

public class KBV_PR_ERP_Bundle_StructureDefinition extends StructureDefinition {
    public final static String STRUCTURE_DEFINITION_XML = "<StructureDefinition " +
            "xmlns=\"http://hl7.org/fhir\">\n" +
            "    <url value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle\" />\n" +
            "    <version value=\"1.0.1\" />\n" +
            "    <name value=\"KBV_PR_ERP_Bundle\" />\n" +
            "    <status value=\"active\" />\n" +
            "    <date value=\"2021-02-23\" />\n" +
            "    <publisher value=\"Kassen&#228;rztliche Bundesvereinigung\" />\n" +
            "    <fhirVersion value=\"4.0.1\" />\n" +
            "    <mapping>\n" +
            "        <identity value=\"v2\" />\n" +
            "        <uri value=\"http://hl7.org/v2\" />\n" +
            "        <name value=\"HL7 v2 Mapping\" />\n" +
            "    </mapping>\n" +
            "    <mapping>\n" +
            "        <identity value=\"rim\" />\n" +
            "        <uri value=\"http://hl7.org/v3\" />\n" +
            "        <name value=\"RIM Mapping\" />\n" +
            "    </mapping>\n" +
            "    <mapping>\n" +
            "        <identity value=\"cda\" />\n" +
            "        <uri value=\"http://hl7.org/v3/cda\" />\n" +
            "        <name value=\"CDA (R2)\" />\n" +
            "    </mapping>\n" +
            "    <mapping>\n" +
            "        <identity value=\"w5\" />\n" +
            "        <uri value=\"http://hl7.org/fhir/fivews\" />\n" +
            "        <name value=\"FiveWs Pattern Mapping\" />\n" +
            "    </mapping>\n" +
            "    <kind value=\"resource\" />\n" +
            "    <abstract value=\"false\" />\n" +
            "    <type value=\"Bundle\" />\n" +
            "    <baseDefinition value=\"http://hl7.org/fhir/StructureDefinition/Bundle\" />\n" +
            "    <derivation value=\"constraint\" />\n" +
            "    <differential>\n" +
            "        <element id=\"Bundle\">\n" +
            "            <path value=\"Bundle\" />\n" +
            "            <constraint>\n" +
            "                <key value=\"-erp-compositionPflicht\" />\n" +
            "                <severity value=\"error\" />\n" +
            "                <human value=\"Die Ressource vom Typ Composition muss genau einmal vorhanden sein\" />\n" +
            "                <expression value=\"entry.where(resource is Composition).count()=1\" />\n" +
            "                <source value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle\" />\n" +
            "            </constraint>\n" +
            "            <constraint>\n" +
            "                <key value=\"-erp-angabePruefnummer\" />\n" +
            "                <severity value=\"error\" />\n" +
            "                <human value=\"Pr&#252;fnummer nicht vorhanden, aber Pflicht bei den Kostentr&#228;ger der Typen &quot;GKV&quot;, &quot;BG&quot;, &quot;SKT&quot; oder &quot;UK&quot;\" />\n" +
            "                <expression value=\"(entry.where(resource is Coverage).exists() and (entry.where(resource is Coverage).resource.type.coding.code=&#39;GKV&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;BG&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;SKT&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;UK&#39;)) implies entry.where(resource is Composition).resource.author.identifier.where(system=&#39;https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer&#39;).exists()\" />\n" +
            "                <source value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle\" />\n" +
            "            </constraint>\n" +
            "            <constraint>\n" +
            "                <key value=\"-erp-angabeZuzahlung\" />\n" +
            "                <severity value=\"error\" />\n" +
            "                <human value=\"Zuzahlungsstatus nicht vorhanden, aber Pflicht bei den Kostentr&#228;gern der Typen &quot;GKV&quot;, &quot;BG&quot;, &quot;SKT&quot; oder &quot;UK&quot;\" />\n" +
            "                <expression value=\"(entry.where(resource is MedicationRequest).exists() and entry.where(resource is Coverage).exists() and (entry.where(resource is Coverage).resource.type.coding.code=&#39;GKV&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;BG&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;SKT&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;UK&#39;)) implies entry.where(resource is MedicationRequest).resource.extension(&#39;https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment&#39;).exists()\" />\n" +
            "                <source value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle\" />\n" +
            "            </constraint>\n" +
            "            <constraint>\n" +
            "                <key value=\"-erp-angabePLZ\" />\n" +
            "                <severity value=\"error\" />\n" +
            "                <human value=\"Postleitzahl nicht vorhanden, aber Pflicht bei den Kostentraegern der Typen &quot;GKV&quot;, &quot;BG&quot;, &quot;SKT&quot; oder &quot;UK&quot;\" />\n" +
            "                <expression value=\"(entry.where(resource is Patient).exists() and entry.where(resource is Coverage).exists() and (entry.where(resource is Coverage).resource.type.coding.code=&#39;GKV&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;BG&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;SKT&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;UK&#39;)) implies entry.where(resource is Patient).resource.address.postalCode.exists()\" />\n" +
            "                <source value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle\" />\n" +
            "            </constraint>\n" +
            "            <constraint>\n" +
            "                <key value=\"-erp-angabeNrAusstellendePerson\" />\n" +
            "                <severity value=\"error\" />\n" +
            "                <human value=\"Nummer der ausstellenden Person nicht vorhanden, aber Pflicht, wenn es sich um einen Arzt oder Zahnarzt handelt\" />\n" +
            "                <expression value=\"(entry.where(resource is Coverage).exists() and (entry.where(resource is Coverage).resource.type.coding.code=&#39;GKV&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;BG&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;SKT&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;UK&#39;) and (entry.where(resource is Practitioner).resource.qualification.coding.code=&#39;00&#39; or entry.where(resource is Practitioner).resource.qualification.coding.code=&#39;01&#39;)) implies entry.where(resource is Practitioner).resource.identifier.exists()\" />\n" +
            "                <source value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle\" />\n" +
            "            </constraint>\n" +
            "            <constraint>\n" +
            "                <key value=\"-erp-angabeBestriebsstaettennr\" />\n" +
            "                <severity value=\"error\" />\n" +
            "                <human value=\"Betriebsstaettennummer nicht vorhanden, aber Pflicht, wenn es sich um einen Arzt, Zahnarzt oder Arzt in Weiterbildung handelt\" />\n" +
            "                <expression value=\"(entry.where(resource is Coverage).exists() and (entry.where(resource is Coverage).resource.type.coding.code=&#39;GKV&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;BG&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;SKT&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;UK&#39;) and (entry.where(resource is Practitioner).resource.qualification.coding.code=&#39;00&#39; or entry.where(resource is Practitioner).resource.qualification.coding.code=&#39;01&#39; or entry.where(resource is Practitioner).resource.qualification.coding.code=&#39;03&#39;)) implies entry.where(resource is Organization).resource.identifier.exists()\" />\n" +
            "                <source value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle\" />\n" +
            "            </constraint>\n" +
            "            <constraint>\n" +
            "                <key value=\"-erp-angabeRechtsgrundlage\" />\n" +
            "                <severity value=\"error\" />\n" +
            "                <human value=\"Rechtsgrundlage nicht vorhanden, aber Pflicht bei den Kostentraegern der Typen &quot;GKV&quot;, &quot;BG&quot;, &quot;SKT&quot; oder &quot;UK&quot;\" />\n" +
            "                <expression value=\"(entry.where(resource is MedicationRequest).exists() and entry.where(resource is Coverage).exists() and (entry.where(resource is Coverage).resource.type.coding.code=&#39;GKV&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;BG&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;SKT&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;UK&#39;)) implies entry.where(resource is Composition).resource.extension(&#39;https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis&#39;).exists()\" />\n" +
            "                <source value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle\" />\n" +
            "            </constraint>\n" +
            "            <constraint>\n" +
            "                <key value=\"-erp-versionComposition\" />\n" +
            "                <severity value=\"error\" />\n" +
            "                <human value=\"Die Instanz der Composition muss vom Profil KBV_PR_ERP_Composition|1.0.1 sein\" />\n" +
            "                <expression value=\"entry.where(resource is Composition).resource.meta.profile = &#39;https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.0.1&#39;\" />\n" +
            "                <source value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle\" />\n" +
            "            </constraint>\n" +
            "            <constraint>\n" +
            "                <key value=\"IK-Kostentraeger-BG-UK-Pflicht\" />\n" +
            "                <severity value=\"error\" />\n" +
            "                <human value=\"BG-Pflicht\" />\n" +
            "                <expression value=\"(entry.where(resource is Coverage).exists() and (entry.where(resource is Coverage).resource.type.coding.code=&#39;BG&#39; or entry.where(resource is Coverage).resource.type.coding.code=&#39;UK&#39;)) implies  entry.select(resource as Coverage).payor.identifier.extension(&#39;https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Alternative_IK&#39;).exists()\" />\n" +
            "                <source value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle\" />\n" +
            "            </constraint>\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.id\">\n" +
            "            <path value=\"Bundle.id\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.meta\">\n" +
            "            <path value=\"Bundle.meta\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.meta.versionId\">\n" +
            "            <path value=\"Bundle.meta.versionId\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.meta.lastUpdated\">\n" +
            "            <path value=\"Bundle.meta.lastUpdated\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.meta.source\">\n" +
            "            <path value=\"Bundle.meta.source\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.meta.profile\">\n" +
            "            <path value=\"Bundle.meta.profile\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <max value=\"1\" />\n" +
            "            <fixedCanonical value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.meta.security\">\n" +
            "            <path value=\"Bundle.meta.security\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.meta.tag\">\n" +
            "            <path value=\"Bundle.meta.tag\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.implicitRules\">\n" +
            "            <path value=\"Bundle.implicitRules\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.language\">\n" +
            "            <path value=\"Bundle.language\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.identifier\">\n" +
            "            <path value=\"Bundle.identifier\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.identifier.use\">\n" +
            "            <path value=\"Bundle.identifier.use\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.identifier.type\">\n" +
            "            <path value=\"Bundle.identifier.type\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.identifier.system\">\n" +
            "            <path value=\"Bundle.identifier.system\" />\n" +
            "            <definition value=\"F&#252;r den digitalen Vordruck &quot;Elektronische Arzneimittelverordnung (Vordruck e160)&quot; wird die Dokumenten-ID gem&#228;&#223; den Vorgaben der gematik gesetzt.\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <fixedUri value=\"https://gematik.de/fhir/NamingSystem/PrescriptionID\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.identifier.value\">\n" +
            "            <path value=\"Bundle.identifier.value\" />\n" +
            "            <short value=\"Dokumenten-ID\" />\n" +
            "            <definition value=\"Eindeutige Identifikation des Dokuments, bzw. der Verordnung. Diese ID wird mit signiert und sichert damit die Verordnung vor F&#228;lschungen.\\n\\nF&#252;r den digitalen Vordruck &quot;Elektronische Arzneimittelverordnung (Vordruck e160)&quot; wird die Dokumenten-ID gem&#228;&#223; den Vorgaben der gematik gesetzt.\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.identifier.period\">\n" +
            "            <path value=\"Bundle.identifier.period\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.identifier.assigner\">\n" +
            "            <path value=\"Bundle.identifier.assigner\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.type\">\n" +
            "            <path value=\"Bundle.type\" />\n" +
            "            <fixedCode value=\"document\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.timestamp\">\n" +
            "            <path value=\"Bundle.timestamp\" />\n" +
            "            <definition value=\"DateTime wann das Dokument technisch erstellt wurde. \\nFachlich bildet das Ausstellungsdatum das entscheidende Datum ab\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.total\">\n" +
            "            <path value=\"Bundle.total\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.link\">\n" +
            "            <path value=\"Bundle.link\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.entry\">\n" +
            "            <path value=\"Bundle.entry\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.entry.link\">\n" +
            "            <path value=\"Bundle.entry.link\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.entry.fullUrl\">\n" +
            "            <path value=\"Bundle.entry.fullUrl\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.entry.resource\">\n" +
            "            <path value=\"Bundle.entry.resource\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.entry.search\">\n" +
            "            <path value=\"Bundle.entry.search\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.entry.request\">\n" +
            "            <path value=\"Bundle.entry.request\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Bundle.entry.response\">\n" +
            "            <path value=\"Bundle.entry.response\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "    </differential>\n" +
            "</StructureDefinition>";

    public KBV_PR_ERP_Bundle_StructureDefinition() {
//        setUrl("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle");
//        setStatus(Enumerations.PublicationStatus.ACTIVE);
//        setKind(StructureDefinition.StructureDefinitionKind.RESOURCE);
//        setAbstract(false);
//        setType("Bundle");
    }
}
