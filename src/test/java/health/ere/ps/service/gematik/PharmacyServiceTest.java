package health.ere.ps.service.gematik;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.profile.RUTestProfile;
import health.ere.ps.service.connector.endpoint.SSLUtilities;
import health.ere.ps.service.fhir.prescription.PrescriptionService;
import health.ere.ps.service.pdf.DocumentService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonReader;
import jakarta.ws.rs.WebApplicationException;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestProfile(RUTestProfile.class)
@Disabled
public class PharmacyServiceTest {

    private static final Logger log = Logger.getLogger(ERezeptWorkflowServiceTest.class.getName());

    @Inject
    ERezeptWorkflowService eRezeptWorkflowService;

    @Inject
    PrescriptionService prescriptionService;

    @Inject
    PharmacyService pharmacyService;

    @Inject
    UserConfig userConfig;

    @BeforeEach
    void init() {
        try {
            // https://community.oracle.com/thread/1307033?start=0&tstart=0
            InputStream is = ERezeptWorkflowServiceTest.class.getResourceAsStream("/logging.properties");
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            log.severe(e.getMessage());
        }

        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dumpTreshold", "999999");

        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();
    }

    @Inject
    DocumentService documentService;

    @Test
    public void testBundle() throws Exception {
        String bundlesString = Files.readString(Path.of("bundle.json"));

        JsonReader reader = Json.createReader(new StringReader(bundlesString.replace("\t", "")));
        JsonArray jsonArray = reader.readArray();
        List<BundleWithAccessCodeOrThrowable> bundles = jsonArray.stream()
            .map(jv -> documentService.convert(jv))
            .filter(Objects::nonNull)
            .toList();

        ByteArrayOutputStream os = documentService.generateERezeptPdf(bundles);
        assertNotNull(os);
    }

    @Test
    void testGetEPrescriptionsForCardHandle() {
        RuntimeConfig runtimeConfig = new RuntimeConfig();
        runtimeConfig.copyValuesFromUserConfig(userConfig);

        WebApplicationException exception = assertThrows(WebApplicationException.class, () ->
            pharmacyService.getEPrescriptionsForCardHandle(null, null, runtimeConfig)
        );
        assertTrue(exception.getMessage().contains("endpoint is forbidden for professionOID 1.2.276.0.76.4.50"));
    }

    @Test
    @Disabled
    void testCreateERezeptOnPrescriptionServerFromXMLBundle() throws Exception {
        String bundle = Files.readString(Paths.get("src/test/resources/evdga/EVDGA_Bundle.xml"));
        Bundle[] bundles = prescriptionService.parseFromString(bundle);

        List<BundleWithAccessCodeOrThrowable> uploadedBundle = eRezeptWorkflowService.createMultipleERezeptsOnPrescriptionServer(
            Arrays.asList(bundles), "162", null, null, null
        );
        DocumentService documentService = new DocumentService();
        documentService.init();
        ByteArrayOutputStream a = documentService.generateERezeptPdf(uploadedBundle);
        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mmX").withZone(UTC).format(Instant.now());
        Files.write(Paths.get("target/E-Rezept-" + thisMoment + ".pdf"), a.toByteArray());
    }
}