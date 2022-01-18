package health.ere.ps.service.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.logging.LogManager;

import javax.inject.Inject;

import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import health.ere.ps.model.status.Status;
import health.ere.ps.profile.TitusTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
public class StatusServiceTest {

    @Inject
    StatusService statusService;

    @BeforeEach
    void init() {
        try {
            // https://community.oracle.com/thread/1307033?start=0&tstart=0
            LogManager.getLogManager().readConfiguration(
                StatusServiceTest.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dumpTreshold", "999999");
    }

    @Test
    public void test() {
        Status status = statusService.getStatus(null);

        assertTrue(status.getConnectorReachable());
        assertEquals("https://kon-instanz2.titus.ti-dienste.de", status.getConnectorInformation());
    
        assertTrue(status.getIdpReachable());
        assertEquals("https://idp.erezept-instanz1.titus.ti-dienste.de/auth/realms/idp/.well-known/openid-configuration", status.getIdpInformation());
    
        assertTrue(status.getIdpaccesstokenObtainable());

        JwtConsumer consumer = new JwtConsumerBuilder()
            .setDisableRequireSignature()
            .setSkipSignatureVerification()
            .setSkipDefaultAudienceValidation()
            .setRequireExpirationTime()
            .build();
        try {
            consumer.process(status.getBearerToken());
        } catch (InvalidJwtException e) {
            fail();
        }

        assertTrue(status.getSmcbAvailable());
        assertEquals("Card Handle: 1-2-ARZT-WaltrautDrombusch01", status.getSmcbInformation());

        assertTrue(status.getCautReadable());
        assertEquals("", status.getCautInformation());

        assertTrue(status.getEhbaAvailable());    
        assertEquals("Card Handle: 1-1-ARZT-WaltrautFinkengrund01", status.getEhbaInformation());

        assertTrue(status.getComfortsignatureAvailable());
        assertEquals("", status.getComfortsignatureInformation());
  
        assertTrue(status.getFachdienstReachable());
        assertEquals("", status.getFachdienstInformation());
    }
}
