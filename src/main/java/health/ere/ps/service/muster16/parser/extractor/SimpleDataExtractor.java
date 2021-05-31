package health.ere.ps.service.muster16.parser.extractor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.util.Optional;

import health.ere.ps.exception.muster16.parser.extractor.Muster16DataExtractorException;

public class SimpleDataExtractor implements DataExtractor<String> {
    @Override
    public Optional<String> extractData(InputStream muster16PdfInputStream)
            throws Muster16DataExtractorException {
        Optional<String> extractedData = Optional.empty();

        try {
            try (PDDocument document = PDDocument.load(muster16PdfInputStream)) {

                if (!document.isEncrypted()) {
                    PDFTextStripper tStripper = new PDFTextStripper();

                    tStripper.setSortByPosition(true);

                    String pdfFileInText = tStripper.getText(document);

                    extractedData = Optional.ofNullable(pdfFileInText);
                }
            }

        } catch(Throwable e) {
            throw new Muster16DataExtractorException("Muster 16 extractor exception", e);
        }

        return extractedData;
    }
}
