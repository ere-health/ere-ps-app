package health.ere.ps.validation.fhir.structuredefinition.fhir.kbv.de.v1_0_1;

public class KBV_PR_ERP_Medication_PZN {
    public final static String STRUCTURE_DEFINITION_XML = "<StructureDefinition xmlns=\"http://hl7.org/fhir\">\n" +
            "    <url value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN\" />\n" +
            "    <version value=\"1.0.1\" />\n" +
            "    <name value=\"KBV_PR_ERP_Medication_PZN\" />\n" +
            "    <status value=\"active\" />\n" +
            "    <date value=\"2021-02-23\" />\n" +
            "    <publisher value=\"Kassen&#228;rztliche Bundesvereinigung\" />\n" +
            "    <fhirVersion value=\"4.0.1\" />\n" +
            "    <mapping>\n" +
            "        <identity value=\"script10.6\" />\n" +
            "        <uri value=\"http://ncpdp.org/SCRIPT10_6\" />\n" +
            "        <name value=\"Mapping to NCPDP SCRIPT 10.6\" />\n" +
            "    </mapping>\n" +
            "    <mapping>\n" +
            "        <identity value=\"rim\" />\n" +
            "        <uri value=\"http://hl7.org/v3\" />\n" +
            "        <name value=\"RIM Mapping\" />\n" +
            "    </mapping>\n" +
            "    <mapping>\n" +
            "        <identity value=\"w5\" />\n" +
            "        <uri value=\"http://hl7.org/fhir/fivews\" />\n" +
            "        <name value=\"FiveWs Pattern Mapping\" />\n" +
            "    </mapping>\n" +
            "    <mapping>\n" +
            "        <identity value=\"v2\" />\n" +
            "        <uri value=\"http://hl7.org/v2\" />\n" +
            "        <name value=\"HL7 v2 Mapping\" />\n" +
            "    </mapping>\n" +
            "    <kind value=\"resource\" />\n" +
            "    <abstract value=\"false\" />\n" +
            "    <type value=\"Medication\" />\n" +
            "    <baseDefinition value=\"http://hl7.org/fhir/StructureDefinition/Medication\" />\n" +
            "    <derivation value=\"constraint\" />\n" +
            "    <differential>\n" +
            "        <element id=\"Medication\">\n" +
            "            <path value=\"Medication\" />\n" +
            "            <constraint>\n" +
            "                <key value=\"-erp-NormgroesseOderMenge\" />\n" +
            "                <severity value=\"error\" />\n" +
            "                <human value=\"Packungsgr&#246;&#223;e oder Normgr&#246;&#223;e m&#252;ssen mindestens angegeben sein\" />\n" +
            "                <expression value=\"extension(&#39;http://fhir.de/StructureDefinition/normgroesse&#39;).exists() or amount.exists()\" />\n" +
            "                <source value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN\" />\n" +
            "            </constraint>\n" +
            "        </element>\n" +
            "        <element id=\"Medication.id\">\n" +
            "            <path value=\"Medication.id\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.meta\">\n" +
            "            <path value=\"Medication.meta\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.meta.versionId\">\n" +
            "            <path value=\"Medication.meta.versionId\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.meta.lastUpdated\">\n" +
            "            <path value=\"Medication.meta.lastUpdated\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.meta.source\">\n" +
            "            <path value=\"Medication.meta.source\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.meta.profile\">\n" +
            "            <path value=\"Medication.meta.profile\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <max value=\"1\" />\n" +
            "            <fixedCanonical value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.meta.security\">\n" +
            "            <path value=\"Medication.meta.security\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.meta.tag\">\n" +
            "            <path value=\"Medication.meta.tag\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.implicitRules\">\n" +
            "            <path value=\"Medication.implicitRules\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.language\">\n" +
            "            <path value=\"Medication.language\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.text\">\n" +
            "            <path value=\"Medication.text\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.contained\">\n" +
            "            <path value=\"Medication.contained\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.extension\">\n" +
            "            <path value=\"Medication.extension\" />\n" +
            "            <slicing>\n" +
            "                <discriminator>\n" +
            "                    <type value=\"value\" />\n" +
            "                    <path value=\"url\" />\n" +
            "                </discriminator>\n" +
            "                <rules value=\"open\" />\n" +
            "            </slicing>\n" +
            "        </element>\n" +
            "        <element id=\"Medication.extension:kategorie\">\n" +
            "            <path value=\"Medication.extension\" />\n" +
            "            <sliceName value=\"kategorie\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <max value=\"1\" />\n" +
            "            <type>\n" +
            "                <code value=\"Extension\" />\n" +
            "                <profile value=\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category\" />\n" +
            "            </type>\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.extension:kategorie.value[x]\">\n" +
            "            <path value=\"Medication.extension.value[x]\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.extension:kategorie.value[x]:valueCoding\">\n" +
            "            <path value=\"Medication.extension.value[x]\" />\n" +
            "            <sliceName value=\"valueCoding\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.extension:kategorie.value[x]:valueCoding.system\">\n" +
            "            <path value=\"Medication.extension.value[x].system\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.extension:kategorie.value[x]:valueCoding.code\">\n" +
            "            <path value=\"Medication.extension.value[x].code\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.extension:impfstoff\">\n" +
            "            <path value=\"Medication.extension\" />\n" +
            "            <sliceName value=\"impfstoff\" />\n" +
            "            <type>\n" +
            "                <code value=\"Extension\" />\n" +
            "                <profile value=\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine\" />\n" +
            "            </type>\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.extension:impfstoff.value[x]:valueBoolean\">\n" +
            "            <path value=\"Medication.extension.value[x]\" />\n" +
            "            <sliceName value=\"valueBoolean\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.extension:normgroesse\">\n" +
            "            <path value=\"Medication.extension\" />\n" +
            "            <sliceName value=\"normgroesse\" />\n" +
            "            <short value=\"Therapiegerechte Packungsgr&#246;&#223;e nach N-Bezeichnung\" />\n" +
            "            <definition value=\"Beschreibung der therapiegerechten Packungsgr&#246;&#223;e (z. B. N1)\" />\n" +
            "            <max value=\"1\" />\n" +
            "            <type>\n" +
            "                <code value=\"Extension\" />\n" +
            "                <profile value=\"http://fhir.de/StructureDefinition/normgroesse\" />\n" +
            "            </type>\n" +
            "            <condition value=\"NormgroesseOderMenge\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.extension:normgroesse.value[x]\">\n" +
            "            <path value=\"Medication.extension.value[x]\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.identifier\">\n" +
            "            <path value=\"Medication.identifier\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.code\">\n" +
            "            <path value=\"Medication.code\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.code.coding\">\n" +
            "            <path value=\"Medication.code.coding\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <max value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.code.coding.system\">\n" +
            "            <path value=\"Medication.code.coding.system\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <fixedUri value=\"http://fhir.de/CodeSystem/ifa/pzn\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.code.coding.version\">\n" +
            "            <path value=\"Medication.code.coding.version\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.code.coding.code\">\n" +
            "            <path value=\"Medication.code.coding.code\" />\n" +
            "            <short value=\"ID des Produktes (PZN)\" />\n" +
            "            <definition value=\"Pharmazentralnummer (PZN), die von der Informationsstelle f&#252;r Arzneispezialit&#228;ten (IFA) produktbezogen ver wird und f&#252;r die gesetzlichen Krankenkassen gem&#228;&#223; Vereinbarung nach &#167; 131 SGB V mit der Pharmazeutischen Industire und nach &#167; 300 dem Deutschen Apothekerverband vereinbart ist.\\nDie Angaben Handelsname, Darreichungsform, Packungsgr&#246;&#223;e usw. entstammen dem Preis- und Produktngaben nach &#167;131 Abs. 4 SGB V.\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.code.coding.display\">\n" +
            "            <extension url=\"http://hl7.org/fhir/StructureDefinition/elementdefinition-translatable\">\n" +
            "                <valueBoolean value=\"true\" />\n" +
            "            </extension>\n" +
            "            <path value=\"Medication.code.coding.display\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.code.coding.userSelected\">\n" +
            "            <path value=\"Medication.code.coding.userSelected\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.code.text\">\n" +
            "            <extension url=\"http://hl7.org/fhir/StructureDefinition/elementdefinition-translatable\">\n" +
            "                <valueBoolean value=\"true\" />\n" +
            "            </extension>\n" +
            "            <path value=\"Medication.code.text\" />\n" +
            "            <short value=\"Handelsname\" />\n" +
            "            <definition value=\"Handelsname des verordneten Pr&#228;parates, aus der PZN abgeleitet\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <maxLength value=\"50\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.status\">\n" +
            "            <path value=\"Medication.status\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.manufacturer\">\n" +
            "            <path value=\"Medication.manufacturer\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.form\">\n" +
            "            <path value=\"Medication.form\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "            <binding>\n" +
            "                <extension url=\"http://hl7.org/fhir/StructureDefinition/elementdefinition-bindingName\">\n" +
            "                    <valueString value=\"MedicationForm\" />\n" +
            "                </extension>\n" +
            "                <strength value=\"required\" />\n" +
            "                <valueSet value=\"https://fhir.kbv.de/ValueSet/KBV_VS_SFHIR_KBV_DARREICHUNGSFORM\" />\n" +
            "            </binding>\n" +
            "        </element>\n" +
            "        <element id=\"Medication.form.coding\">\n" +
            "            <path value=\"Medication.form.coding\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <max value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.form.coding.system\">\n" +
            "            <path value=\"Medication.form.coding.system\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.form.coding.version\">\n" +
            "            <path value=\"Medication.form.coding.version\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.form.coding.code\">\n" +
            "            <path value=\"Medication.form.coding.code\" />\n" +
            "            <short value=\"Code der Darreichungsform\" />\n" +
            "            <definition value=\"Darreichungsform entsprechend der Daten nach &#167; 131 Abs. 4 SGB V\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.form.coding.display\">\n" +
            "            <extension url=\"http://hl7.org/fhir/StructureDefinition/elementdefinition-translatable\">\n" +
            "                <valueBoolean value=\"true\" />\n" +
            "            </extension>\n" +
            "            <path value=\"Medication.form.coding.display\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.form.coding.userSelected\">\n" +
            "            <path value=\"Medication.form.coding.userSelected\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.form.text\">\n" +
            "            <extension url=\"http://hl7.org/fhir/StructureDefinition/elementdefinition-translatable\">\n" +
            "                <valueBoolean value=\"true\" />\n" +
            "            </extension>\n" +
            "            <path value=\"Medication.form.text\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.amount\">\n" +
            "            <path value=\"Medication.amount\" />\n" +
            "            <condition value=\"NormgroesseOderMenge\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.amount.numerator\">\n" +
            "            <path value=\"Medication.amount.numerator\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <condition value=\"SystemundCode\" />\n" +
            "            <constraint>\n" +
            "                <key value=\"-erp-begrenzungValue\" />\n" +
            "                <severity value=\"error\" />\n" +
            "                <human value=\"Die Packungsgr&#246;&#223;e darf aus maximal 7 Zeichen bestehen\" />\n" +
            "                <expression value=\"value.toString().length()&lt;=7\" />\n" +
            "                <source value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN\" />\n" +
            "            </constraint>\n" +
            "            <constraint>\n" +
            "                <key value=\"-erp-codeUndSystem\" />\n" +
            "                <severity value=\"error\" />\n" +
            "                <human value=\"Wenn ein Code eingegeben ist ,muss auch das System hinterlegt sein.\" />\n" +
            "                <expression value=\"code.exists() implies system.exists()\" />\n" +
            "                <source value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN\" />\n" +
            "            </constraint>\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.amount.numerator.value\">\n" +
            "            <path value=\"Medication.amount.numerator.value\" />\n" +
            "            <short value=\"Packungsgr&#246;&#223;e nach abgeteilter Menge\" />\n" +
            "            <definition value=\"Menge der Packungsgr&#246;&#223;e (z.B. 100). Tritt nur in Verbindung mit Angabe der Einheit auf - z.B. 100 St.\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <condition value=\"begrenzungValue\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.amount.numerator.comparator\">\n" +
            "            <path value=\"Medication.amount.numerator.comparator\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.amount.numerator.unit\">\n" +
            "            <extension url=\"http://hl7.org/fhir/StructureDefinition/elementdefinition-translatable\">\n" +
            "                <valueBoolean value=\"true\" />\n" +
            "            </extension>\n" +
            "            <path value=\"Medication.amount.numerator.unit\" />\n" +
            "            <short value=\"Einheit\" />\n" +
            "            <definition value=\"Einheit der Packungsgr&#246;&#223;e (z.B. St.). Tritt nur in Verbindung mit „Packungsgr&#246;&#223;e nach abgeteilter Menge“ auf - z.B. 100 St.\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <maxLength value=\"12\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.amount.numerator.system\">\n" +
            "            <path value=\"Medication.amount.numerator.system\" />\n" +
            "            <fixedUri value=\"http://unitsofmeasure.org\" />\n" +
            "            <condition value=\"codeUndSystem\" />\n" +
            "            <mustSupport value=\"false\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.amount.numerator.code\">\n" +
            "            <path value=\"Medication.amount.numerator.code\" />\n" +
            "            <short value=\"Packungseinheit (kodiert)\" />\n" +
            "            <definition value=\"optionale kodierte Angabe zur Einheit der Packung gem&#228;&#223; unitsofmeasure.org\" />\n" +
            "            <condition value=\"codeUndSystem\" />\n" +
            "            <mustSupport value=\"false\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.amount.denominator\">\n" +
            "            <path value=\"Medication.amount.denominator\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.amount.denominator.value\">\n" +
            "            <path value=\"Medication.amount.denominator.value\" />\n" +
            "            <min value=\"1\" />\n" +
            "            <fixedDecimal value=\"1\" />\n" +
            "            <mustSupport value=\"true\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.amount.denominator.comparator\">\n" +
            "            <path value=\"Medication.amount.denominator.comparator\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.amount.denominator.unit\">\n" +
            "            <extension url=\"http://hl7.org/fhir/StructureDefinition/elementdefinition-translatable\">\n" +
            "                <valueBoolean value=\"true\" />\n" +
            "            </extension>\n" +
            "            <path value=\"Medication.amount.denominator.unit\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.amount.denominator.system\">\n" +
            "            <path value=\"Medication.amount.denominator.system\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.amount.denominator.code\">\n" +
            "            <path value=\"Medication.amount.denominator.code\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "        <element id=\"Medication.ingredient\">\n" +
            "            <path value=\"Medication.ingredient\" />\n" +
            "            <max value=\"0\" />\n" +
            "        </element>\n" +
            "    </differential>\n" +
            "</StructureDefinition>";
}
