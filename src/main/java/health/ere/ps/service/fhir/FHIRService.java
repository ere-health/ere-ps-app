package health.ere.ps.service.fhir;

import health.ere.ps.event.BundleEvent;
import health.ere.ps.event.NewMuster16FormEvent;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.fhir.bundle.PrescriptionBundleBuilder;

import org.hl7.fhir.r4.model.Bundle;
import org.jboss.logmanager.Level;

import java.text.ParseException;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

@ApplicationScoped
public class FHIRService {

    @Inject
    Event<BundleEvent> bundleEvent;

    private static Logger log = Logger.getLogger(FHIRService.class.getName());

    public void generatePrescriptionBundle(@ObservesAsync NewMuster16FormEvent newMuster16FormEvent) {
        Muster16PrescriptionForm muster16PrescriptionForm =
                newMuster16FormEvent.getMuster16PrescriptionForm();

        PrescriptionBundleBuilder bundleBuilder =
                new PrescriptionBundleBuilder(muster16PrescriptionForm);

        try {
            Bundle bundle = bundleBuilder.createBundle();
            bundleEvent.fire(new BundleEvent(bundle));
        } catch (ParseException e) {
            log.log(Level.ERROR,
                    "Exception encountered while generating e-prescription bundle.", e);
            //TODO: Send/publish error notification
        }

        //TODO: Post process created bundle - e.g. display on web page or send to connector.
    }
}
