package health.ere.ps.service.muster16;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.muster16.parser.IMuster16FormParser;
import health.ere.ps.service.muster16.parser.Muster16FormDataParser;
import health.ere.ps.service.muster16.parser.Muster16SvgExtractorParser;

@ApplicationScoped
public class Muster16FormDataExtractorService {
    @Inject
    Muster16SvgExtractorParser parser;

    public String extractData(InputStream muster16PdfFile) throws IOException {
        PDDocument document = createDocumentRotate90(muster16PdfFile);
        String text = new PDFTextStripper().getText(document);

        return text;
    }

    public PDDocument createDocumentRotate90(InputStream muster16PdfFile) throws IOException {
        PDDocument document = PDDocument.load(muster16PdfFile);
        PDPage page = document.getDocumentCatalog().getPages().get(0);
        page.setRotation(90);
        return document;
    }

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

    public Muster16PrescriptionForm extractDataWithSvgExtractorParser(InputStream muster16PdfFile) throws URISyntaxException {
        parser.init(muster16PdfFile);

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
