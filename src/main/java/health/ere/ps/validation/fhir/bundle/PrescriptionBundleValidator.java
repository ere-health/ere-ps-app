package health.ere.ps.validation.fhir.bundle;

import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import health.ere.ps.validation.fhir.context.support.ErePrePopulatedValidationSupport;

@ApplicationScoped
public class PrescriptionBundleValidator {

    private static final Logger logger =
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
        ValidationResult validationResult = validator.validateWithResult(resourceText);

        if(showIssues) {
            showIssues(validationResult);
        }

        return validationResult;
    }

    protected void showIssues(ValidationResult validationResult) {
        if(!validationResult.isSuccessful()) {
            for (SingleValidationMessage next : validationResult.getMessages()) {
                logger.info(" Next issue " + next.getSeverity() + " - " +
                        next.getLocationString() + " - " + next.getMessage());
            }
        }
    }
}
