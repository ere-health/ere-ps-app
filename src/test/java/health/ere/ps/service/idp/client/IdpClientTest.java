package health.ere.ps.service.idp.client;

import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import jakarta.inject.Inject;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.profile.RUTestProfile;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.connector.endpoint.SSLUtilities;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Disabled;

@QuarkusTest
@TestProfile(RUTestProfile.class)
@Disabled
public class IdpClientTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final Logger log = Logger.getLogger(getClass().getName());

    @Inject
    AppConfig appConfig;
    @Inject
    IdpClient idpClient;
    @Inject
    CardCertificateReaderService cardCertificateReaderService;
    @Inject
    ConnectorCardsService connectorCardsService;

    String discoveryDocumentUrl;

    @BeforeAll
    public static void init() {

        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();

        /*try {
            // https://community.oracle.com/thread/1307033?start=0&tstart=0
            LogManager.getLogManager().readConfiguration(
                    IdpClientTest.class
                            .getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dumpTreshold", "999999");
    }

    @BeforeEach
    void configureSecureTransport() {
        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();
    }

    @Test
    public void test_Successful_Idp_Login_With_Connector_Smcb() throws IdpJoseException,
            IdpClientException, IdpException, ConnectorCardCertificateReadException, ConnectorCardsException {

        discoveryDocumentUrl = appConfig.getIdpBaseURL() + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;

        idpClient.init(appConfig.getIdpClientId(), appConfig.getIdpAuthRequestRedirectURL(), discoveryDocumentUrl, true);
        idpClient.initializeClient();

        String cardHandle = connectorCardsService.getConnectorCardHandle(
                ConnectorCardsService.CardHandleType.SMC_B);

        X509Certificate x509Certificate = cardCertificateReaderService.retrieveSmcbCardCertificate(cardHandle);

        IdpTokenResult idpTokenResult = idpClient.login(x509Certificate);

        log.info("Access Token: " + idpTokenResult.getAccessToken().getRawString());

        Assertions.assertNotNull(idpTokenResult, "Idp Token result present.");
        Assertions.assertNotNull(idpTokenResult.getAccessToken(), "Access Token present");
        Assertions.assertNotNull(idpTokenResult.getIdToken(), "Id Token present");
    }

    @Test
    @Tag("titus")
    public void test_With_RuntimeConfig() throws IdpJoseException,
            IdpClientException, IdpException, ConnectorCardCertificateReadException, ConnectorCardsException {

        discoveryDocumentUrl = appConfig.getIdpBaseURL() + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;

        idpClient.init(appConfig.getIdpClientId(), appConfig.getIdpAuthRequestRedirectURL(), discoveryDocumentUrl, true);
        idpClient.initializeClient();

        RuntimeConfig runtimeConfig = new RuntimeConfig();
        runtimeConfig.setEHBAHandle("1-1-ARZT-WaltrautFinkengrund01");
        runtimeConfig.setSMCBHandle("1-2-ARZT-WaltrautDrombusch01");
        runtimeConfig.getConfigurations().setConnectorBaseURL("https://kon-instanz2.titus.gematik.solutions");
        runtimeConfig.getConfigurations().setVersion("PTV4+");
        runtimeConfig.getConfigurations().setMandantId("ps_erp_incentergy_01");
        runtimeConfig.getConfigurations().setWorkplaceId("CATS");
        runtimeConfig.getConfigurations().setClientSystemId("ps_erp_incentergy_01_HBA");
        runtimeConfig.getConfigurations().setClientCertificate("data:application/x-pkcs12;base64,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");
        runtimeConfig.getConfigurations().setClientCertificatePassword("00");
        

        X509Certificate x509Certificate = cardCertificateReaderService.retrieveSmcbCardCertificate(runtimeConfig.getSMCBHandle());

        IdpTokenResult idpTokenResult = idpClient.login(x509Certificate, runtimeConfig);

        log.info("Access Token: " + idpTokenResult.getAccessToken().getRawString());

        Assertions.assertNotNull(idpTokenResult, "Idp Token result present.");
        Assertions.assertNotNull(idpTokenResult.getAccessToken(), "Access Token present");
        Assertions.assertNotNull(idpTokenResult.getIdToken(), "Id Token present");
    }
}
