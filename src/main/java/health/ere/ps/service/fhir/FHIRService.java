package health.ere.ps.service.fhir;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import org.hl7.fhir.r4.model.Bundle;

import health.ere.ps.config.AppConfig;
import health.ere.ps.event.BundlesEvent;
import health.ere.ps.event.Muster16PrescriptionFormEvent;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.fhir.bundle.EreBundle;
import health.ere.ps.service.fhir.bundle.PrescriptionBundlesBuilderV2;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;

@ApplicationScoped
public class FHIRService {

    private static final Logger log = Logger.getLogger(FHIRService.class.getName());

    @Inject
    AppConfig appConfig;

    @Inject
    PrescriptionBundleValidator prescriptionBundleValidator;

    @Inject
    Event<BundlesEvent> bundleEvent;

    @Inject
    Event<Exception> exceptionEvent;

    public void generatePrescriptionBundle(@ObservesAsync Muster16PrescriptionFormEvent muster16PrescriptionFormEvent) {
        Muster16PrescriptionForm muster16PrescriptionForm = muster16PrescriptionFormEvent.getMuster16PrescriptionForm();
        PrescriptionBundlesBuilderV2 bundleBuilder = new PrescriptionBundlesBuilderV2(muster16PrescriptionForm);

        List<Bundle> bundles = bundleBuilder.createBundles();

        bundles = bundles.stream().filter(bundle -> {

            // Warning: Do not use return value of validation method below to stop submission of
            // possible problem bundle to front-end. For now, only report possible
            // validation issues for generated outgoing bundle for review in the logs.

            //TODO: Create a separate configuration to enable/disable validating outgoing
            // bundles.
            if (appConfig.isValidateSignRequestBundles()) {
                return isOutgoingBundleOk((EreBundle) bundle);
            } else {
                return true;
            }
        }).collect(Collectors.toList());
        bundleEvent.fireAsync(new BundlesEvent(bundles));
    }

    private boolean isOutgoingBundleOk(EreBundle bundle) {
        return prescriptionBundleValidator.validateResource(bundle.encodeToJson(),
                true).isSuccessful();
    }
}
