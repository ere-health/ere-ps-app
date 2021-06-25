package health.ere.ps.validation.fhir.context.support;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class ErePrePopulatedValidationSupport extends PrePopulatedValidationSupport {
    Logger logger = Logger.getLogger(ErePrePopulatedValidationSupport.class);

    protected static final String URL_PREFIX_STRUCTURE_DEFINITION = "https://fhir.kbv" +
            ".de/StructureDefinition/";
    protected static final String URL_PREFIX_VALUE_SET_DEFINITION = "https://fhir.kbv" +
            ".de/ValueSet/";
    protected static final String URL_PREFIX_CODE_SYSTEM_DEFINITION = "https://fhir.kbv" +
            ".de/CodeSystem/";
    protected static final String URL_PREFIX_NAMING_SYSTEM_DEFINITION = "https://fhir.kbv" +
            ".de/NamingSystem/";
    protected static final String URL_PREFIX_STRUCTURE_DEFINITION_BASE = "http://hl7.org/fhir/";

    protected List<List<String>> structureDefinitionFilePaths = Arrays.asList(
            List.of("/fhir/r4/profile/v1_0_1/KBV_PR_ERP_Bundle.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.1", "1.0.1"),
            List.of("/fhir/r4/profile/v1_0_1/KBV_PR_ERP_Composition.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.0.1", "1.0" +
                            ".1"),
            List.of("/fhir/r4/profile/v1_0_1/KBV_PR_ERP_Medication_Compounding.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Compounding|1" +
                            ".0.1", "1.0.1"),
            List.of("/fhir/r4/profile/v1_0_1/KBV_PR_ERP_Medication_FreeText.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText|1.0.1",
                    "1.0.1"),
            List.of("/fhir/r4/profile/v1_0_1/KBV_PR_ERP_Medication_Ingredient.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient|1.0" +
                            ".1", "1.0.1"),
            List.of("/fhir/r4/profile/v1_0_1/KBV_PR_ERP_Medication_PZN.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.1", "1" +
                            ".0.1"),
            List.of("/fhir/r4/profile/v1_0_1/KBV_PR_ERP_PracticeSupply.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_PracticeSupply|1.0.1", "1" +
                            ".0.1"),
            List.of("/fhir/r4/profile/v1_0_1/KBV_PR_ERP_Prescription.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.1", "1.0" +
                            ".1"),

            List.of("/fhir/r4/profile/v1_0_3/KBV_PR_FOR_Coverage.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3", "1.0.3"),
            List.of("/fhir/r4/profile/v1_0_3/KBV_PR_FOR_Organization.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3", "1.0" +
                            ".3"),
            List.of("/fhir/r4/profile/v1_0_3/KBV_PR_FOR_Patient.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3", "1.0.3"),
            List.of("/fhir/r4/profile/v1_0_3/KBV_PR_FOR_Practitioner.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3", "1.0" +
                            ".3"),
            List.of("/fhir/r4/profile/v1_0_3/KBV_PR_FOR_PractitionerRole.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_PractitionerRole|1.0.3",
                    "1.0.3"),

            List.of("/fhir/r4/profile/v1_1_3/KBV_PR_Base_Organization.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_PR_Base_Organization|1.1.3", "1" +
                            ".1.3"),
            List.of("/fhir/r4/profile/v1_1_3/KBV_PR_Base_Practitioner.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_PR_Base_Practitioner|1.1.3", "1" +
                            ".1.3"),
            List.of("/fhir/r4/profile/v1_1_3/KBV_PR_Base_Patient.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_PR_Base_Patient|1.1.3", "1.1.3"),

            List.of("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_Accident.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Accident", "1.0.1"),
            List.of("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_BVG.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG", "1.0.1"),
            List.of("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_DosageFlag.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag", "1.0.1"),
            List.of("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_EmergencyServicesFee.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee",
                    "1.0.1"),
            List.of("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_Medication_Category.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category",
                    "1.0.1"),
            List.of("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_Medication_CompoundingInstruction" +
                    ".xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_CompoundingInstruction",
                    "1.0.1"),
            List.of("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_Medication_Ingredient_Amount.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Ingredient_Amount",
                    "1.0.1"),
            List.of("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_Medication_Ingredient_Form.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Ingredient_Form",
                    "1.0.1"),
            List.of("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_Medication_Packaging.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Packaging",
                    "1.0.1"),
            List.of("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_Medication_Vaccine.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine",
                    "1.0.1"),
            List.of("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_Multiple_Prescription.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription",
                    "1.0.1"),
            List.of("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_PracticeSupply_Payor.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_PracticeSupply_Payor",
                    "1.0.1"),
            List.of("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_StatusCoPayment.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment", "1.0.1"),

            List.of("/fhir/r4/extension/v1_0_3/KBV_EX_FOR_Alternative_IK.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Alternative_IK", "1.0.3"),
            List.of("/fhir/r4/extension/v1_0_3/KBV_EX_FOR_Legal_basis.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis", "1.0.3"),
            List.of("/fhir/r4/extension/v1_0_3/KBV_EX_FOR_PKV_Tariff.xml",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_PKV_Tariff", "1.0.3")
    );

    protected Map<List<String>, List<List<String>>> structureDefinitionExtensionsMap = Map.of(
            List.of("KBV_PR_ERP_Medication_Compounding", "1.0.1"),
            List.of(List.of("KBV_EX_ERP_Medication_Category", "1.0.1",
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category"))
    );


    protected List<List<String>> valueSets = Arrays.asList(
            List.of("/fhir/r4/valueset/v1_0_1/KBV_VS_ERP_Accident_Type.xml",
                    "KBV_VS_ERP_Accident_Type", "1.0.1"),
            List.of("/fhir/r4/valueset/v1_0_1/KBV_VS_ERP_Medication_Category.xml",
                    "KBV_VS_ERP_Medication_Category", "1.0.1"),
            List.of("/fhir/r4/valueset/v1_0_1/KBV_VS_ERP_StatusCoPayment.xml",
                    "KBV_VS_ERP_StatusCoPayment", "1.0.1"),

            List.of("/fhir/r4/valueset/v1_0_3/KBV_VS_FOR_Payor_type.xml",
                    "KBV_VS_FOR_Payor_type", "1.0.3"),
            List.of("/fhir/r4/valueset/v1_0_3/KBV_VS_FOR_Qualification_Type.xml",
                    "KBV_VS_FOR_Qualification_Type", "1.0.3")
    );

    protected List<List<String>> namingSystems = Arrays.asList(
            List.of("/fhir/r4/namingsystems/Pruefnummer.xml", "KBV_NS_FOR_Pruefnummer"));

    protected List<List<String>> codeSystems = Arrays.asList(
            List.of("/fhir/r4/codesystem/v1_0_1/KBV_CS_ERP_Medication_Category.xml",
                    "KBV_CS_ERP_Medication_Category", "1.0.1"),
            List.of("/fhir/r4/codesystem/v1_0_1/KBV_CS_ERP_Medication_Type.xml",
                    "KBV_CS_ERP_Medication_Type", "1.0.1"),
            List.of("/fhir/r4/codesystem/v1_0_1/KBV_CS_ERP_Section_Type.xml",
                    "KBV_CS_ERP_Section_Type", "1.0.1"),
            List.of("/fhir/r4/codesystem/v1_0_1/KBV_CS_ERP_StatusCoPayment.xml",
                    "KBV_CS_ERP_StatusCoPayment", "1.0.1"),

            List.of("/fhir/r4/codesystem/v1_0_3/KBV_CS_FOR_Payor_Type_KBV.xml",
                    "KBV_CS_ERP_StatusCoPayment", "1.0.3"),
            List.of("/fhir/r4/codesystem/v1_0_3/KBV_CS_FOR_Qualification_Type.xml",
                    "KBV_CS_FOR_Qualification_Type", "1.0.3"),
            List.of("/fhir/r4/codesystem/v1_0_3/KBV_CS_FOR_Ursache_Type.xml",
                    "KBV_CS_FOR_Ursache_Type", "1.0.3")
    );

    public ErePrePopulatedValidationSupport(FhirContext theContext) {
        super(theContext);

        initValidationConfigs();
    }

    protected void addStructureDefinition(String configUrl,
                                          String configVersion,
                                          InputStream configDefinitionInputStream) {
        FhirContext ctx = FhirContext.forR4();
        IParser xmlParser = ctx.newXmlParser();

        StructureDefinition structureDefinition;

        try(configDefinitionInputStream) {
            structureDefinition = xmlParser.parseResource(StructureDefinition.class,
                    configDefinitionInputStream);
            structureDefinition.setUrl(configUrl);
            structureDefinition.setVersion(configVersion);

            if(!structureDefinition.getType().equals("Extension")) {
                List<List<String>> extensions = structureDefinitionExtensionsMap.get(Arrays.asList(
                        structureDefinition.getName(), structureDefinition.getVersion()));

                if (CollectionUtils.isNotEmpty(extensions)) {
                    extensions.stream().forEach(extList -> {
                        logger.infof("Adding extension %s to StructureDefinition %s",
                                extList.get(2), structureDefinition.getUrl());
                        structureDefinition.addExtension(new Extension(extList.get(2)));
                    });
                }
            }

            addStructureDefinition(structureDefinition);
        } catch (IOException e) {
            logger.errorf(e,"Error loading StructureDefinition profile %s", configUrl);
        }
    }

    protected void addValueSet(String configUrl, String configVersion,
                                          InputStream configDefinitionInputStream) {
        FhirContext ctx = FhirContext.forR4();
        IParser xmlParser = ctx.newXmlParser();

        ValueSet valueSet;

        try(configDefinitionInputStream) {
            valueSet = xmlParser.parseResource(ValueSet.class,
                    configDefinitionInputStream);
            valueSet.setUrl(configUrl);
            valueSet.setVersion(configVersion);
            addValueSet(valueSet);
        } catch (IOException e) {
            logger.errorf(e,"Error loading ValueSet profile %s", configUrl);
        }
    }

    protected void addCodeSystem(String configUrl, String configVersion,
                               InputStream configDefinitionInputStream) {
        FhirContext ctx = FhirContext.forR4();
        IParser xmlParser = ctx.newXmlParser();

        CodeSystem codeSystem;

        try(configDefinitionInputStream) {
            codeSystem = xmlParser.parseResource(CodeSystem.class,
                    configDefinitionInputStream);
            codeSystem.setUrl(configUrl);
            codeSystem.setVersion(configVersion);
            addCodeSystem(codeSystem);
        } catch (IOException e) {
            logger.errorf(e,"Error loading CodeSystem profile %s", configUrl);
        }
    }

    protected void initValidationConfigs() {
        // Init Structure Definitions.
        structureDefinitionFilePaths.stream().forEach(configList -> {
            String url = configList.get(1).contains("/")? configList.get(1) :
                    URL_PREFIX_STRUCTURE_DEFINITION + configList.get(1) +
                            "|" + configList.get(2);

            addStructureDefinition(url, configList.get(2),
                    ErePrePopulatedValidationSupport.class.getResourceAsStream(configList.get(0)));
        });

        // Init value sets.
        valueSets.stream().forEach(configList -> {
            addValueSet(URL_PREFIX_VALUE_SET_DEFINITION + configList.get(1),
                    configList.get(2),
                    ErePrePopulatedValidationSupport.class.getResourceAsStream(configList.get(0)));
        });

        // Init code systems
        codeSystems.stream().forEach(configList -> {
            logger.infof("Loading CodeSystem %s", configList.get(0));
            addCodeSystem(URL_PREFIX_CODE_SYSTEM_DEFINITION + configList.get(1),
                    configList.get(2),
                    ErePrePopulatedValidationSupport.class.getResourceAsStream(configList.get(0)));
            logger.infof("Loaded CodeSystem %s", configList.get(0));
        });
    }
}
