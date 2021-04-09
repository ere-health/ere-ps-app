package com.erehealth.ps.service.fhir;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

@ApplicationScoped
public class FHIRService {
    @Inject
    EventBus eventBus;

    private static Logger log = Logger.getLogger(FHIRService.class.getName());

    @ConsumeEvent("fhir-prescription-bundle")
    public void generatePrescriptionBundle(String muster16PdfData) {
        // TODO: Generate prescription SOAP message bundle
        log.info("FHIR Prescription Input Data Received For Processing: " + muster16PdfData);
    }
}
