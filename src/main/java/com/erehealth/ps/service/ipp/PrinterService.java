package com.erehealth.ps.service.ipp;

import com.erehealth.ps.service.muster16.Muster16FormDataExtractorService;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.vertx.core.eventbus.EventBus;

@ApplicationScoped
public class PrinterService {
    @Inject
    EventBus eventBus;

    @Inject
    Muster16FormDataExtractorService muster16FormDataExtractor;

    public void print(InputStream muster16file) throws IOException {
        String pdfText = muster16FormDataExtractor.extractData(muster16file);

        eventBus.send(pdfText, "fhir-prescription-bundle");
    }
}
