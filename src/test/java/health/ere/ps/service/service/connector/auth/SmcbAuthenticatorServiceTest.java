package health.ere.ps.service.connector.auth;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

import javax.xml.ws.Holder;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.hp.jipp.model.Status;
import com.ibm.icu.impl.Pair;

import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureServicePortType;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardServicePortType;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7.ExternalAuthenticateResponse;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;

public class SmcbAuthenticatorServiceTest {

    @Mock
    MultiConnectorServicesProvider connectorServicesProvider;

    @Mock
    ConnectorCardsService connectorCardsService;

    @InjectMocks
    SmcbAuthenticatorService smcbAuthenticatorService;

    @Test
    public void testSignIdpChallenge() throws JoseException {
        // Arrange
        Pair<String, String> jwtPair = Pair.of("base64Header", "base64Payload");
        RuntimeConfig runtimeConfig = new RuntimeConfig();

        // Mock X509Certificate
        X509Certificate mockCertificate = mock(X509Certificate.class);
        smcbAuthenticatorService.setX509Certificate(mockCertificate);

        // Act
        String compactSerialization = smcbAuthenticatorService.signIdpChallenge(jwtPair, runtimeConfig);

        // Assert
        assertNotNull(compactSerialization);
        assertTrue(compactSerialization.contains("eyJhbGciOiJBMjU2R0NNS1ciLCJle"));
    }

    @Test
    public void testExternalAuthenticate() throws JoseException {
        // Arrange
        byte[] sha256Hash = new byte[] { 0x01, 0x02, 0x03 };
        String smcbCardHandle = "SMCB_HANDLE";
        RuntimeConfig runtimeConfig = new RuntimeConfig();

        ExternalAuthenticateResponse mockResponse = new ExternalAuthenticateResponse();
        mockResponse.setSignatureObject(new SignatureObject());

        when(connectorServicesProvider.getContextType(runtimeConfig)).thenReturn(new ContextType());
        when(connectorServicesProvider.getAuthSignatureServicePortType(runtimeConfig)).thenReturn(mock(AuthSignatureServicePortType.class));
        when(connectorServicesProvider.getCardServicePortType(runtimeConfig)).thenReturn(mock(CardServicePortType.class));

        // Act
        byte[] signatureBytes = smcbAuthenticatorService.externalAuthenticate(sha256Hash, smcbCardHandle, runtimeConfig);

        // Assert
        assertNotNull(signatureBytes);
        assertTrue(signatureBytes.length > 0);
    }
}
