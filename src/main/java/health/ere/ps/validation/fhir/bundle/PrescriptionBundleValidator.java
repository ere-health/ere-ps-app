package health.ere.ps.validation.fhir.bundle;

import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jboss.logging.Logger;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.interceptor.executor.InterceptorService;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import health.ere.ps.validation.fhir.context.support.ErePrePopulatedValidationSupport;
import health.ere.ps.validation.fhir.hook.EreValidationHook;

@ApplicationScoped
public class PrescriptionBundleValidator {
    private static final Logger logger = Logger.getLogger(PrescriptionBundleValidator.class);
    private final FhirValidator validator;

    public PrescriptionBundleValidator() throws IOException {
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
        validatorModule.setCustomExtensionDomains(
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis",
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_PKV_Tariff",
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment",
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee",
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG",
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription",
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag",
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category",
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine",
                "http://fhir.de",
                "http://fhir.de/StructureDefinition/normgroesse",
                "http://fhir.de/StructureDefinition/gkv/besondere-personengruppe",
                "http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen",
                "http://fhir.de/StructureDefinition/gkv/wop",
                "http://fhir.de/StructureDefinition/gkv/versichertenart"
        );

        validator = ctx.newValidator().registerValidatorModule(validatorModule);

        InterceptorService interceptorService = new InterceptorService();

        interceptorService.registerInterceptor(new EreValidationHook());

        validator.setInterceptorBroadcaster(interceptorService);
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
