package health.ere.ps.service.idp;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.idp.client.IdpClient;
import health.ere.ps.websocket.ExceptionWithReplyToException;
import jakarta.enterprise.event.Event;
import jakarta.websocket.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.cert.X509Certificate;

import static health.ere.ps.service.connector.cards.ConnectorCardsService.CardHandleType.SMC_B;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BearerTokenServiceTest {

    @Mock
    private IdpClient idpClient;

    @Mock
    private CardCertificateReaderService cardCertificateReaderService;

    @Mock
    private ConnectorCardsService connectorCardsService;

    @Mock
    private Event<Exception> exceptionEvent;

    @Mock
    private RuntimeConfig runtimeConfig;

    @Mock
    private Session session;

    @Mock
    private X509Certificate x509Certificate;

    @Mock
    private IdpTokenResult idpTokenResult;

    @Mock
    private JsonWebToken accessToken;

    @Captor
    private ArgumentCaptor<ExceptionWithReplyToException> exceptionCaptor;

    @InjectMocks
    private BearerTokenService bearerTokenService;

    private static final String CARD_HANDLE = "test-card-handle";
    private static final String ACCESS_TOKEN = "test-bearer-token-12345";
    private static final String REPLY_TO_MESSAGE_ID = "message-123";

    @Test
    void requestBearerToken_withNoParameters_shouldReturnAccessToken() throws Exception {
        // Arrange
        when(connectorCardsService.getConnectorCardHandle(eq(SMC_B), isNull())).thenReturn(CARD_HANDLE);
        when(cardCertificateReaderService.retrieveSmcbCardCertificate(eq(CARD_HANDLE), isNull())).thenReturn(x509Certificate);
        when(idpClient.login(eq(x509Certificate), isNull())).thenReturn(idpTokenResult);
        when(idpTokenResult.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getRawString()).thenReturn(ACCESS_TOKEN);

        // Act
        String result = bearerTokenService.requestBearerToken();

        // Assert
        assertEquals(ACCESS_TOKEN, result);
        verify(connectorCardsService).getConnectorCardHandle(SMC_B, null);
        verify(cardCertificateReaderService).retrieveSmcbCardCertificate(CARD_HANDLE, null);
        verify(idpClient).login(x509Certificate, null);
        verifyNoInteractions(exceptionEvent);
    }

    @Test
    void requestBearerToken_withRuntimeConfig_shouldReturnAccessToken() throws Exception {
        // Arrange
        when(connectorCardsService.getConnectorCardHandle(eq(SMC_B), eq(runtimeConfig))).thenReturn(CARD_HANDLE);
        when(cardCertificateReaderService.retrieveSmcbCardCertificate(eq(CARD_HANDLE), eq(runtimeConfig))).thenReturn(x509Certificate);
        when(idpClient.login(eq(x509Certificate), eq(runtimeConfig))).thenReturn(idpTokenResult);
        when(idpTokenResult.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getRawString()).thenReturn(ACCESS_TOKEN);

        // Act
        String result = bearerTokenService.requestBearerToken(runtimeConfig);

        // Assert
        assertEquals(ACCESS_TOKEN, result);
        verify(connectorCardsService).getConnectorCardHandle(SMC_B, runtimeConfig);
        verify(cardCertificateReaderService).retrieveSmcbCardCertificate(CARD_HANDLE, runtimeConfig);
        verify(idpClient).login(x509Certificate, runtimeConfig);
        verifyNoInteractions(exceptionEvent);
    }

    @Test
    void requestBearerToken_withValidSMCBHandle_shouldUseSMCBHandleFromConfig() throws Exception {
        // Arrange
        String configCardHandle = "config-card-handle";
        when(runtimeConfig.getSMCBHandle()).thenReturn(configCardHandle);
        when(cardCertificateReaderService.retrieveSmcbCardCertificate(eq(configCardHandle), eq(runtimeConfig))).thenReturn(x509Certificate);
        when(idpClient.login(eq(x509Certificate), eq(runtimeConfig))).thenReturn(idpTokenResult);
        when(idpTokenResult.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getRawString()).thenReturn(ACCESS_TOKEN);

        // Act
        String result = bearerTokenService.requestBearerToken(runtimeConfig, session, REPLY_TO_MESSAGE_ID);

        // Assert
        assertEquals(ACCESS_TOKEN, result);
        verify(runtimeConfig, times(2)).getSMCBHandle(); // Called twice: once for null check, once to get value
        verify(connectorCardsService, never()).getConnectorCardHandle(
                any(ConnectorCardsService.CardHandleType.class),
                any(RuntimeConfig.class)
        );
        verify(cardCertificateReaderService).retrieveSmcbCardCertificate(configCardHandle, runtimeConfig);
    }

    @Test
    void requestBearerToken_withNullSMCBHandle_shouldRetrieveCardHandleFromConnector() throws Exception {
        // Arrange
        when(runtimeConfig.getSMCBHandle()).thenReturn(null);
        when(connectorCardsService.getConnectorCardHandle(eq(SMC_B), eq(runtimeConfig))).thenReturn(CARD_HANDLE);
        when(cardCertificateReaderService.retrieveSmcbCardCertificate(eq(CARD_HANDLE), eq(runtimeConfig))).thenReturn(x509Certificate);
        when(idpClient.login(eq(x509Certificate), eq(runtimeConfig))).thenReturn(idpTokenResult);
        when(idpTokenResult.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getRawString()).thenReturn(ACCESS_TOKEN);

        // Act
        String result = bearerTokenService.requestBearerToken(runtimeConfig, session, REPLY_TO_MESSAGE_ID);

        // Assert
        assertEquals(ACCESS_TOKEN, result);
        verify(connectorCardsService).getConnectorCardHandle(SMC_B, runtimeConfig);
    }

    @Test
    void requestBearerToken_whenCardHandleRetrievalFails_shouldThrowRuntimeExceptionAndFireEvent() throws Exception {
        // Arrange
        Exception originalException = new RuntimeException("Card handle retrieval failed");
        when(connectorCardsService.getConnectorCardHandle(eq(SMC_B), eq(runtimeConfig))).thenThrow(originalException);

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                bearerTokenService.requestBearerToken(runtimeConfig, session, REPLY_TO_MESSAGE_ID)
        );

        assertEquals(originalException, thrown.getCause());
        verify(exceptionEvent).fireAsync(exceptionCaptor.capture());
        ExceptionWithReplyToException capturedException = exceptionCaptor.getValue();
        assertEquals(originalException, capturedException.getException());
        assertEquals(session, capturedException.getReplyTo());
        assertEquals(REPLY_TO_MESSAGE_ID, capturedException.getMessageId());
    }

    @Test
    void requestBearerToken_whenCertificateRetrievalFails_shouldThrowRuntimeExceptionAndFireEvent() throws Exception {
        // Arrange
        Exception originalException = new RuntimeException("Certificate retrieval failed");
        when(connectorCardsService.getConnectorCardHandle(eq(SMC_B), isNull())).thenReturn(CARD_HANDLE);
        when(cardCertificateReaderService.retrieveSmcbCardCertificate(eq(CARD_HANDLE), isNull())).thenThrow(originalException);

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                bearerTokenService.requestBearerToken()
        );

        assertEquals(originalException, thrown.getCause());
        verify(exceptionEvent).fireAsync(exceptionCaptor.capture());
        ExceptionWithReplyToException capturedException = exceptionCaptor.getValue();
        assertEquals(originalException, capturedException.getException());
        assertNull(capturedException.getReplyTo());
        assertNull(capturedException.getMessageId());
    }

    @Test
    void requestBearerToken_whenIdpLoginFails_shouldThrowRuntimeExceptionAndFireEvent() throws Exception {
        // Arrange
        Exception originalException = new RuntimeException("IDP login failed");
        when(connectorCardsService.getConnectorCardHandle(eq(SMC_B), isNull())).thenReturn(CARD_HANDLE);
        when(cardCertificateReaderService.retrieveSmcbCardCertificate(eq(CARD_HANDLE), isNull())).thenReturn(x509Certificate);
        when(idpClient.login(eq(x509Certificate), isNull())).thenThrow(originalException);

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                bearerTokenService.requestBearerToken()
        );

        assertEquals(originalException, thrown.getCause());
        verify(exceptionEvent).fireAsync(exceptionCaptor.capture());
        ExceptionWithReplyToException capturedException = exceptionCaptor.getValue();
        assertEquals(originalException, capturedException.getException());
        assertNull(capturedException.getReplyTo());
        assertNull(capturedException.getMessageId());
    }

    @Test
    void requestBearerToken_withReplyToAndMessageId_shouldPassThemToExceptionEvent() throws Exception {
        // Arrange
        Exception originalException = new RuntimeException("Test exception");
        when(connectorCardsService.getConnectorCardHandle(eq(SMC_B), eq(runtimeConfig))).thenThrow(originalException);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                bearerTokenService.requestBearerToken(runtimeConfig, session, REPLY_TO_MESSAGE_ID)
        );

        verify(exceptionEvent).fireAsync(exceptionCaptor.capture());
        ExceptionWithReplyToException capturedException = exceptionCaptor.getValue();
        assertEquals(session, capturedException.getReplyTo());
        assertEquals(REPLY_TO_MESSAGE_ID, capturedException.getMessageId());
    }
}
