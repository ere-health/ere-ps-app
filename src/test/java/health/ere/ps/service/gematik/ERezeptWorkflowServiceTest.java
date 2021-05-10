package health.ere.ps.service.gematik;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.parser.XMLParserException;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage;

public class ERezeptWorkflowServiceTest {

    private static Logger log = Logger.getLogger(ERezeptWorkflowServiceTest.class.getName());

    FhirContext fhirContext = FhirContext.forR4();

    String testBearerToken = "";

    @Test @Disabled
    void testCreateERezeptOnPresciptionServer() throws InvalidCanonicalizerException, XMLParserException, CanonicalizationException, FaultMessage, IOException {
        ERezeptWorkflowService eRezeptWorkflowService = new ERezeptWorkflowService();
        eRezeptWorkflowService.prescriptionserverUrl = "https://fd.erezept-instanz1.titus.ti-dienste.de";
        Bundle bundle = fhirContext.newXmlParser().parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml"));
        eRezeptWorkflowService.createERezeptOnPresciptionServer(testBearerToken, bundle);
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
