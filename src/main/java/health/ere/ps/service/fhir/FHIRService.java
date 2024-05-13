package health.ere.ps.service.fhir;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Bundle;

import health.ere.ps.config.UserConfig;
import health.ere.ps.event.BundlesEvent;
import health.ere.ps.event.Muster16PrescriptionFormEvent;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.fhir.bundle.PrescriptionBundlesBuilder;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;

@ApplicationScoped
public class FHIRService {

    private static final Logger log = Logger.getLogger(FHIRService.class.getName());

    @Inject
    UserConfig userConfig;

    @Inject
    PrescriptionBundleValidator prescriptionBundleValidator;

    @Inject
    Event<BundlesEvent> bundleEvent;

    @Inject
    Event<Exception> exceptionEvent;

    private static final FhirContext fhirContext = FhirContext.forR4();

    public void generatePrescriptionBundle(@ObservesAsync Muster16PrescriptionFormEvent muster16PrescriptionFormEvent) {
        try {
            Muster16PrescriptionForm muster16PrescriptionForm = muster16PrescriptionFormEvent.getMuster16PrescriptionForm();
            PrescriptionBundlesBuilder bundleBuilder = new PrescriptionBundlesBuilder(muster16PrescriptionForm, userConfig.getPruefnummer());

            List<Bundle> bundles = bundleBuilder.createBundles();
            bundleEvent.fireAsync(new BundlesEvent(bundles));
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not create bundles", e);
            exceptionEvent.fireAsync(e);
        }
    }

    public static FhirContext getFhirContext() {
        return fhirContext;
    }
}