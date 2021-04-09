package com.erehealth.ps.service.muster16;

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
}
