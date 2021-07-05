package health.ere.ps.service.idp.client;

import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.service.connector.endpoint.SSLUtilities;
import health.ere.ps.test.DefaultTestProfile;

import io.quarkus.test.junit.TestProfile;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.inject.Inject;

import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.common.security.SecureSoapTransportConfigurer;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestProfile(DefaultTestProfile.class)
public class IdpClientTest {

    @Inject
    AppConfig appConfig;

    @Inject
    IdpClient idpClient;

    @Inject
    CardCertificateReaderService cardCertificateReaderService;

    @Inject
    ConnectorCardsService connectorCardsService;

    @Inject
    SecureSoapTransportConfigurer secureSoapTransportConfigurer;

    String discoveryDocumentUrl;

    private final Logger log = Logger.getLogger(getClass().getName());

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @BeforeAll
    public static void init() {

        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();

        try {
			// https://community.oracle.com/thread/1307033?start=0&tstart=0
			LogManager.getLogManager().readConfiguration(
                IdpClientTest.class
							.getResourceAsStream("/logging.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dumpTreshold", "999999");
    }

    @BeforeEach
    void configureSecureTransport() throws SecretsManagerException {
        secureSoapTransportConfigurer.init(connectorCardsService);
        secureSoapTransportConfigurer.configureSecureTransport(
                appConfig.getEventServiceEndpointAddress(),
                SecretsManagerService.SslContextType.TLS,
                appConfig.getIdpConnectorTlsCertTrustStore(),
                appConfig.getIdpConnectorTlsCertTustStorePwd());

        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();
    }

    @Test
    public void test_Successful_Idp_Login_With_Connector_Smcb() throws IdpJoseException,
            IdpClientException, IdpException, ConnectorCardCertificateReadException,
            ConnectorCardsException {

        discoveryDocumentUrl = appConfig.getIdpBaseURL() + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;

        idpClient.init(appConfig.getClientId(), appConfig.getRedirectURL(), discoveryDocumentUrl, true);
        idpClient.initializeClient();

        String cardHandle = connectorCardsService.getConnectorCardHandle(
                ConnectorCardsService.CardHandleType.SMC_B);

        X509Certificate x509Certificate = cardCertificateReaderService.retrieveSmcbCardCertificate(
                appConfig.getMandantId(),
                appConfig.getClientSystem(),
                appConfig.getWorkplace(),
                cardHandle);

        IdpTokenResult idpTokenResult = idpClient.login(x509Certificate);

        log.info("Access Token: " + idpTokenResult.getAccessToken().getRawString());

        Assertions.assertNotNull(idpTokenResult, "Idp Token result present.");
        Assertions.assertNotNull(idpTokenResult.getAccessToken(), "Access Token present");
        Assertions.assertNotNull(idpTokenResult.getIdToken(), "Id Token present");
    }
}
