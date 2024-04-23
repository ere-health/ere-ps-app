package health.ere.ps.validation.fhir.bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import health.ere.ps.service.fhir.FHIRService;
import health.ere.ps.validation.fhir.context.support.ErePrePopulatedValidationSupport;
import io.quarkus.runtime.Startup;


@Startup
@ApplicationScoped
public class PrescriptionBundleValidator {

    private static final Logger log =
            Logger.getLogger(PrescriptionBundleValidator.class.getName());
    private FhirValidator validator;

    @PostConstruct
    void init() {
        try {
            log.info("Starting validator");
            FhirContext fhirContext = FHIRService.getFhirContext();

            // Create a chain that will hold our modules
            ValidationSupportChain validationSupportChain = new ValidationSupportChain();

            // DefaultProfileValidationSupport supplies base FHIR definitions. This is generally required
            // even if you are using custom profiles, since those profiles will derive from the base
            // definitions.
            validationSupportChain.addValidationSupport(new DefaultProfileValidationSupport(fhirContext));
            validationSupportChain.addValidationSupport(new ErePrePopulatedValidationSupport(fhirContext));
            validationSupportChain.addValidationSupport(new CommonCodeSystemsTerminologyService(fhirContext));
            validationSupportChain.addValidationSupport(new InMemoryTerminologyServerValidationSupport(fhirContext));
            validationSupportChain.addValidationSupport(new SnapshotGeneratingValidationSupport(fhirContext));

            CachingValidationSupport cache = new CachingValidationSupport(validationSupportChain); // todo: 10 min cache timeout...can't we just keep it in memory for the lifetime of the app?

            FhirInstanceValidator validatorModule = new FhirInstanceValidator(cache);

            validatorModule.setAnyExtensionsAllowed(true);
            validatorModule.setErrorForUnknownProfiles(false);
            validatorModule.setNoTerminologyChecks(true); // TODO: Fix issues when set to false.
            validatorModule.setCustomExtensionDomains("http://fhir.de", "https://fhir.kbv.de");

            validator = fhirContext.newValidator().registerValidatorModule(validatorModule);

            // needed for initializing
            validateResource("{\"resourceType\":\"Bundle\",\"id\":\"2e38f9d3-6de0-4272-b343-7b6975e8fe9e\",\"meta\":{\"lastUpdated\":\"2021-04-06T08:30:00Z\",\"profile\":"+
            "[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.1.0\"]},\"identifier\":{\"system\":\"https://gematik.de/fhir/NamingSystem/PrescriptionID\",\"value\":\"160.10"+
            "0.000.000.004.30\"},\"type\":\"document\",\"timestamp\":\"2021-04-06T08:30:00Z\",\"entry\":[{\"fullUrl\":\"http://pvs.praxis.local/fhir/Composition/70e4e747-a1e6-44cd-b91d-"+
            "7cc2eef89c0c\",\"resource\":{\"resourceType\":\"Composition\",\"id\":\"70e4e747-a1e6-44cd-b91d-7cc2eef89c0c\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/"+
            "KBV_PR_ERP_Composition|1.1.0\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/Co"+
            "deSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN\",\"code\":\"00\"}}],\"status\":\"final\",\"type\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFH"+
            "IR_KBV_FORMULAR_ART\",\"code\":\"e16A\"}]},\"subject\":{\"reference\":\"Patient/93866fdc-3e50-4902-a7e9-891b54737b5e\"},\"date\":\"2021-04-05T08:00:00Z\",\"author\""+
            ":[{\"reference\":\"Practitioner/cb7558e2-0fdf-4107-93f6-07f13f39e067\",\"type\":\"Practitioner\"},{\"type\":\"Device\",\"identifier\":{\"system\":\"https://fhir.kbv.de/N"+
            "amingSystem/KBV_NS_FOR_Pruefnummer\",\"value\":\"Y/400/2107/36/999\"}}],\"title\":\"elektronische Arzneimittelverordnung\",\"attester\":[{\"mode\":\"legal\",\"p"+
            "arty\":{\"reference\":\"Practitioner/667ffd79-42a3-4002-b7ca-6b9098f20ccb\"}}],\"custodian\":{\"reference\":\"Organization/5d3f4ac0-2b44-4d48-b363-e63efa72973b\"},\"se"+
            "ction\":[{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\",\"code\":\"Prescription\"}]},\"entry\":[{\"reference\":\"Medicatio"+
            "nRequest/877e9689-523e-46ca-aa78-8de34a023583\"}]},{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\",\"code\":\"Coverage\""+
            "}]},\"entry\":[{\"reference\":\"Coverage/1b89236c-ab14-4e92-937e-5af0b59d0cd4\"}]}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/MedicationRequest/877e9689-523e-46ca-aa"+
            "78-8de34a023583\",\"resource\":{\"resourceType\":\"MedicationRequest\",\"id\":\"877e9689-523e-46ca-aa78-8de34a023583\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/Stru"+
            "ctureDefinition/KBV_PR_ERP_Prescription|1.1.0\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment\",\"valueCoding\":{\"syst"+
            "em\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_StatusCoPayment\",\"code\":\"0\"}},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee\""+
            ",\"valueBoolean\":false},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG\",\"valueBoolean\":false},{\"url\":\"https://fhir.kbv.de/StructureDefinition/"+
            "KBV_EX_ERP_Multiple_Prescription\",\"extension\":[{\"url\":\"Kennzeichen\",\"valueBoolean\":false}]}],\"status\":\"active\",\"intent\":\"order\",\"medicationReference\":"+
            "{\"reference\":\"Medication/b7dd5ddb-b5ad-4b04-af11-6d2a354bce0c\"},\"subject\":{\"reference\":\"Patient/93866fdc-3e50-4902-a7e9-891b54737b5e\"},\"authoredOn\":\"2021-0"+
            "4-06\",\"requester\":{\"reference\":\"Practitioner/cb7558e2-0fdf-4107-93f6-07f13f39e067\"},\"insurance\":[{\"reference\":\"Coverage/1b89236c-ab14-4e92-937e-5af0b59d0cd"+
            "4\"}],\"note\":[{\"text\":\"Bitte auf Anwendung schulen\"}],\"dosageInstruction\":[{\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageF"+
            "lag\",\"valueBoolean\":false}]}],\"dispenseRequest\":{\"quantity\":{\"value\":2,\"system\":\"http://unitsofmeasure.org\",\"code\":\"{Package}\"}},\"substitution\":{\"al"+
            "lowedBoolean\":true}}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Medication/b7dd5ddb-b5ad-4b04-af11-6d2a354bce0c\",\"resource\":{\"resourceType\":\"Medication\",\"id\""+
            ":\"b7dd5ddb-b5ad-4b04-af11-6d2a354bce0c\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.1.0\"]},\"extension\":[{\"url\":\""+
            "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category\",\""+
            "code\":\"00\"}},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine\",\"valueBoolean\":false},{\"url\":\"http://fhir.de/StructureDefinitio"+
            "n/normgroesse\",\"valueCode\":\"N1\"}],\"code\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/ifa/pzn\",\"code\":\"00427833\"}],\"text\":\"Viani 50\u00B5g/250\u00B5"+
            "g 1 Diskus 60 ED N1\"},\"form\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM\",\"code\":\"IHP\"}]},\"amount\":{\"numerato"+
            "r\":{\"value\":1,\"unit\":\"Diskus\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"{tbl}\"},\"denominator\":{\"value\":1}}}},{\"fullUrl\":\"http://pvs.praxis.local/"+
            "fhir/Patient/93866fdc-3e50-4902-a7e9-891b54737b5e\",\"resource\":{\"resourceType\":\"Patient\",\"id\":\"93866fdc-3e50-4902-a7e9-891b54737b5e\",\"meta\":{\"profile\":[\"htt"+
            "ps://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/identifier-type-de-basis\""+
            ",\"code\":\"GKV\"}]},\"system\":\"http://fhir.de/NamingSystem/gkv/kvid-10\",\"value\":\"K220635158\"}],\"name\":[{\"use\":\"official\",\"family\":\"K\u00F6nigsstein\",\"_f"+
            "amily\":{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/humanname-own-name\",\"valueString\":\"K\u00F6nigsstein\"}]},\"given\":[\"Ludger\"]}],\"birthDat"+
            "e\":\"1935-06-22\",\"address\":[{\"type\":\"both\",\"line\":[\"Blumenweg\"],\"_line\":[{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-stre"+
            "etName\",\"valueString\":\"Blumenweg\"}]}],\"city\":\"Esens\",\"postalCode\":\"26427\",\"country\":\"D\"}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Practitioner/cb7558e"+
            "2-0fdf-4107-93f6-07f13f39e067\",\"resource\":{\"resourceType\":\"Practitioner\",\"id\":\"cb7558e2-0fdf-4107-93f6-07f13f39e067\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/S"+
            "tructureDefinition/KBV_PR_FOR_Practitioner|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0203\",\"code\":\"LANR\"}]}"+
            ",\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR\",\"value\":\"895268385\"}],\"name\":[{\"use\":\"official\",\"family\":\"Fischer\",\"_family\":{\"extension\":[{"+
            "\"url\":\"http://hl7.org/fhir/StructureDefinition/humanname-own-name\",\"valueString\":\"Fischer\"}]},\"given\":[\"Alexander\"]}],\"qualification\":[{\"code\":{\"coding\":[{\""+
            "system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Qualification_Type\",\"code\":\"03\"}]}},{\"code\":{\"text\":\"Weiterbildungsassistent\"}}]}},{\"fullUrl\":\"http://pvs.p"+
            "raxis.local/fhir/Practitioner/667ffd79-42a3-4002-b7ca-6b9098f20ccb\",\"resource\":{\"resourceType\":\"Practitioner\",\"id\":\"667ffd79-42a3-4002-b7ca-6b9098f20ccb\",\"meta\":{\""+
            "profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSy"+
            "stem/v2-0203\",\"code\":\"LANR\"}]},\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR\",\"value\":\"987654423\"}],\"name\":[{\"use\":\"official\",\"family\":\"Sch"+
            "neider\",\"_family\":{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/humanname-own-name\",\"valueString\":\"Schneider\"}]},\"given\":[\"Emma\"],\"prefix\":["+
            "\"Dr. med.\"],\"_prefix\":[{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier\",\"valueCode\":\"AC\"}]}]}],\"qualification\":[{\"code\":{"+
            "\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Qualification_Type\",\"code\":\"00\"}]}},{\"code\":{\"text\":\"Fach\u00E4rztin f\u00FCr Innere Medizin\"}}]"+
            "}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Organization/5d3f4ac0-2b44-4d48-b363-e63efa72973b\",\"resource\":{\"resourceType\":\"Organization\",\"id\":\"5d3f4ac0-2b44-4d48-"+
            "b363-e63efa72973b\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http:"+
            "//terminology.hl7.org/CodeSystem/v2-0203\",\"code\":\"BSNR\"}]},\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR\",\"value\":\"721111100\"}],\"name\":\"MVZ\",\"te"+
            "lecom\":[{\"system\":\"phone\",\"value\":\"0301234567\"},{\"system\":\"fax\",\"value\":\"030123456789\"},{\"system\":\"email\",\"value\":\"mvz@e-mail.de\"}],\"address\":[{\"typ"+
            "e\":\"both\",\"line\":[\"Herbert-Lewin-Platz 2\"],\"_line\":[{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber\",\"valueString\":\"2\""+
            "},{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName\",\"valueString\":\"Herbert-Lewin-Platz\"}]}],\"city\":\"Berlin\",\"postalCode\":\"10623\",\"countr"+
            "y\":\"D\"}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Coverage/1b89236c-ab14-4e92-937e-5af0b59d0cd4\",\"resource\":{\"resourceType\":\"Coverage\",\"id\":\"1b89236c-ab14-4e92-"+
            "937e-5af0b59d0cd4\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3\"]},\"extension\":[{\"url\":\"http://fhir.de/StructureDefinition/gk"+
            "v/besondere-personengruppe\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE\",\"code\":\"00\"}},{\"url\":\"http://fhir.de/StructureDe"+
            "finition/gkv/dmp-kennzeichen\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP\",\"code\":\"05\"}},{\"url\":\"http://fhir.de/StructureDefinition"+
            "/gkv/wop\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP\",\"code\":\"17\"}},{\"url\":\"http://fhir.de/StructureDefinition/gkv/versichertenart"+
            "\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS\",\"code\":\"5\"}}],\"status\":\"active\",\"type\":{\"coding\":[{\"system\":\"h"+
            "ttp://fhir.de/CodeSystem/versicherungsart-de-basis\",\"code\":\"GKV\"}]},\"beneficiary\":{\"reference\":\"Patient/93866fdc-3e50-4902-a7e9-891b54737b5e\"},\"payor\":[{\"identifier\""+
            ":{\"system\":\"http://fhir.de/NamingSystem/arge-ik/iknr\",\"value\":\"109719018\"},\"display\":\"AOK Nordost\"}]}}]}", false);
        } catch(Exception ex) {
            log.log(Level.SEVERE, "Could not start validator", ex);
        }
    }

    public ValidationResult validateResource(IBaseResource resource, boolean showIssues) {
        ValidationResult validationResult = validator.validateWithResult(resource);

        if(showIssues) {
            showIssues(validationResult);
        }

        return validationResult;
    }

    public ValidationResult validateResource(String resourceText, boolean showIssues) {
        return validateResource(resourceText, showIssues, null);
    }

    public ValidationResult validateResource(String resourceText,
                                             List<String> validationErrorsCollectorList) {
        return validateResource(resourceText, false, validationErrorsCollectorList);
    }

    public ValidationResult validateResource(String resourceText, boolean showIssues,
                                             List<String> validationErrorsCollectorList) {
        ValidationResult validationResult = validator.validateWithResult(resourceText);

        if(showIssues || validationErrorsCollectorList != null) {
            showIssues(validationResult, validationErrorsCollectorList);
        }

        return validationResult;
    }

    protected void showIssues(ValidationResult validationResult) {
        showIssues(validationResult, null);
    }

    protected void showIssues(ValidationResult validationResult,
                              List<String> validationErrorsCollectorList) {
        if(!validationResult.isSuccessful()) {
            String errorReport = "";

            for (SingleValidationMessage next : validationResult.getMessages()) {
                errorReport = " Next issue " + next.getSeverity() + " - " +
                        next.getLocationString() + " - " + next.getMessage();
                if(validationErrorsCollectorList != null) {
                    validationErrorsCollectorList.add(errorReport);
                }

                log.info(errorReport);
            }
        }
    }

    public JsonObject bundlesValidationResult(JsonObject bundlePayload) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("type", "BundlesValidationResult");
        JsonArrayBuilder payload = Json.createArrayBuilder();
        for (JsonValue jsonValue : bundlePayload.getJsonArray("payload")) {
            if (jsonValue instanceof JsonArray) {
                for (JsonValue singleBundle : (JsonArray) jsonValue) {
                    JsonObjectBuilder singleBundleResults = validateBundle(singleBundle);
                    payload.add(singleBundleResults);
                }
            }
        }
        builder.add("payload", payload);
        if(bundlePayload.containsKey("id")) {
            builder.add("replyToMessageId", bundlePayload.getString("id", null));
        } else {
            builder.add("replyToMessageId", "");
        }
        return builder.build();
    }

    public JsonObjectBuilder validateBundle(JsonValue singleBundle) {
        log.info("Now validating incoming sign and upload bundle:\n" +
                    singleBundle.toString());
        JsonObjectBuilder singleBundleResults = Json.createObjectBuilder();
        String bundleJson = singleBundle.toString();
        List<String> errorsList = new ArrayList<>(1);

        if (!validateResource(bundleJson,
        true, errorsList).isSuccessful()) {
            JsonArrayBuilder errorsJson = Json.createArrayBuilder();
            errorsList.stream().forEach(s -> errorsJson.add(s));
            singleBundleResults.add("errors", errorsJson);
            singleBundleResults.add("valid", false);
        } else {
            singleBundleResults.add("valid", true);
            log.info("Validation for the following incoming sign and " +
            "upload bundle passed:\n" +
            singleBundle.toString());
        }
        return singleBundleResults;
    }
}
