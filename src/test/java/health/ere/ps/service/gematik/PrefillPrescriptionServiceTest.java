package health.ere.ps.service.gematik;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.util.logging.LogManager;

import javax.inject.Inject;
import javax.naming.InvalidNameException;
import javax.xml.bind.JAXBException;

import org.bouncycastle.crypto.CryptoException;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage;
import health.ere.ps.config.UserConfig;
import health.ere.ps.profile.RUTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@Disabled
@TestProfile(RUTestProfile.class)
class PrefillPrescriptionServiceTest {

    @Inject
    UserConfig userConfig;

    @Inject
    PrefillPrescriptionService prefillPrescriptionService;

    IParser iParser = FhirContext.forR4().newXmlParser();

    @BeforeAll
    public static void init() {

        try {
            // https://community.oracle.com/thread/1307033?start=0&tstart=0
            LogManager.getLogManager().readConfiguration(
                    PrefillPrescriptionServiceTest.class
                            .getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dumpTreshold", "999999");
    }

    @Test
    void testGet() throws FaultMessage, de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage, JAXBException,
            de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage, CryptoException, IOException,
            InvalidNameException, CertificateEncodingException {

        Bundle bundle = prefillPrescriptionService.get(null);
        iParser.setPrettyPrint(true);
        System.out.println(iParser.encodeResourceToString(bundle));
    }

}
