package health.ere.ps.service.muster16;


import health.ere.ps.event.Muster16PrescriptionFormEvent;
import health.ere.ps.event.SVGExtractorResultEvent;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.muster16.parser.IMuster16FormParser;
import health.ere.ps.service.muster16.parser.rgxer.Muster16SvgRegexParser;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class Muster16FormDataExtractorService {

    private static final Logger log = Logger.getLogger(Muster16FormDataExtractorService.class.getName());

    @Inject
    Event<Exception> exceptionEvent;

    @Inject
    Event<Muster16PrescriptionFormEvent> muster16PrescriptionFormEvent;


    public void extractDataWithSvgExtractorParser(@ObservesAsync SVGExtractorResultEvent sVGExtractorResultEvent) {
        try {
            Muster16SvgRegexParser parser = new Muster16SvgRegexParser(sVGExtractorResultEvent.getSvgExtractionResult());
            Muster16PrescriptionForm muster16Form = fillForm(parser);

            muster16PrescriptionFormEvent.fireAsync(new Muster16PrescriptionFormEvent(muster16Form));
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not parse results", e);
            exceptionEvent.fireAsync(e);
        }
    }

    public static Muster16PrescriptionForm fillForm(IMuster16FormParser parser) {
        return new Muster16PrescriptionForm(
            parser.parseInsuranceCompany(),
            parser.parseInsuranceCompanyId(),
            parser.parsePatientNamePrefix(),
            parser.parsePatientFirstName(),
            parser.parsePatientLastName(),
            parser.parsePatientStreetName(),
            parser.parsePatientStreetNumber(),
            parser.parsePatientCity(),
            parser.parsePatientZipCode(),
            parser.parsePatientInsuranceId(),
            parser.parsePatientDateOfBirth(),
            parser.parsePatientStatus(),
            parser.parseClinicId(),
            parser.parseDoctorId(),
            parser.parsePrescriptionDate(),
            parser.parsePractitionerFirstName(),
            parser.parsePractitionerLastName(),
            parser.parsePractitionerStreetName(),
            parser.parsePractitionerStreetNumber(),
            parser.parsePractitionerCity(),
            parser.parsePractitionerZipCode(),
            parser.parsePractitionerNamePrefix(),
            parser.parsePractitionerPhoneNumber(),
            parser.parsePractitionerFaxNumber(),
            parser.parseIsWithPayment(),
            parser.parsePrescriptionList()
        );
    }
}
