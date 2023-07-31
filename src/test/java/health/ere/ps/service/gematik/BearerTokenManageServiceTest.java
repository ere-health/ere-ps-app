package health.ere.ps.service.gematik;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.idp.BearerTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.websocket.Session;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BearerTokenManageServiceTest {

    private static final String EXPIRED_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZXhwIjowLCJpYXQiOjE1MTYyMzkwMjJ9.5Hx8bZnrbl3HFG6XZJVrmeS9tVYs975XX-Z01nrlzgU";
    private static final String MOCK_JWT = "MockedJwt_NotValid";
    private BearerTokenManageService bearerTokenManageService;
    private RuntimeConfig mockRuntimeConfig;
    private Session mockSession;

    BearerTokenService mockBearerTokenService;

    @BeforeEach
    void init() {
        bearerTokenManageService = new BearerTokenManageService();
        mockRuntimeConfig = mock(RuntimeConfig.class);
        mockSession = mock(Session.class);
        mockBearerTokenService = mock(BearerTokenService.class);
        bearerTokenManageService.bearerTokenService = mockBearerTokenService;
        when(mockBearerTokenService.requestBearerToken(any(), any(), anyString())).thenReturn(MOCK_JWT);
    }

    @Test
    public void testRequestBearerTokenWithExistingButExpiredToken() {
        Map<RuntimeConfig, String> configMap = new HashMap<>();
        configMap.put(mockRuntimeConfig, EXPIRED_JWT);
        bearerTokenManageService.bearerToken = configMap;

        assertEquals(configMap.size(), 1);
        assertEquals(configMap.get(mockRuntimeConfig), EXPIRED_JWT);

        bearerTokenManageService.requestNewAccessTokenIfNecessary(mockRuntimeConfig, mockSession, "someId");

        assertEquals(configMap.size(), 1);
        assertEquals(configMap.get(mockRuntimeConfig), MOCK_JWT);
    }

    @Test
    public void testRequestBearerTokenWithoutExistingToken() {
        Map<RuntimeConfig, String> bearerTokenMap = bearerTokenManageService.bearerToken;

        assertEquals(bearerTokenMap.size(), 0);

        bearerTokenManageService.requestNewAccessTokenIfNecessary(mockRuntimeConfig, mockSession, "someId");

        assertEquals(bearerTokenMap.size(), 1);
        assertEquals(bearerTokenMap.get(mockRuntimeConfig), MOCK_JWT);
    }

    @Test
    public void testRequestBearerTokenWithValidToken() {
        String[] parts = EXPIRED_JWT.split("\\.");
        String decodedExpiredTokenPart = new String(Base64.getUrlDecoder().decode(parts[1]));
        String decodedValidTokenPart = decodedExpiredTokenPart.replaceFirst("\"exp\":0", ",\"exp\":" + Instant.now().getEpochSecond() + 60);
        parts[1] = Base64.getEncoder().encodeToString(decodedValidTokenPart.getBytes());
        String encodedValidToken = String.join(".", parts);

        Map<RuntimeConfig, String> bearerTokenMap = bearerTokenManageService.bearerToken;
        bearerTokenMap.put(mockRuntimeConfig, encodedValidToken);

        assertEquals(bearerTokenMap.size(), 1);
        assertEquals(bearerTokenMap.get(mockRuntimeConfig), encodedValidToken);

        bearerTokenManageService.requestNewAccessTokenIfNecessary(mockRuntimeConfig, mockSession, "someId");

        assertEquals(bearerTokenMap.size(), 1);
        assertEquals(bearerTokenMap.get(mockRuntimeConfig), encodedValidToken);
    }
}