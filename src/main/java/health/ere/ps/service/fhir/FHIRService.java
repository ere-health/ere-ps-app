package health.ere.ps.service.fhir;

import org.hl7.fhir.r4.model.Bundle;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.ValidationResult;
import health.ere.ps.event.BundlesEvent;
import health.ere.ps.event.Muster16PrescriptionFormEvent;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.fhir.bundle.PrescriptionBundlesBuilder;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;

@ApplicationScoped
public class FHIRService {

    private static final Logger log = Logger.getLogger(FHIRService.class.getName());

    @Inject
    PrescriptionBundleValidator prescriptionBundleValidator;

    @Inject
    Event<BundlesEvent> bundleEvent;

    @Inject
    Event<Exception> exceptionEvent;

    public void generatePrescriptionBundle(@ObservesAsync Muster16PrescriptionFormEvent muster16PrescriptionFormEvent) {
        Muster16PrescriptionForm muster16PrescriptionForm = muster16PrescriptionFormEvent.getMuster16PrescriptionForm();
        PrescriptionBundlesBuilder bundleBuilder = new PrescriptionBundlesBuilder(muster16PrescriptionForm);

        try {
            List<Bundle> bundles = bundleBuilder.createBundles();
            FhirContext ctx = FhirContext.forR4();

            IParser jsonParser = ctx.newJsonParser();

            jsonParser.setPrettyPrint(true);

            bundles.forEach(bundle -> {
                String jsonSerialized = jsonParser.encodeResourceToString(bundle);

                log.info(String.format("Bundle builder created bundle: %s with validation results" +
                        " shown below:", jsonSerialized));

                // Warning: Do not use return value of validation method below to stop submission of
                // possible problem bundle to front-end. For now, only report possible
                // validation issues for generated outgoing bundle for review in the logs.
                doValidationChecks(bundle);
                bundleEvent.fireAsync(new BundlesEvent(bundle));
            });
        } catch (ParseException e) {
            log.log(Level.SEVERE, "Exception encountered while generating e-prescription bundle" +
                    ".", e);
            exceptionEvent.fireAsync(e);
        }
    }

    private boolean doValidationChecks(Bundle bundle) {
        return prescriptionBundleValidator.validateResource(bundle, true).isSuccessful();
    }
}
