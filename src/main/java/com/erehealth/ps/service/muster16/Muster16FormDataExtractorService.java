package com.erehealth.ps.service.muster16;

import com.erehealth.ps.model.muster16.Muster16PrescriptionForm;
import com.erehealth.ps.service.muster16.parser.Muster16FormDataParser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Muster16FormDataExtractorService {
    public String extractData(InputStream muster16PdfFile) throws IOException {
        PDDocument doc = PDDocument.load(muster16PdfFile);
        String text = new PDFTextStripper().getText(doc);

        return text;
    }

    public Muster16PrescriptionForm extractData(String muster16PdfFileData) {
        Muster16FormDataParser parser = new Muster16FormDataParser(muster16PdfFileData);
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
                parser.parseClinicId(),
                parser.parseDoctorId(),
                parser.parsePrescriptionDate(),
                parser.parsePrescriptionList()
        );

        return muster16Form;
    }
}
