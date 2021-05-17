package health.ere.ps.service.fhir;

import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import org.hl7.fhir.r4.model.Bundle;

import health.ere.ps.event.BundlesEvent;
import health.ere.ps.event.Muster16PrescriptionFormEvent;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.fhir.bundle.PrescriptionBundleBuilder;

@ApplicationScoped
public class FHIRService {

    @Inject
    Event<BundlesEvent> bundleEvent;

    @Inject
    Event<Exception> exceptionEvent;

    private static Logger log = Logger.getLogger(FHIRService.class.getName());

    public void generatePrescriptionBundle(@ObservesAsync Muster16PrescriptionFormEvent muster16PrescriptionFormEvent) {
        log.info("FHIRService.generatePrescriptionBundle");
        Muster16PrescriptionForm muster16PrescriptionForm = muster16PrescriptionFormEvent.muster16PrescriptionForm;

        PrescriptionBundleBuilder bundleBuilder =
                new PrescriptionBundleBuilder(muster16PrescriptionForm);

        try {
            Bundle bundle = bundleBuilder.createBundle();
            bundleEvent.fireAsync(new BundlesEvent(bundle));
        } catch (ParseException e) {
            log.log(Level.SEVERE,
                    "Exception encountered while generating e-prescription bundle.", e);
            exceptionEvent.fireAsync(e);
        }
    }
}
