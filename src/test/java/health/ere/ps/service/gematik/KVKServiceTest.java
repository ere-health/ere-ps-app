package health.ere.ps.service.gematik;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// @QuarkusTest fährt eine kleine Test-Instanz der App hoch
@QuarkusTest
public class KVKServiceTest {

    // Wir bitten das System, uns die echte KVKService Klasse zu geben
    @Inject
    KVKService kvkService;

    @Test
    public void testServiceInjection() {
        // TEST-LOGIK:
        // Wir prüfen nur, ob der Service erfolgreich erstellt wurde 
        // und nicht "null" (leer) ist.
        // Wenn das klappt, funktioniert die Dependency Injection.
        assertNotNull(kvkService, "Der KVKService sollte erfolgreich geladen werden.");
    }
}