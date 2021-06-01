package health.ere.ps.service.muster16;


import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
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
import health.ere.ps.service.muster16.parser.extractor.SimpleDataExtractor;
import health.ere.ps.service.muster16.parser.formatter.SimpleDataFormatter;

@ApplicationScoped
public class Muster16FormDataExtractorService {

    private static Logger log = Logger.getLogger(Muster16FormDataExtractorService.class.getName());

    @Inject
    Event<Exception> exceptionEvent;

    @Inject
    Event<Muster16PrescriptionFormEvent> muster16PrescriptionFormEvent;

    public void extractData(String muster16PdfFileData) {
        IMuster16FormParser parser =
                new Muster16FormDataParser(
                        new ByteArrayInputStream(
                                StringUtils.defaultString(
                                        muster16PdfFileData).getBytes(StandardCharsets.UTF_8)),
                        new SimpleDataExtractor(), null, new SimpleDataFormatter());
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

            Muster16PrescriptionForm muster16Form = fillForm(parser);

            muster16PrescriptionFormEvent.fireAsync(new Muster16PrescriptionFormEvent(muster16Form));
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not parse results", e);
            exceptionEvent.fireAsync(e);
        }
    }

    public static Muster16PrescriptionForm fillForm(Muster16SvgExtractorParser parser) {
        return new Muster16PrescriptionForm(
            parser.parseInsuranceCompany(),
            parser.parseInsuranceCompanyId(),
            parser.parsePatientFirstName(),
            parser.parsePatientLastName(),
            parser.parsePatientStreetName(),
            parser.parsePatientStreetNumber(),
            parser.parsePatientCity(),
            parser.parsePatientZipCode(),
            parser.parsePatientInsuranceId(),
            parser.parsePatientDateOfBirth(),
            parser.parseClinicId(),
            parser.parseDoctorId(),
            parser.parsePrescriptionDate(),
            parser.parsePrescriptionList()
        );
    }
}
