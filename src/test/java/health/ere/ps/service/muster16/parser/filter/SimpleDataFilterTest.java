package health.ere.ps.service.muster16.parser.filter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SimpleDataFilterTest {
    @Inject
    Logger logger;

    @Test
    void filter() throws IOException {
        DataFilter simpleDataFilter = new SimpleDataFilter(Arrays.asList("\\-"));

        try (PDDocument document = PDDocument
                .load(getClass().getResourceAsStream("/muster-16-print-samples/manuel-blechschmidt-ibuprofen.pdf"))) {

            if (!document.isEncrypted()) {
                PDFTextStripper tStripper = new PDFTextStripper();

                tStripper.setSortByPosition(true);

                String pdfFileInText = tStripper.getText(document);

                logger.info("Raw string");
                logger.info(pdfFileInText);
                logger.info("======================================");
                // split by whitespace
                List<String> pdfLines = simpleDataFilter.filter(pdfFileInText, "\\r?\\n");
                String lines[] = pdfFileInText.split("\\r?\\n");

                logger.info("Unfiltered lines");
                for (String line : lines) {
                    logger.info(line);
                }

                logger.info("======================================");

                logger.info("Filtered lines");
                for (String line : pdfLines) {
                    logger.info(line);
                }
            }
        }
    }
}