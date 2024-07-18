package health.ere.ps.service.gematik;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.service.connector.endpoint.SSLUtilities;
import jakarta.inject.Inject;

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
    void appendFailedRejectToFile() throws IOException {
        String testDanglingPrescriptions = "target/test-dangling-e-prescriptions.dat";
        Path path = Paths.get(testDanglingPrescriptions);
        File file = path.toFile();
        if(file.exists()) {
            file.delete();
        }

        PharmacyService pharmacyService = new PharmacyService();
        pharmacyService.failedRejectsFile = testDanglingPrescriptions;
        RuntimeConfig runtimeConfig = new RuntimeConfig();
        runtimeConfig.setEHBAHandle("ehba");
        runtimeConfig.setSMCBHandle("smcb");
        UserConfigurations userConfigurations = new UserConfigurations();
        userConfigurations.setMandantId("m");
        userConfigurations.setClientSystemId("c");
        userConfigurations.setWorkplaceId("w");
        userConfigurations.setClientCertificate("ClientCertificate");
        userConfigurations.setClientCertificatePassword("ClientCertificatePassword");
        userConfigurations.setConnectorBaseURL("https://192.168.178.42/");
        runtimeConfig.updateProperties(userConfigurations);
        pharmacyService.appendFailedRejectToFile("prescriptionId", "secret", runtimeConfig);

        String testDanglingPrescriptionsData = new String(Files.readAllBytes(path));
        assertEquals("{\"prescriptionId\":\"prescriptionId\",\"secret\":\"secret\",\"runtimeConfig\":{\"configurations\":{\"erixaHotfolder\":null,\"erixaDrugstoreEmail\":null,\"erixaUserEmail\":null,\"erixaUserPassword\":null,\"erixaApiKey\":null,\"muster16TemplateProfile\":null,\"connectorBaseURL\":\"https://192.168.178.42/\",\"mandantId\":\"m\",\"workplaceId\":\"w\",\"clientSystemId\":\"c\",\"userId\":null,\"version\":null,\"tvMode\":null,\"clientCertificate\":\"ClientCertificate\",\"clientCertificatePassword\":\"ClientCertificatePassword\",\"basicAuthUsername\":null,\"basicAuthPassword\":null,\"pruefnummer\":null},\"sendPreview\":true,\"idpBaseURL\":null,\"idpAuthRequestRedirectURL\":null,\"idpClientId\":null,\"prescriptionServerURL\":null,\"ehbahandle\":\"ehba\",\"smcbhandle\":\"smcb\",\"connectorAddress\":\"192.168.178.42\",\"userId\":null,\"erixaHotfolder\":null,\"erixaReceiverEmail\":null,\"erixaUserEmail\":null,\"erixaUserPassword\":null,\"connectorBaseURL\":\"https://192.168.178.42/\",\"mandantId\":\"m\",\"workplaceId\":\"w\",\"clientSystemId\":\"c\",\"tvMode\":null,\"connectorVersion\":null,\"pruefnummer\":null,\"erixaApiKey\":null,\"muster16TemplateConfiguration\":\"DENS\"}}"+System.lineSeparator(), testDanglingPrescriptionsData);
        file.delete();

        FailedRejectEntry read = PharmacyService.objectMapper.readValue(testDanglingPrescriptionsData, FailedRejectEntry.class);
        assertEquals("prescriptionId", read.getPrescriptionId());
        assertEquals("secret", read.getSecret());
        assertEquals(runtimeConfig, read.getRuntimeConfig());
        assertEquals(runtimeConfig.getConfigurations(), read.getRuntimeConfig().getConfigurations());
    }

    @Test
    void appendFailedRejectToFileNull() throws IOException {
        String testDanglingPrescriptions = "target/test-dangling-e-prescriptions.dat";
        Path path = Paths.get(testDanglingPrescriptions);
        PharmacyService pharmacyService = new PharmacyService();
        pharmacyService.failedRejectsFile = testDanglingPrescriptions;
        
        pharmacyService.appendFailedRejectToFile(null, null, null);
        path.toFile().delete();
    }

    @Test
    void appendFailedRejectToFileTwoLines() throws IOException {
        String testDanglingPrescriptions = "target/test-dangling-e-prescriptions.dat";
        Path path = Paths.get(testDanglingPrescriptions);
        PharmacyService pharmacyService = new PharmacyService();
        pharmacyService.failedRejectsFile = testDanglingPrescriptions;
        RuntimeConfig runtimeConfig = new RuntimeConfig();
        pharmacyService.appendFailedRejectToFile("prescriptionId", "secret", runtimeConfig);
        pharmacyService.appendFailedRejectToFile("prescriptionId2", "secret2", runtimeConfig);

        List<String> lines = Files.readAllLines(path);
        
        FailedRejectEntry read = PharmacyService.objectMapper.readValue(lines.get(0), FailedRejectEntry.class);
        assertEquals("prescriptionId", read.getPrescriptionId());
        assertEquals("secret", read.getSecret());
        assertEquals(runtimeConfig, read.getRuntimeConfig());
        assertEquals(runtimeConfig.getConfigurations(), read.getRuntimeConfig().getConfigurations());

        FailedRejectEntry read2 = PharmacyService.objectMapper.readValue(lines.get(1), FailedRejectEntry.class);
        assertEquals("prescriptionId2", read2.getPrescriptionId());
        assertEquals("secret2", read2.getSecret());
        assertEquals(runtimeConfig, read2.getRuntimeConfig());
        assertEquals(runtimeConfig.getConfigurations(), read2.getRuntimeConfig().getConfigurations());

        path.toFile().delete();
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

    }

    @Test
    @Disabled
    void testGetEPrescriptionsForCardHandle() throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        String correlationId = UUID.randomUUID().toString();
        pharmacyService.getEPrescriptionsForCardHandle(correlationId, null, null, null);
    }

}
