package health.ere.ps.service.ipp;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import org.apache.pdfbox.pdmodel.PDDocument;

import health.ere.ps.event.NewMuster16FormEvent;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.muster16.Muster16FormDataExtractorService;

@ApplicationScoped
public class PrinterService {
    @Inject
    Event<NewMuster16FormEvent> newMuster16FormEvent;

    @Inject
    Muster16FormDataExtractorService muster16FormDataExtractor;

    public void print(@ObservesAsync PDDocument muster16file) throws IOException {
        String pdfText = Muster16FormDataExtractorService.extractData(muster16file);

        Muster16PrescriptionForm muster16PrescriptionForm =
                muster16FormDataExtractor.extractData(pdfText);

        newMuster16FormEvent.fireAsync(new NewMuster16FormEvent(muster16PrescriptionForm));
    }
}
