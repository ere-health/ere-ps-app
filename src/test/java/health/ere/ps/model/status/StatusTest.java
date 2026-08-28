package health.ere.ps.model.status;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusTest {

    @Test
    void shouldStoreAllStatusFlagsAndDescriptions() {
        Status status = new Status();

        status.setConnectorReachable(true, "connector ok");
        status.setIdpReachable(false, "idp down");
        status.setSmcbAvailable(true, "smcb present");
        status.setCautReadable(false, "caut unreadable");
        status.setEhbaAvailable(true, "ehba ok");
        status.setComfortsignatureAvailable(false, "comfort signature disabled");
        status.setIdpaccesstokenObtainable(true, "access token obtainable", "dummy-token");
        status.setFachdienstReachable(true, "fachdienst ok");

        assertTrue(status.getConnectorReachable());
        assertEquals("connector ok", status.getConnectorInformation());

        assertFalse(status.getIdpReachable());
        assertEquals("idp down", status.getIdpInformation());

        assertTrue(status.getSmcbAvailable());
        assertEquals("smcb present", status.getSmcbInformation());

        assertFalse(status.getCautReadable());
        assertEquals("caut unreadable", status.getCautInformation());

        assertTrue(status.getEhbaAvailable());
        assertEquals("ehba ok", status.getEhbaInformation());

        assertFalse(status.getComfortsignatureAvailable());
        assertEquals("comfort signature disabled", status.getComfortsignatureInformation());

        assertTrue(status.getIdpaccesstokenObtainable());
        assertEquals("access token obtainable", status.getIdpaccesstokenInformation());
        assertEquals("dummy-token", status.getBearerToken());

        assertTrue(status.getFachdienstReachable());
        assertEquals("fachdienst ok", status.getFachdienstInformation());
    }

    @Test
    void twoArgIdpAccessTokenSetterShouldNotSetBearerToken() {
        Status status = new Status();

        status.setIdpaccesstokenObtainable(true, "access token obtainable");

        assertTrue(status.getIdpaccesstokenObtainable());
        assertEquals("access token obtainable", status.getIdpaccesstokenInformation());
        assertNull(status.getBearerToken(), "Bearer token should remain null when using the two-arg setter");
    }
}
