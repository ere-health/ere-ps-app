package health.ere.ps.service.muster16;


import java.net.URISyntaxException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;

import health.ere.ps.event.SVGExtractorResultEvent;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.muster16.parser.IMuster16FormParser;
import health.ere.ps.service.muster16.parser.Muster16FormDataParser;
import health.ere.ps.service.muster16.parser.Muster16SvgExtractorParser;

@ApplicationScoped
public class Muster16FormDataExtractorService {


    public Muster16PrescriptionForm extractData(String muster16PdfFileData) {
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

        return muster16Form;
    }

    public Muster16PrescriptionForm extractDataWithSvgExtractorParser(@ObservesAsync SVGExtractorResultEvent sVGExtractorResultEvent) throws URISyntaxException {
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

        return muster16Form;
    }
}
