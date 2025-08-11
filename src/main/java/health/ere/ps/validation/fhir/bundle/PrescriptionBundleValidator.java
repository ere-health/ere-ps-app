package health.ere.ps.validation.fhir.bundle;

import ca.uhn.fhir.validation.SingleValidationMessage;
import de.gematik.refv.ValidationModuleFactory;
import de.gematik.refv.commons.configuration.ValidationModuleConfiguration;
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
import jakarta.json.JsonValue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ca.uhn.fhir.rest.api.Constants.FORMAT_JSON;
import static de.gematik.refv.SupportedValidationModule.ERP;

@Startup
@ApplicationScoped
public class PrescriptionBundleValidator {

    private static final Logger log = Logger.getLogger(PrescriptionBundleValidator.class.getName());

    ValidationModule erpModule;

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        try {
            erpModule = new ValidationModuleFactory().createValidationModule(ERP);
            ValidationModuleConfiguration configuration = erpModule.getConfiguration();
            Field acceptedEncodings = configuration.getClass().getDeclaredField("acceptedEncodings");
            acceptedEncodings.setAccessible(true);
            List<String> list = (ArrayList<String>) acceptedEncodings.get(configuration);
            if (!list.contains(FORMAT_JSON)) {
                list.add(FORMAT_JSON);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not init validator", e);
        }
    }

    public ValidationResult validateResource(String resourceText, boolean showIssues, List<String> errors) {
        ValidationResult validationResult = erpModule.validateString(resourceText);

        if (showIssues || errors != null) {
            showIssues(validationResult, errors);
        }

        return validationResult;
    }

    protected void showIssues(ValidationResult validationResult, List<String> errors) {
        if (!validationResult.isValid()) {
            String errorReport = "";

            for (SingleValidationMessage next : validationResult.getValidationMessages()) {
                errorReport = " Next issue " + next.getSeverity() + " - " +
                    next.getLocationString() + " - " + next.getMessage();
                if (errors != null) {
                    errors.add(errorReport);
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
        if (bundlePayload.containsKey("id")) {
            builder.add("replyToMessageId", bundlePayload.getString("id", null));
        } else {
            builder.add("replyToMessageId", "");
        }
        return builder.build();
    }

    public JsonObjectBuilder validateBundle(JsonValue singleBundle) {
        log.fine("Now validating incoming sign and upload bundle:\n" + singleBundle.toString());
        JsonObjectBuilder singleBundleResults = Json.createObjectBuilder();
        String bundleJson = singleBundle.toString();
        List<String> errorsList = new ArrayList<>(1);

        if (!validateResource(bundleJson, true, errorsList).isValid()) {
            JsonArrayBuilder errorsJson = Json.createArrayBuilder();
            errorsList.forEach(errorsJson::add);
            singleBundleResults.add("errors", errorsJson);
            singleBundleResults.add("valid", false);
        } else {
            singleBundleResults.add("valid", true);
            log.info("Validation for the following incoming sign and upload bundle passed:\n" + singleBundle);
        }
        return singleBundleResults;
    }
}