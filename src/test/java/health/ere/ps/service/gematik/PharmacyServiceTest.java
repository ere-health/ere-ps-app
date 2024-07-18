package health.ere.ps.service.gematik;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import health.ere.ps.config.RuntimeConfig;
import io.quarkus.test.junit.QuarkusMock;
import jakarta.inject.Inject;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
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
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(RUTestProfile.class)
@Disabled
public class PharmacyServiceTest {

    private static final Logger log = Logger.getLogger(ERezeptWorkflowServiceTest.class.getName());
    private final IParser iParser = FhirContext.forR4().newXmlParser();
    private static final String FAILED_REJECTS_FILE = "dangling-e-prescriptions.json";

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
    void testRetryFailedRejects() throws IOException {
        String successPrescriptionId = "prescriptionId_success";
        String successSecret = "secret_success";
        RuntimeConfig successConfig = new RuntimeConfig();
        FailedRejectEntry successEntry = new FailedRejectEntry(successPrescriptionId, successSecret, successConfig);
        String failedPrescriptionId = "prescriptionId_failed";
        String failedSecret = "secret_failed";
        RuntimeConfig failedConfig = new RuntimeConfig();
        FailedRejectEntry failedEntry = new FailedRejectEntry(failedPrescriptionId, failedSecret, failedConfig);
        Path path = Paths.get(FAILED_REJECTS_FILE);

        // Mock the file content
        when(Files.exists(path)).thenReturn(true);



        Path path = Paths.get("dangling-e-prescriptions.dat");
        Files.write(path, "prescriptionId1,secret1\nprescriptionId2,secret2\n".getBytes());

        PharmacyService mockPharmacyService = new PharmacyService();
        mockPharmacyService.client = mock(Client.class);
        WebTarget mockWebTarget1 = mock(WebTarget.class);
        when(mockPharmacyService.client.target(anyString())).thenReturn(mockWebTarget1);
        when(mockWebTarget1.path("/Task/prescriptionId1/$reject")).thenReturn(mockWebTarget1);
        WebTarget mockWebTarget2 = mock(WebTarget.class);
        when(mockWebTarget1.path("/Task/prescriptionId2/$reject")).thenReturn(mockWebTarget2);


        Mockito.when(mockPharmacyService.attemptReject("prescriptionId1", "secret1")).thenReturn(true);
        Mockito.when(mockPharmacyService.attemptReject("prescriptionId2", "secret2")).thenReturn(false);
        Mockito.doCallRealMethod().when(mockPharmacyService).retryFailedRejects();
        QuarkusMock.installMockForType(mockPharmacyService, PharmacyService.class);

        mockPharmacyService.retryFailedRejects();

        String content = Files.readString(path);
        assert content.equals("prescriptionId2,secret2\n");
    }

    @Test
    @Disabled
    void testGetEPrescriptionsForCardHandle() throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        String correlationId = UUID.randomUUID().toString();
        pharmacyService.getEPrescriptionsForCardHandle(correlationId, null, null, null);
    }

}
