package health.ere.ps.service.gematik;

import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;

public class ERezeptWorkflowServiceTest {

    private static Logger log = Logger.getLogger(ERezeptWorkflowServiceTest.class.getName());

    FhirContext fhirContext = FhirContext.forR4();

    @Test
    void testCreateERezeptOnPresciptionServer() {
        // TODO
    }

    @Test
    void testUpdateERezeptTask() {
       // TODO
    }

    @Test
    void testUpdateBundleWithTask() {
        // TODO
    }

    @Test
    void testSignBundleWithIdentifiers() {
        // TODO
    }

    @Test
    void testCreateERezeptTask() {
        // TODO
    }
}
