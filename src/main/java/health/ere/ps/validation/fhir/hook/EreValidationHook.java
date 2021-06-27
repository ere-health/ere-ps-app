package health.ere.ps.validation.fhir.hook;

import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;

/**
 * This class is a validation hook interceptor used to intercept and alter the results of the
 * validator it is registered with.
 *
 */
@Interceptor
public class EreValidationHook {

    private final static Logger logger = Logger.getLogger(EreValidationHook.class);

    /**
     * This method intercepts the validation result object from the validator and overrides its
     * list of validation messages in order to fix what appears to be a bug that is associated with
     * a specific validation scenario which causes both error and success validation results to
     * be returned for the same validation item.
     *
     * @param validationResult the validation result object returned by the validator.
     * @return
     */
    @Hook(Pointcut.VALIDATION_COMPLETED)
    public ValidationResult validationResultOverrideHook(ValidationResult validationResult) {
        List<SingleValidationMessage> singleValidationMessages = validationResult.getMessages();

        ValidationResult updatedValidationResult = new ValidationResult(
                validationResult.getContext(),
                filterOutFailedAmbiguousValidationResults(singleValidationMessages));

        return updatedValidationResult;
    }

    /**
     * This method filters out what appears to be a specific set of conflicting validation results
     * which are returned as both error and success for the same validated item.
     *
     * The error results that have matching success results are filtered out.
     *
     * @param singleValidationMessages the original list of validation results intercepted from the
     *                                 validator.
     * @return
     */
    protected List<SingleValidationMessage> filterOutFailedAmbiguousValidationResults(
            List<SingleValidationMessage> singleValidationMessages) {
        final List<SingleValidationMessage> possibleAmbiguousValidationMessages =
                new ArrayList<>(4);

        if (CollectionUtils.isNotEmpty(singleValidationMessages)) {
            possibleAmbiguousValidationMessages.addAll(
                    singleValidationMessages.stream().filter(singleValMsg ->
                singleValMsg.getMessage().matches("Unable to find a match for profile " +
                        "MedicationRequest/.+ among choices: https://fhir\\.kbv" +
                        "\\.de/StructureDefinition/KBV_PR_ERP_Prescription\\|1\\.0\\.1") ||
                singleValMsg.getMessage().matches("Details for MedicationRequest/.+ " +
                        "matching against Profilehttps://fhir\\.kbv\\.de/StructureDefinition/" +
                        "KBV_PR_ERP_Prescription\\|1\\.0\\.1") ||
                singleValMsg.getMessage().matches("Unable to find a match for profile " +
                        "Coverage/.+ " +
                        "among choices: https://fhir\\.kbv" +
                        "\\.de/StructureDefinition/KBV_PR_FOR_Coverage\\|1\\.0\\.1") ||
                singleValMsg.getMessage().matches("Details for Coverage/.+ " +
                        "matching against " +
                        "Profilehttps://fhir\\.kbv\\.de/StructureDefinition/KBV_PR_FOR_Coverage" +
                        "\\|1\\.0\\.1") &&
                        (singleValMsg.getSeverity() == ResultSeverityEnum.ERROR ||
                                singleValMsg.getSeverity() == ResultSeverityEnum.INFORMATION)
            ).collect(Collectors.toList()));
        }

        singleValidationMessages.stream().forEach(valMsg -> logger.infof("Unfiltered " +
                "results message = " +
                "%s", valMsg.getMessage()));

        possibleAmbiguousValidationMessages.stream().forEach(valMsg -> logger.infof("Filtered " +
                "results message = " +
                "%s", valMsg.getMessage()));

        if(CollectionUtils.isNotEmpty(possibleAmbiguousValidationMessages) &&
                possibleAmbiguousValidationMessages.size() % 2 == 0) {
            return singleValidationMessages.stream().filter(singleValMsg ->
                    singleValMsg.getSeverity() == ResultSeverityEnum.ERROR &&
                            !possibleAmbiguousValidationMessages.contains(singleValMsg))
                    .collect(Collectors.toList());
        }

        return singleValidationMessages;
    }
}
