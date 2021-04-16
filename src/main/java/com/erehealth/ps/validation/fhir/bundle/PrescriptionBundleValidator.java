package com.erehealth.ps.validation.fhir.bundle;

import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;

public class PrescriptionBundleValidator {
    private FhirValidator validator;

    public PrescriptionBundleValidator() {
        FhirContext ctx = FhirContext.forR4();

        // Create a chain that will hold our modules
        ValidationSupportChain supportChain = new ValidationSupportChain();

        // DefaultProfileValidationSupport supplies base FHIR definitions. This is generally required
        // even if you are using custom profiles, since those profiles will derive from the base
        // definitions.
        DefaultProfileValidationSupport defaultSupport = new DefaultProfileValidationSupport(ctx);
        supportChain.addValidationSupport(defaultSupport);

        // Create a PrePopulatedValidationSupport which can be used to load custom definitions.
        // In this example we're loading two things, but in a real scenario we might
        // load many StructureDefinitions, ValueSets, CodeSystems, etc.
//        PrePopulatedValidationSupport prePopulatedSupport = new PrePopulatedValidationSupport(ctx);
//        prePopulatedSupport.addStructureDefinition(someStructureDefnition);
//        prePopulatedSupport.addValueSet(someValueSet);

        // Add the custom definitions to the chain
//        supportChain.addValidationSupport(prePopulatedSupport);

        // Wrap the chain in a cache to improve performance
        CachingValidationSupport cache = new CachingValidationSupport(supportChain);

        // Create a validator using the FhirInstanceValidator module. We can use this
        // validator to perform validation
        FhirInstanceValidator validatorModule = new FhirInstanceValidator(cache);
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
                System.out.println(" Next issue " + next.getSeverity() + " - " +
                        next.getLocationString() + " - " + next.getMessage());
            }
        }
    }
}
