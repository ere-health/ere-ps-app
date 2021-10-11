package health.ere.ps.validation.fhir.bundle;

import java.util.ArrayList;
import java.util.List;

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
import org.jboss.logging.Logger;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import health.ere.ps.validation.fhir.context.support.ErePrePopulatedValidationSupport;

@ApplicationScoped
public class PrescriptionBundleValidator {

    private static final Logger log =
            Logger.getLogger(PrescriptionBundleValidator.class.getName());
    private FhirValidator validator;

    @PostConstruct
    void init() {
        FhirContext ctx = FhirContext.forR4();

        // Create a chain that will hold our modules
        ValidationSupportChain validationSupportChain = new ValidationSupportChain();

        // DefaultProfileValidationSupport supplies base FHIR definitions. This is generally required
        // even if you are using custom profiles, since those profiles will derive from the base
        // definitions.
        validationSupportChain.addValidationSupport(new DefaultProfileValidationSupport(ctx));
        validationSupportChain.addValidationSupport(new ErePrePopulatedValidationSupport(ctx));
        validationSupportChain.addValidationSupport(new CommonCodeSystemsTerminologyService(ctx));
        validationSupportChain.addValidationSupport(new InMemoryTerminologyServerValidationSupport(ctx));
        validationSupportChain.addValidationSupport(new SnapshotGeneratingValidationSupport(ctx));

        CachingValidationSupport cache = new CachingValidationSupport(validationSupportChain);

        FhirInstanceValidator validatorModule = new FhirInstanceValidator(cache);

        validatorModule.setAnyExtensionsAllowed(true);
        validatorModule.setErrorForUnknownProfiles(false);
        validatorModule.setNoTerminologyChecks(true); // TODO: Fix issues when set to false.
        validatorModule.setCustomExtensionDomains("http://fhir.de", "https://fhir.kbv.de");

        validator = ctx.newValidator().registerValidatorModule(validatorModule);
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
        builder.add("replyToMessageId", bundlePayload.getString("id", null));
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
