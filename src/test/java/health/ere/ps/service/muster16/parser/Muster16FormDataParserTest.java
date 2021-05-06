package health.ere.ps.service.muster16.parser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class Muster16FormDataParserTest {

    @Inject
    Logger logger;

    @Test
    public void testReadMuster16FormPDF() throws IOException {
        try (PDDocument document = PDDocument.load(
                getClass().getResourceAsStream(
                        "/muster-16-print-samples/test1.pdf"))) {

            document.getClass();

            if (!document.isEncrypted()) {
                PDFTextStripper tStripper = new PDFTextStripper();
                String pdfFileInText = tStripper.getText(document);

                // split by whitespace
                String lines[] = pdfFileInText.split("\\r?\\n");
                List<String> pdfLines = new ArrayList<>();
                StringBuilder sb = new StringBuilder();
                for (String line : lines) {
                    logger.info(line);
                }
            }
        }
    }
}