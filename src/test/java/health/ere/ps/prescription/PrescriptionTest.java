import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class PrescriptionTest {

    @TestHTTPResource("/frontend/app/src/settings") 
    URL url;

    @Test
    public void testPrescritpionButtons() throws IOException {
        try (InputStream in = url.openStream()) {
            String contents = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            Assertions.assertTrue(contents.contains("Status"));
            Assertions.assertTrue(contents.contains("Karten"));
            Assertions.assertTrue(contents.contains("Konnektor"));
            Assertions.assertTrue(contents.contains("Dokumenterkennung"));
            Assertions.assertTrue(contents.contains("KIM für Direktzuweisung"));
            Assertions.assertTrue(contents.contains("KBV Prüfnummer"));
            Assertions.assertTrue(contents.contains("<th>Komponent</th>"));

        }
    }
}