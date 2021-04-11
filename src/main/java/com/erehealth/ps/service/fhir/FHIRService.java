package com.erehealth.ps.service.fhir;

import com.erehealth.ps.event.NewMuster16FormEvent;
import com.erehealth.ps.model.muster16.Muster16PrescriptionForm;
import com.erehealth.ps.service.fhir.bundle.PrescriptionBundleBuilder;

import org.hl7.fhir.r4.model.Bundle;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;

@ApplicationScoped
public class FHIRService {
    private static Logger log = Logger.getLogger(FHIRService.class.getName());

    public void generatePrescriptionBundle(@ObservesAsync NewMuster16FormEvent newMuster16FormEvent) {
        Muster16PrescriptionForm muster16PrescriptionForm =
                newMuster16FormEvent.getMuster16PrescriptionForm();

        PrescriptionBundleBuilder bundleBuilder =
                new PrescriptionBundleBuilder(muster16PrescriptionForm);

        Bundle bundle = bundleBuilder.createBundle();

        //TODO: Post process created bundle - e.g. display on web page or send to connector.
    }
}
