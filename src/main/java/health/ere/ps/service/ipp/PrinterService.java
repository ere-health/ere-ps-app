package health.ere.ps.service.ipp;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import health.ere.ps.event.NewMuster16FormEvent;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.muster16.Muster16FormDataExtractorService;

@ApplicationScoped
public class PrinterService {
    @Inject
    Event<NewMuster16FormEvent> newMuster16FormEvent;

    @Inject
    Muster16FormDataExtractorService muster16FormDataExtractor;

    public void print(InputStream muster16file) throws IOException {
        String pdfText = muster16FormDataExtractor.extractData(muster16file);

        Muster16PrescriptionForm muster16PrescriptionForm =
                muster16FormDataExtractor.extractData(pdfText);

        newMuster16FormEvent.fireAsync(new NewMuster16FormEvent(muster16PrescriptionForm));
    }
}
