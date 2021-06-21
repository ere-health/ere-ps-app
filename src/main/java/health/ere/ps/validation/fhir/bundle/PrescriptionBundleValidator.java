package health.ere.ps.validation.fhir.bundle;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.BaseExtension;
import org.hl7.fhir.r4.model.Element;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.StructureDefinition;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;
import health.ere.ps.validation.fhir.context.support.KbvValidationSupport;
import health.ere.ps.validation.fhir.structuredefinition.fhir.kbv.de.v1_0_1.KBV_PR_ERP_Bundle_StructureDefinition;
import health.ere.ps.validation.fhir.structuredefinition.fhir.kbv.de.v1_0_1.KBV_PR_ERP_Medication_PZN;
import health.ere.ps.validation.fhir.structuredefinition.fhir.kbv.de.v1_0_1.extension.KBV_EX_ERP_Medication_Category;

public class PrescriptionBundleValidator {
    private FhirValidator validator;
    private String profile;

    public PrescriptionBundleValidator(String profile) throws IOException {
        this();

        setProfile(profile);
    }

    public PrescriptionBundleValidator() throws IOException {
        FhirContext ctx = FhirContext.forR4();
        IParser xmlParser = ctx.newXmlParser();

        // Create a chain that will hold our modules
        ValidationSupportChain validationSupportChain = new ValidationSupportChain();

        // DefaultProfileValidationSupport supplies base FHIR definitions. This is generally required
        // even if you are using custom profiles, since those profiles will derive from the base
        // definitions.
//        DefaultProfileValidationSupport defaultSupport = new DefaultProfileValidationSupport(ctx);
        validationSupportChain.addValidationSupport(new DefaultProfileValidationSupport(ctx));
        validationSupportChain.addValidationSupport(new KbvValidationSupport(ctx));
        validationSupportChain.addValidationSupport(new SnapshotGeneratingValidationSupport(ctx));
        validationSupportChain.addValidationSupport(new InMemoryTerminologyServerValidationSupport(ctx));



        // This module supplies several code systems that are commonly used in validation
//        validationSupportChain.addValidationSupport(new CommonCodeSystemsTerminologyService(ctx));

        // This module implements terminology services for in-memory code validation


        // Create a PrePopulatedValidationSupport which can be used to load custom definitions.
        // In this example we're loading two things, but in a real scenario we might
        // load many StructureDefinitions, ValueSets, CodeSystems, etc.
        PrePopulatedValidationSupport prePopulatedSupport = new PrePopulatedValidationSupport(ctx);
        StructureDefinition kbvBundleStructureDefinition =
                xmlParser.parseResource(StructureDefinition.class,
                KBV_PR_ERP_Bundle_StructureDefinition.STRUCTURE_DEFINITION_XML);

        kbvBundleStructureDefinition.setUrl("https://fhir.kbv" +
                ".de/StructureDefinition/KBV_PR_ERP_Bundle");
        kbvBundleStructureDefinition.setBaseDefinition("http://hl7.org/fhir/StructureDefinition" +
                "/Bundle");
        kbvBundleStructureDefinition.setVersion("1.0.1");

//        StructureDefinition kbvMedicationPznStructureDefinition =
//                xmlParser.parseResource(StructureDefinition.class,
//                        KBV_PR_ERP_Medication_PZN.STRUCTURE_DEFINITION_XML);
//        StructureDefinition kbvExErpMedicationStructureDefinition =
//                xmlParser.parseResource(StructureDefinition.class,
//                        KBV_EX_ERP_Medication_Category.EXTENSION_STRUCTURE_DEFINITION_XML);

//        StructureDefinition customProfile = loadResource(ctx, StructureDefinition.class, "/r4/profile.json");

        prePopulatedSupport.addStructureDefinition(kbvBundleStructureDefinition);
//        addStructureDefinition(prePopulatedSupport,
//                getClass().getResourceAsStream(
//                        "/fhir/structuredefinition/kbv/de/v1_0_1/KBV_PR_ERP_Composition.xml"),
//                "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition",
//                "1.0.1",
//                "Composition",
//                "http://hl7.org/fhir/StructureDefinition/Composition",
//                true);
//        prePopulatedSupport.addStructureDefinition(kbvMedicationPznStructureDefinition);
//        prePopulatedSupport.addStructureDefinition(kbvExErpMedicationStructureDefinition);

//        prePopulatedSupport.addCodeSystem(new IdentifierTypeDeBasisCodeSystem());
//        prePopulatedSupport.addCodeSystem(new KBV_CS_SFHIR_KBV_FORMULAR_ART_CodeSystem());


        // Add the custom definitions to the chain
        validationSupportChain.addValidationSupport(prePopulatedSupport);

        // Wrap the chain in a cache to improve performance
        CachingValidationSupport cache = new CachingValidationSupport(validationSupportChain);

        // Create a validator using the FhirInstanceValidator module. We can use this
        // validator to perform validation
        FhirInstanceValidator validatorModule = new FhirInstanceValidator(cache);

        validatorModule.setAnyExtensionsAllowed(false);
        validatorModule.setErrorForUnknownProfiles(false);
        validatorModule.setNoTerminologyChecks(false);
        validatorModule.setCustomExtensionDomains("hl7.org/fhir",
                "http://hl7.org/fhir/",
                "https://fhir.kbv.de/StructureDefinition",
                "https://fhir.de/StructureDefinition",
                "http://fhir.de/StructureDefinition", "fhir.kbv.de", "http://hl7.org/fhir",
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition");

        validator = ctx.newValidator().registerValidatorModule(validatorModule);
    }

    public ValidationResult validateResource(IBaseResource resource, boolean showIssues) {
        ValidationResult validationResult = validator.validateWithResult(resource);

        if(StringUtils.isNotBlank(getProfile())) {
            validationResult = validator.validateWithResult(resource,
                    new ValidationOptions().addProfile(getProfile()));
        }

        if(showIssues) {
            showIssues(validationResult);
        }

        return validationResult;
    }

    public ValidationResult validateResource(String resourceText, boolean showIssues) {
        ValidationResult validationResult = validator.validateWithResult(resourceText);

        if(StringUtils.isNotBlank(getProfile())) {
            validationResult = validator.validateWithResult(resourceText,
                    new ValidationOptions().addProfile(getProfile()));
        }

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

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    protected void addStructureDefinition(
            PrePopulatedValidationSupport prePopulatedSupport,
            InputStream structureDefinitionInputStream,
            String structureDefinitionUrl,
            String structureDefinitionVersion,
            String resourceType,
            String baseDefinition, boolean closeStream) throws IOException {
        StructureDefinition kbvPrErpCompositionStructureDefinition;
        FhirContext ctx = FhirContext.forR4();
        IParser xmlParser = ctx.newXmlParser();
        StructureDefinition structureDefinition;

        structureDefinition = xmlParser.parseResource(StructureDefinition.class,
                structureDefinitionInputStream);
        structureDefinition.setUrl(structureDefinitionUrl);
        structureDefinition.setBaseDefinition(baseDefinition);
        structureDefinition.setVersion(structureDefinitionVersion);
        structureDefinition.setType(resourceType);

        prePopulatedSupport.addStructureDefinition(structureDefinition);

        if(closeStream)
            structureDefinitionInputStream.close();
    }
}
