package health.ere.ps.service.muster16;


import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import health.ere.ps.event.Muster16PrescriptionFormEvent;
import health.ere.ps.event.SVGExtractorResultEvent;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.muster16.parser.IMuster16FormParser;
import health.ere.ps.service.muster16.parser.Muster16FormDataParser;
import health.ere.ps.service.muster16.parser.Muster16SvgExtractorParser;

@ApplicationScoped
public class Muster16FormDataExtractorService {

    private static Logger log = Logger.getLogger(Muster16FormDataExtractorService.class.getName());

    @Inject
    Event<Exception> exceptionEvent;

    @Inject
    Event<Muster16PrescriptionFormEvent> muster16PrescriptionFormEvent;

    public void extractData(String muster16PdfFileData) {
        IMuster16FormParser parser = new Muster16FormDataParser(muster16PdfFileData);
        Muster16PrescriptionForm muster16Form = new Muster16PrescriptionForm(
                parser.parseInsuranceCompany(),
                parser.parseInsuranceCompanyId(),
                parser.parsePatientFirstName(),
                parser.parsePatientLastName(),
                parser.parsePatientStreetName(),
                parser.parsePatientStreetNumber(),
                parser.parsePatientCity(),
                parser.parsePatientZipCode(),
                parser.parsePatientDateOfBirth(),
                parser.parsePatientInsuranceId(),
                parser.parseClinicId(),
                parser.parseDoctorId(),
                parser.parsePrescriptionDate(),
                parser.parsePrescriptionList()
        );

        muster16PrescriptionFormEvent.fireAsync(new Muster16PrescriptionFormEvent(muster16Form));
    }

    public void extractDataWithSvgExtractorParser(@ObservesAsync SVGExtractorResultEvent sVGExtractorResultEvent) {
        log.info("Muster16FormDataExtractorService.extractDataWithSvgExtractorParser");
        try {
            Muster16SvgExtractorParser parser = new Muster16SvgExtractorParser(sVGExtractorResultEvent.map);

            Muster16PrescriptionForm muster16Form = new Muster16PrescriptionForm(
                    parser.parseInsuranceCompany(),
                    parser.parseInsuranceCompanyId(),
                    parser.parsePatientFirstName(),
                    parser.parsePatientLastName(),
                    parser.parsePatientStreetName(),
                    parser.parsePatientStreetNumber(),
                    parser.parsePatientCity(),
                    parser.parsePatientZipCode(),
                    parser.parsePatientDateOfBirth(),
                    parser.parsePatientInsuranceId(),
                    parser.parseClinicId(),
                    parser.parseDoctorId(),
                    parser.parsePrescriptionDate(),
                    parser.parsePrescriptionList()
            );

            muster16PrescriptionFormEvent.fireAsync(new Muster16PrescriptionFormEvent(muster16Form));
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not parse results", e);
            exceptionEvent.fireAsync(e);
        }
    }
}
