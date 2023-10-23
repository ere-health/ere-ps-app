package health.ere.ps.service.idp.client.crypto;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.gematik.ws.conn.eventservice.v7.Event;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.idp.BearerTokenService;
import health.ere.ps.service.idp.client.IdpClient;
import health.ere.ps.service.idp.client.authentication.Before;

import javax.websocket.Session;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class BearerTokenServiceTest {

    @InjectMocks
    private BearerTokenService bearerTokenService;

    @Mock
    private AppConfig appConfig;
    
    @Mock
    private IdpClient idpClient;

    @Mock
    private CardCertificateReaderService cardCertificateReaderService;

    @Mock
    private ConnectorCardsService connectorCardsService;

    @Mock
    private X509Certificate x509Certificate;

    @Mock
    private IdpTokenResult idpTokenResult;

    @Mock
    private RuntimeConfig runtimeConfig;

    @Mock
    private Session session;

    @Test
    public void testRequestBearerTokenWithException() {
        try {
            when(idpClient.login(x509Certificate, runtimeConfig)).thenThrow(new RuntimeException("Test Exception"));
        } catch (IdpJoseException | IdpClientException | IdpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            bearerTokenService.requestBearerToken(runtimeConfig, session, "messageId");
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("Test Exception", e.getMessage());
        }

        Object exceptionEvent;
    }

    public BearerTokenService getBearerTokenService() {
        return bearerTokenService;
    }

    public void setBearerTokenService(BearerTokenService bearerTokenService) {
        this.bearerTokenService = bearerTokenService;
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public IdpClient getIdpClient() {
        return idpClient;
    }

    public void setIdpClient(IdpClient idpClient) {
        this.idpClient = idpClient;
    }

    public CardCertificateReaderService getCardCertificateReaderService() {
        return cardCertificateReaderService;
    }

    public void setCardCertificateReaderService(CardCertificateReaderService cardCertificateReaderService) {
        this.cardCertificateReaderService = cardCertificateReaderService;
    }

    public ConnectorCardsService getConnectorCardsService() {
        return connectorCardsService;
    }

    public void setConnectorCardsService(ConnectorCardsService connectorCardsService) {
        this.connectorCardsService = connectorCardsService;
    }

    public X509Certificate getX509Certificate() {
        return x509Certificate;
    }

    public void setX509Certificate(X509Certificate x509Certificate) {
        this.x509Certificate = x509Certificate;
    }

    public IdpTokenResult getIdpTokenResult() {
        return idpTokenResult;
    }

    public void setIdpTokenResult(IdpTokenResult idpTokenResult) {
        this.idpTokenResult = idpTokenResult;
    }

    public RuntimeConfig getRuntimeConfig() {
        return runtimeConfig;
    }

    public void setRuntimeConfig(RuntimeConfig runtimeConfig) {
        this.runtimeConfig = runtimeConfig;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getToken() {
        return getToken();
    }
}
