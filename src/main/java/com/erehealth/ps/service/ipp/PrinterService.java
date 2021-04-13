package com.erehealth.ps.service.ipp;

import com.erehealth.ps.event.NewMuster16FormEvent;
import com.erehealth.ps.model.muster16.Muster16PrescriptionForm;
import com.erehealth.ps.service.muster16.Muster16FormDataExtractorService;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@ApplicationScoped
public class PrinterService {
    @Inject
    Event<NewMuster16FormEvent> newMuster16FormEvent;

    @Inject
    Muster16FormDataExtractorService muster16FormDataExtractor;

    public void print(InputStream muster16file) throws IOException {
        String pdfText = Muster16FormDataExtractorService.extractData(muster16file);

        Muster16PrescriptionForm muster16PrescriptionForm =
                muster16FormDataExtractor.extractData(pdfText);

        newMuster16FormEvent.fireAsync(new NewMuster16FormEvent(muster16PrescriptionForm));
    }
}
