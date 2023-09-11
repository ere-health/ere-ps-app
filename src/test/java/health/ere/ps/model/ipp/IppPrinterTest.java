package health.ere.ps.model.ipp;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.model.JobState;
import com.hp.jipp.model.PrinterState;
import static com.hp.jipp.model.Types.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IppPrinterTest {

    private IppPrinter ippPrinter;
    private final URI testUri = URI.create("http://localhost:8080");


    @BeforeEach
    public void setUp() {
        ippPrinter = new IppPrinter();
    }

    @Test
    public void testGetPrinterAttributes() {
        List<Attribute<?>> attributes = ippPrinter.getPrinterAttributes(testUri);

        // Expected URI attribute
        Attribute<?> uriAttribute = attributes.stream().filter(attr -> attr.getType().equals(printerUriSupported)).findFirst().orElse(null);
        assertNotNull(uriAttribute);
        assertEquals(testUri, uriAttribute.get(0));

        // Check if default printer attributes are present
        assertTrue(attributes.contains(printerName.of(DefaultAttributes.PRINTER_NAME)));
        assertEquals(PrinterState.idle, attributes.stream().filter(attr -> attr.getType().equals(printerState)).findFirst().get().get(0));
    }


    @Test
    public void testPrinterWithNoQueuedJobs() {
        // all jobs are cleared from the queue or printer is set to a state with no jobs
        List<Attribute<?>> attributes = ippPrinter.getPrinterAttributes(testUri);
        Attribute<?> jobsAttribute = attributes.stream()
                .filter(attr -> attr.getType().equals(queuedJobCount))
                .findFirst().orElse(null);

        assertNotNull(jobsAttribute);
        assertEquals(0, (int) jobsAttribute.get(0));
    }

    @Test
    public void testGetOperationAttributes() {
        List<Attribute<?>> operationAttributes = ippPrinter.getOperationAttributes();
        assertEquals("utf-8", operationAttributes.stream().filter(attr -> attr.getType().equals(attributesCharset)).findFirst().get().get(0));
        assertEquals("en-us", operationAttributes.stream().filter(attr -> attr.getType().equals(attributesNaturalLanguage)).findFirst().get().get(0));
    }

    @Test
    public void testGetJobAttributes() {

        List<Attribute<?>> jobAttributes = ippPrinter.getJobAttributes(testUri);

        // Check for incremented job ID in URI
        URI expectedUri = URI.create(testUri + "/job/1");
        assertEquals(expectedUri, jobAttributes.stream().filter(attr -> attr.getType().equals(jobUri)).findFirst().get().get(0));
        assertEquals(JobState.pending, jobAttributes.stream().filter(attr -> attr.getType().equals(jobState)).findFirst().get().get(0));
    }

}
