package health.ere.ps.service.gematik;

import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.idp.BearerTokenService;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.xml.ws.Holder;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PharmacyServiceTest {

    @InjectMocks
    PharmacyService pharmacyService;

    @Mock
    AppConfig appConfig;

    @Mock
    MultiConnectorServicesProvider connectorServicesProvider;

    @Mock
    VSDServicePortType vsdService;

    @Mock
    Client mockClient;

    @Mock
    WebTarget mockTarget;

    @Mock
    Invocation.Builder mockBuilder;

    @Mock
    Response mockResponse;

    RuntimeConfig runtimeConfig;

    @Mock
    BearerTokenService mockBearerTokenService;

    @BeforeEach
    void init() {
        pharmacyService.client = mockClient;
        pharmacyService.bearerTokenService = mockBearerTokenService;

        when(appConfig.getPrescriptionServiceURL()).thenReturn("http://fake-url");
        when(appConfig.getUserAgent()).thenReturn("JUnit-Tester");

        when(connectorServicesProvider.getContextType(any())).thenReturn(new ContextType());
        when(connectorServicesProvider.getVSDServicePortType(any())).thenReturn(vsdService);

        runtimeConfig = new RuntimeConfig();
        pharmacyService.bearerToken = new HashMap<>();
        pharmacyService.bearerToken.put(runtimeConfig, "fake-token");
    }

    @Test
    @DisplayName("Should successfully return Bundle when VSD service provides proof and prescription service responds with 200 OK")
    void testGetEPrescriptions_success() throws Exception {
        setUpHttpMocks();
        byte[] pnwBytes = "proof".getBytes();
        String pnwBase64 = Base64.getEncoder().encodeToString(pnwBytes);

        doAnswer(invocation -> {
            Holder<byte[]> pruefungsnachweis = invocation.getArgument(9);
            pruefungsnachweis.value = pnwBytes;
            return null;
        }).when(vsdService).readVSD(any(), any(), anyBoolean(), anyBoolean(), any(), any(), any(), any(), any(), any());

        String bundleXml = "<Bundle xmlns=\"http://hl7.org/fhir\"></Bundle>";
        when(mockResponse.readEntity(String.class)).thenReturn(bundleXml);
        when(mockResponse.getStatus()).thenReturn(200);

        Bundle result = pharmacyService.getEPrescriptionsForCardHandle("egk", "smcb", runtimeConfig);

        assertNotNull(result, "Returned Bundle should not be null");
        verify(mockTarget).queryParam(eq("pnw"), eq(pnwBase64));
    }

    @Test
    @DisplayName("Should throw WebApplicationException when prescription service returns HTTP error")
    void testGetEPrescriptions_httpError() throws Exception {
        setUpHttpMocks();
        byte[] pnwBytes = "proof".getBytes();
        when(mockResponse.getStatus()).thenReturn(500);
        when(mockResponse.readEntity(String.class)).thenReturn("error");

        doAnswer(invocation -> {
            Holder<byte[]> pruefungsnachweis = invocation.getArgument(9);
            pruefungsnachweis.value = pnwBytes;
            return null;
        }).when(vsdService).readVSD(any(), any(), anyBoolean(), anyBoolean(), any(), any(), any(), any(), any(), any());

        assertThrows(WebApplicationException.class, () ->
                pharmacyService.getEPrescriptionsForCardHandle("egk", "smcb", new RuntimeConfig())
        );
    }

    private void setUpHttpMocks() {
        when(mockClient.target(anyString())).thenReturn(mockTarget);
        when(mockTarget.path(anyString())).thenReturn(mockTarget);
        when(mockTarget.queryParam(anyString(), any())).thenReturn(mockTarget);
        when(mockTarget.request()).thenReturn(mockBuilder);
        when(mockBuilder.header(any(), any())).thenReturn(mockBuilder);
        when(mockBuilder.get()).thenReturn(mockResponse);
    }
}
