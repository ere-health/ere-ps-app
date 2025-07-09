package health.ere.ps.validation.fhir.bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.validation.SingleValidationMessage;
import de.gematik.refv.SupportedValidationModule;
import de.gematik.refv.ValidationModuleFactory;
import de.gematik.refv.commons.exceptions.ValidationModuleInitializationException;
import de.gematik.refv.commons.validation.ValidationModule;
import de.gematik.refv.commons.validation.ValidationResult;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;


@Startup
@ApplicationScoped
public class PrescriptionBundleValidator {

    private static final Logger log =
            Logger.getLogger(PrescriptionBundleValidator.class.getName());

    ValidationModule erpModule;

    @PostConstruct
    public void init() {
        try {
            erpModule = new ValidationModuleFactory().createValidationModule(SupportedValidationModule.ERP);
            erpModule.getConfiguration().setAcceptedEncodings(Arrays.asList(Constants.FORMAT_XML, Constants.FORMAT_JSON));
        } catch (IllegalArgumentException | ValidationModuleInitializationException e) {
            log.log(Level.SEVERE, "Could not init validator", e);
        }
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
        ValidationResult validationResult = erpModule.validateString(resourceText);

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
        if(!validationResult.isValid()) {
            String errorReport = "";

            for (SingleValidationMessage next : validationResult.getValidationMessages()) {
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
        log.info("Now validating incoming sign and upload bundle.");
        log.fine("Bundle for Validation:\n" +
                    singleBundle.toString());
        JsonObjectBuilder singleBundleResults = Json.createObjectBuilder();
        String bundleJson = singleBundle.toString();
        // JsonString.toString adds prepending and appending quotation marks.
        // We have to call JsonString.getString
        if (singleBundle instanceof JsonString)
            bundleJson = ((JsonString) singleBundle).getString();
        List<String> errorsList = new ArrayList<>(1);

        if (!validateResource(bundleJson,
        true, errorsList).isValid()) {
            JsonArrayBuilder errorsJson = Json.createArrayBuilder();
            errorsList.stream().forEach(s -> errorsJson.add(s));
            singleBundleResults.add("errors", errorsJson);
            singleBundleResults.add("valid", false);
        } else {
            singleBundleResults.add("valid", true);
            log.info("Validation passed.");
            log.fine("Valid incoming sign and upload bundle:\n" +
            singleBundle.toString());
        }
        return singleBundleResults;
    }
}
