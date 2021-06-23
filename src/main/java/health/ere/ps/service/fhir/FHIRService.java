package health.ere.ps.service.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.ere.ps.event.BundlesEvent;
import health.ere.ps.event.Muster16PrescriptionFormEvent;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.fhir.bundle.PrescriptionBundlesBuilder;
import org.hl7.fhir.r4.model.Bundle;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class FHIRService {

    private static final Logger log = Logger.getLogger(FHIRService.class.getName());

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
            IParser parser = ctx.newJsonParser();
            parser.setPrettyPrint(true);


            bundles.forEach(bundle -> {
                String serialized = parser.encodeResourceToString(bundle);

                log.info("Bundle builder created bundle: " + serialized);
                bundleEvent.fireAsync(new BundlesEvent(bundle));
            });
        } catch (ParseException e) {
            log.log(Level.SEVERE, "Exception encountered while generating e-prescription bundle.", e);
            exceptionEvent.fireAsync(e);
        }
    }
}
