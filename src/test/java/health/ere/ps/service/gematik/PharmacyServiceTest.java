package health.ere.ps.service.gematik;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import health.ere.ps.profile.RUTestProfile;
import health.ere.ps.service.connector.endpoint.SSLUtilities;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(RUTestProfile.class)
public class PharmacyServiceTest {

    private static final Logger log = Logger.getLogger(ERezeptWorkflowServiceTest.class.getName());
    private final IParser iParser = FhirContext.forR4().newXmlParser();

    @Inject
    PharmacyService pharmacyService;


    @BeforeEach
    void init() {
        try {
            // https://community.oracle.com/thread/1307033?start=0&tstart=0
            LogManager.getLogManager().readConfiguration(
                    ERezeptWorkflowServiceTest.class
                            .getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dumpTreshold", "999999");

        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();
    }

    @Test
    @Disabled
    void testGetEPrescriptionsForCardHandle() throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        pharmacyService.getEPrescriptionsForCardHandle(null, null, null);
    }

}