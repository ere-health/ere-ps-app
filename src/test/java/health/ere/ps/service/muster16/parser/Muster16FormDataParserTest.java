package health.ere.ps.service.muster16.parser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Muster16FormDataParserTest {

    @Test
    public void testReadMuster16FormPDF() throws IOException {
        try (PDDocument document = PDDocument.load(
                getClass().getResourceAsStream(
                        "/muster-16-print-samples/apraxos_DIN_A4_Output_F-job_222.pdf"))) {

            document.getClass();

            if (!document.isEncrypted()) {
                PDFTextStripper tStripper = new PDFTextStripper();
                String pdfFileInText = tStripper.getText(document);

                // split by whitespace
                String lines[] = pdfFileInText.split("\\r?\\n");
                List<String> pdfLines = new ArrayList<>();
                StringBuilder sb = new StringBuilder();
                for (String line : lines) {
                    System.out.println(line);
                }
            }
        }
    }
}