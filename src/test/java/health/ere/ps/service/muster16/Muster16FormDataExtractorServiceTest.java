package health.ere.ps.service.muster16;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class Muster16FormDataExtractorServiceTest {

    @Test
    public void testExtractData() throws IOException {
        String text = Muster16FormDataExtractorService
                .extractData(getClass().getResourceAsStream("/muster-16-print-samples/cgm-z1-manuel-blechschmidt.pdf"));
        assertEquals("X\n" + "Amoxicillin 1000mg N2\n" + "3x täglich alle 8 Std\n"
                        + "-  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -\n" + "1234\n" + "Zahnärzte\n"
                        + "Dr. Zahnarzt Ein & Dr. Zahnarzt Zwei\n" + "In der tollen Str.115\n" + "12345 Berlin\n"
                        + "Tel. 030/123 4567\n" + "TK > Brandenburg            83\n" + "Blechschmidt\n"
                        + "Manuel               16.07.86\n" + "Droysenstr. 7\n" + "D 10629 Berlin          \n"
                        + "100696012 V062074590   1000000\n" + " 30001234  30001234  13.04.21\n", text);
    }
}