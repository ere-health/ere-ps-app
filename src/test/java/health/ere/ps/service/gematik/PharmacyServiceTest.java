package health.ere.ps.service.gematik;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.jmx.ReadEPrescriptionsMXBeanImpl;
import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.service.gematik.PharmacyService.ReadVSDResult;
import health.ere.ps.service.idp.BearerTokenService;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.DatatypeConverter;
import jakarta.xml.ws.Holder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PharmacyServiceTest {

    private static final Logger log = Logger.getLogger(PharmacyServiceTest.class.getName());

    private static final Path FAILED_REJECTS_FILE = Paths.get("target/test-dangling-e-prescriptions.dat");

    static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    private PharmacyService pharmacyService;

    @BeforeEach
    void init() throws IOException {
        Files.deleteIfExists(FAILED_REJECTS_FILE);
        pharmacyService = new PharmacyService();
        pharmacyService.failedRejectsFile = FAILED_REJECTS_FILE;
    }

    @AfterEach
    void cleanup() throws Exception {
        pharmacyService.close();
    }

    @Test
    void appendFailedRejectToFile() throws Exception {
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
        pharmacyService.appendFailedRejectToFile("prescriptionId", "secret", runtimeConfig).get(2, TimeUnit.SECONDS);

        String testDanglingPrescriptionsData = new String(Files.readAllBytes(FAILED_REJECTS_FILE));
        assertEquals("{\"prescriptionId\":\"prescriptionId\",\"runtimeConfig\":{\"clientSystemId\":\"c\",\"configurations\":{\"basicAuthPassword\":null,\"basicAuthUsername\":null,\"clientCertificate\":\"ClientCertificate\",\"clientCertificatePassword\":\"ClientCertificatePassword\",\"clientSystemId\":\"c\",\"connectorBaseURL\":\"https://192.168.178.42/\",\"erixaApiKey\":null,\"erixaDrugstoreEmail\":null,\"erixaHotfolder\":null,\"erixaUserEmail\":null,\"erixaUserPassword\":null,\"konnektorHost\":\"192.168.178.42\",\"mandantId\":\"m\",\"muster16TemplateProfile\":null,\"pruefnummer\":null,\"tvMode\":null,\"userId\":null,\"version\":null,\"workplaceId\":\"w\"},\"connectorAddress\":\"192.168.178.42\",\"connectorBaseURL\":\"https://192.168.178.42/\",\"connectorVersion\":null,\"ehbahandle\":\"ehba\",\"erixaApiKey\":null,\"erixaHotfolder\":null,\"erixaReceiverEmail\":null,\"erixaUserEmail\":null,\"erixaUserPassword\":null,\"idpAuthRequestRedirectURL\":null,\"idpBaseURL\":null,\"idpClientId\":null,\"konnektorHost\":\"192.168.178.42\",\"mandantId\":\"m\",\"muster16TemplateConfiguration\":\"DENS\",\"prescriptionServerURL\":null,\"pruefnummer\":null,\"sendPreview\":true,\"smcbhandle\":\"smcb\",\"tvMode\":null,\"userId\":null,\"workplaceId\":\"w\"},\"secret\":\"secret\"}"
            + System.lineSeparator(), testDanglingPrescriptionsData);

        FailedRejectEntry read = PharmacyService.objectMapper.readValue(testDanglingPrescriptionsData, FailedRejectEntry.class);
        assertEquals("prescriptionId", read.getPrescriptionId());
        assertEquals("secret", read.getSecret());
        assertEquals(runtimeConfig, read.getRuntimeConfig());
        assertEquals(runtimeConfig.getUserConfigurations(), read.getRuntimeConfig().getUserConfigurations());
    }

    @Test
    void appendFailedRejectToFileNull() throws Exception {
        pharmacyService.appendFailedRejectToFile(null, null, null).get(2, TimeUnit.SECONDS);
        String testDanglingPrescriptionsData = new String(Files.readAllBytes(FAILED_REJECTS_FILE));
        assertFalse(testDanglingPrescriptionsData.isEmpty());
    }

    @Test
    void appendFailedRejectToFileTwoLines() throws Exception {
        RuntimeConfig runtimeConfig = new RuntimeConfig();
        pharmacyService.appendFailedRejectToFile("prescriptionId", "secret", runtimeConfig).get(2, TimeUnit.SECONDS);
        pharmacyService.appendFailedRejectToFile("prescriptionId2", "secret2", runtimeConfig).get(2, TimeUnit.SECONDS);

        List<String> lines = Files.readAllLines(FAILED_REJECTS_FILE);

        FailedRejectEntry read = PharmacyService.objectMapper.readValue(lines.get(0), FailedRejectEntry.class);
        assertEquals("prescriptionId", read.getPrescriptionId());
        assertEquals("secret", read.getSecret());
        assertEquals(runtimeConfig, read.getRuntimeConfig());
        assertEquals(runtimeConfig.getUserConfigurations(), read.getRuntimeConfig().getUserConfigurations());

        FailedRejectEntry read2 = PharmacyService.objectMapper.readValue(lines.get(1), FailedRejectEntry.class);
        assertEquals("prescriptionId2", read2.getPrescriptionId());
        assertEquals("secret2", read2.getSecret());
        assertEquals(runtimeConfig, read2.getRuntimeConfig());
        assertEquals(runtimeConfig.getUserConfigurations(), read2.getRuntimeConfig().getUserConfigurations());
    }

    @Test
    void retryFailedRejects() throws Exception {
        try (PharmacyService pharmacyService = getPharmacyServiceWithMocks()) {
            pharmacyService.failedRejectsFile = FAILED_REJECTS_FILE;

            pharmacyService.appendFailedRejectToFile("prescriptionId_success", "secret_success", new RuntimeConfig()).get(2, TimeUnit.SECONDS);

            assertEquals(1, Files.readAllLines(FAILED_REJECTS_FILE).size());

            pharmacyService.retryFailedRejects();

            assertEquals(0, Files.readAllLines(FAILED_REJECTS_FILE).size());
        }
    }

    @Test
    void testRetryFailedRejects() throws Exception {
        String successPrescriptionId = "prescriptionId_success";
        String successSecret = "secret_success";
        RuntimeConfig successConfig = new RuntimeConfig();
        FailedRejectEntry successEntry = new FailedRejectEntry(successPrescriptionId, successSecret, successConfig);
        String failedPrescriptionId = "prescriptionId_failed";
        String failedSecret = "secret_failed";
        RuntimeConfig failedConfig = new RuntimeConfig();
        FailedRejectEntry failedEntry = new FailedRejectEntry(failedPrescriptionId, failedSecret, failedConfig);

        try (PharmacyService pharmacyService = getPharmacyServiceWithMocks()) {
            List<String> failedRequests = pharmacyService.reprocessFailingEntries(Stream.of(successEntry, failedEntry));

            assertEquals(1, failedRequests.size());
            assertEquals("{\"prescriptionId\":\"prescriptionId_failed\",\"runtimeConfig\":{\"clientSystemId\":null,\"configurations\":{\"basicAuthPassword\":null,\"basicAuthUsername\":null,\"clientCertificate\":null,\"clientCertificatePassword\":null,\"clientSystemId\":null,\"connectorBaseURL\":null,\"erixaApiKey\":null,\"erixaDrugstoreEmail\":null,\"erixaHotfolder\":null,\"erixaUserEmail\":null,\"erixaUserPassword\":null,\"konnektorHost\":null,\"mandantId\":null,\"muster16TemplateProfile\":null,\"pruefnummer\":null,\"tvMode\":null,\"userId\":null,\"version\":null,\"workplaceId\":null},\"connectorAddress\":null,\"connectorBaseURL\":null,\"connectorVersion\":null,\"ehbahandle\":null,\"erixaApiKey\":null,\"erixaHotfolder\":null,\"erixaReceiverEmail\":null,\"erixaUserEmail\":null,\"erixaUserPassword\":null,\"idpAuthRequestRedirectURL\":null,\"idpBaseURL\":null,\"idpClientId\":null,\"konnektorHost\":null,\"mandantId\":null,\"muster16TemplateConfiguration\":\"DENS\",\"prescriptionServerURL\":null,\"pruefnummer\":null,\"sendPreview\":true,\"smcbhandle\":null,\"tvMode\":null,\"userId\":null,\"workplaceId\":null},\"secret\":\"secret_failed\"}", failedRequests.get(0));

            List<FailedRejectEntry> rejectEntries = failedRequests.stream().map(s -> {
                    try {
                        return objectMapper.readValue(s, FailedRejectEntry.class);
                    } catch (JsonProcessingException e) {
                        log.log(Level.SEVERE, "Failed to deserialize FailedRejectEntry: " + s, e);
                        return null;
                    }
                }).filter(Objects::nonNull)
                .toList();

            assertFalse(rejectEntries.isEmpty());
        }
    }

    @Test
    public void testExtractKvnr() {
        try (PharmacyService pharmacyService = new PharmacyService()) {
            ReadVSDResult readVSDResult = pharmacyService.new ReadVSDResult();
            readVSDResult.persoenlicheVersichertendaten = new Holder<byte[]>(DatatypeConverter.parseBase64Binary("H4sIAAAAAAAAAI1Sy27bMBD8FYH3iJITIXaxYhDERewijoO6dXMzGGltCSWXBUk5rb81H9NVHo4d9NALyRnOcpYcwsVva5It+tA6KkWeZiJBqlzd0qYU08X8ZDgsRid5IZIQNdXaOMJS/MEgLhR8v1rdcalDMm3V4LI/hmcfkZU83M9ukqvxbLX8/HUxnd+WokgHvQN7UihFE+OvT1I+hnSDVsf2Z1qjXGu5DbXtB7llvVBwcK4/QrSajtV9nmdnw2IwykF+3IPn9kjBNT50PgbuqrMqH50Ps7PBOcgjGpbOk7aovrhGEyWXZu0oJIvWOkom2Nbo2eFVA7e6ap5XT/5BE8g9Zq9QNQarJqpZb7FHXGORwq4LOu7UtdfrvuyAgm9tRKPuvFunydinIF8IWESvQ0C6rD3yzPdyIRps4043Rg2y06IAecTB3Ec10ZbvtwHZA7jh/BT8cA0FFhnNIaHnqFGNQf6LBvlS8uquJs4EfthHbQzINxImugvUWcvZnGYgD+Be9N63fAtEHocq/+Mrqb+FySMvrQIAAA=="));
            readVSDResult.pruefungsnachweis = new Holder<byte[]>(DatatypeConverter.parseBase64Binary("H4sIAAAAAAAA/7MJ8FNwdvGND3MNCvb097NVMtQz0DNQUqjIzckrtlXKKCkpsNLXLy/WS0/NTSzJzNZLSdVPS9QvK07J1S/IK9cvA6pXsrMJCbYzMjAyMTAysDA0NjAwMLTRBwrZuNoZ2+i72tnoB/jZAQBWMwfLagAAAA=="));

            assertEquals("X110485291", PharmacyService.extractKVNR(readVSDResult));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e);
        }
    }

    @Test
    public void testExtractHCV() {
        try (PharmacyService pharmacyService = new PharmacyService()) {
            ReadVSDResult readVSDResult = pharmacyService.new ReadVSDResult();
            readVSDResult.persoenlicheVersichertendaten = new Holder<byte[]>(DatatypeConverter.parseBase64Binary("H4sIAAAAAAAA/41S0U7rMAz9lSrvq9vCRIfcIMQGTGIMMe7gbQqtWSva5CpJx2Vfj7vBLgMeeEniY+f4OCd48q+pgxVZVxmdiTiMREA6N0Wll5kYz6a9NO0PenFfBM4rXajaaMrEKzlxIvHP2eKGrxrSdZWXNO9oeLeeuJKXh8lVcDacLOaj29l4ep2Jfph0Hbindpkovf97DPDiwiU1ylfPYUHwpGDliqZbYMX1QuInXrsX6cV4KM/TKI6iw+ToCOFrDjfytMQLemytd6yqbWQ8SOMoPYgQ9mCcG6tVQ/KOezPXe4TXKi83p4utSoQdwrwuL2vKSy8nHd0u6sic8usXY71cGb3h2wF4V3mq5dCGCNsjzrxVzpE+LSzxztKN8zVVfq3KWibJYX+AsIfhlJkuVcMjLBG6AK/YIon3ptSOi2rFPpBlN0kOEX6CEbZX3rvLc1tRYfkJe6NHfsUe4yzxI4uXqnW6bRr2IWY5n0I81Ty7rZ745ddtN6iMkzAYebUkhO/JHen/geHDLNg3HH7xzeQbugD9a8kCAAA="));
            readVSDResult.allgemeineVersicherungsdaten  = new Holder<byte[]>(DatatypeConverter.parseBase64Binary("H4sIAAAAAAAA/7VT30/CMBD+V5a+SzfMApquBsEYgoARReMLqezYFsbNtAWVv94rotnI/PHiS5u7r993392l4ux1lXsb0CYrMGJBw2ce4LyIM0wi1p+Mj9rt8OQoCJlnrMJY5QVCxN7AsDMp7rqzTp4nsIIMYepE5inoNSYmVhbwYXjldXvD2fTiZtIfjyIWNpquAJVEE7HU2udTzl9MgxSUzZaNGPhC8Y2JV+7gG3rPpPgStqBLkStj5unabqU4hyRDlE0/OPYDPxB8nxCDwpARqxUkjlwJl4BIIpIIYSsMgxPBa/EqK1eAMWgaEcjeAaOMiZFagbyFeYrZErQ30ApJcamMAcF3oOg8aYfvSH922gp9/z+cDj5d8R9s8cO4dh2Pa6PsNsNFYSrB5WBaWScqbWVQEtmnypzZ3g3pz3b8DwfEsMraXdcd0FubOwEnVOjEZWlGtXnxqFJU3zC+xcSEqtEXIRD0OR2EO0Jd2nWoMKdFW7iCzDiTgO51PfA51HJL9+Nr6TcFdzfZ+mEc/HC+vDJ9Xv09/PcvK98BTCKoUhQEAAA="));            

            assertEquals("66648b7131", PharmacyService.extractHCV(readVSDResult));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e);
        }
    }

    @Test
    public void testExtractClaculateHCV() {
        try (PharmacyService pharmacyService = new PharmacyService()) {
            assertEquals("10be65f365", PharmacyService.calculateHCV("2018-01-11T07:00:00", "Beispielstrasse"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e);
        }
    }

    private static PharmacyService getPharmacyServiceWithMocks() {
        Client client = mock(Client.class);
        WebTarget successTarget = mock(WebTarget.class);
        WebTarget failedTarget = mock(WebTarget.class);

        when(successTarget.path("/Task/prescriptionId_success/$reject")).thenReturn(successTarget);
        when(successTarget.queryParam("secret", "secret_success")).thenReturn(successTarget);
        Builder successBuilder = mock(Builder.class);
        when(successTarget.request()).thenReturn(successBuilder);
        when(successBuilder.header(nullable(String.class), nullable(String.class))).thenReturn(successBuilder);
        when(successBuilder.post(any())).thenReturn(Response.ok().build());

        when(successTarget.path("/Task/prescriptionId_failed/$reject")).thenReturn(failedTarget);
        Builder failedBuilder = mock(Builder.class);
        when(failedTarget.request()).thenReturn(failedBuilder);
        when(failedBuilder.header(nullable(String.class), nullable(String.class))).thenReturn(failedBuilder);
        when(failedBuilder.post(any())).thenReturn(Response.serverError().build());

        when(client.target(nullable(String.class))).thenReturn(successTarget);

        PharmacyService pharmacyService = new PharmacyService();
        pharmacyService.client = client;
        pharmacyService.appConfig = new AppConfig();
        pharmacyService.bearerTokenService = mock(BearerTokenService.class);
        pharmacyService.readEPrescriptionsMXBean = mock(ReadEPrescriptionsMXBeanImpl.class);
        when(pharmacyService.bearerTokenService.getBearerToken(any())).thenReturn("BearerToken");
        return pharmacyService;
    }
}
