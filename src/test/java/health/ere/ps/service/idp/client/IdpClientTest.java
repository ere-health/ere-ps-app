package health.ere.ps.service.idp.client;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.logging.LogManager;

import javax.inject.Inject;

import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.connector.certificate.CardCertReadExecutionService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.ssl.SSLUtilities;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class IdpClientTest {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Inject
    IdpClient idpClient;

    @Inject
    CardCertificateReaderService cardCertificateReaderService;

    @Inject
    CardCertReadExecutionService cardCertReadExecutionService;

    @ConfigProperty(name = "idp.client.id")
    String clientId;

    @ConfigProperty(name = "idp.connector.client.system.id")
    String clientSystem;

    @ConfigProperty(name = "idp.connector.workplace.id")
    String workplace;

    @ConfigProperty(name = "idp.connector.card.handle")
    String cardHandle;

    @ConfigProperty(name = "idp.base.url")
    String idpBaseUrl;

    String discoveryDocumentUrl;

    @ConfigProperty(name = "idp.auth.request.redirect.url")
    String redirectUrl;

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

    // @Disabled("Disabled until Titus Idp Card Certificate Service API Endpoint Is Fixed By Gematik")
    @Test
    public void test_Successful_Idp_Login_With_Gematik_Card()
            throws ConnectorCardCertificateReadException, IdpException,
            IdpClientException, IdpCryptoException, IdpJoseException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        cardCertificateReaderService.setMockCertificate(null);

        //InputStream p12Certificate = CardCertificateReaderService.class.getResourceAsStream("/ps_erp_incentergy_01.p12");
        // cardCertReadExecutionService.setUpCustomSSLContext(p12Certificate);

        discoveryDocumentUrl = idpBaseUrl + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;

        idpClient.init(clientId, redirectUrl, discoveryDocumentUrl, true);
        idpClient.initializeClient();

        PkiIdentity identity = cardCertificateReaderService.retrieveCardCertIdentity(clientId,
                clientSystem, workplace, cardHandle);

        IdpTokenResult idpTokenResult = idpClient.login(identity);
        System.out.println("Access Token: "+idpTokenResult.getAccessToken().getRawString());

        Assertions.assertNotNull(idpTokenResult, "Idp Token result present.");
        Assertions.assertNotNull(idpTokenResult.getAccessToken(), "Access Token present");
        Assertions.assertNotNull(idpTokenResult.getIdToken(), "Id Token present");
    }

    @Test/* @Disabled*/
    public void test_Successful_Idp_Login_RSA()
            throws ConnectorCardCertificateReadException, IdpException,
            IdpClientException, IdpCryptoException, IdpJoseException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

        String p12= "/certs/1-2-ARZT-WaltrautDrombusch01-80276001011699910223-C_SMCB_AUT_R2048_X509.p12";
        testP12(p12);
    }

    @Test/* @Disabled*/
    public void test_Successful_Idp_Login_ECC()
            throws ConnectorCardCertificateReadException, IdpException,
            IdpClientException, IdpCryptoException, IdpJoseException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

        String p12= "/certs/80276001011699910223-C_SMCB_AUT_E256_X509.p12";
        testP12(p12);
    }


    public void testP12(String p12) throws ConnectorCardCertificateReadException, IdpException,
    IdpClientException, IdpCryptoException, IdpJoseException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        InputStream inStream = CardCertificateReaderService.class.getResourceAsStream(p12);

        cardCertificateReaderService.setMockCertificate(inStream.readAllBytes());

        InputStream p12Certificate = CardCertificateReaderService.class.getResourceAsStream("/ps_erp_incentergy_01.p12");
        cardCertReadExecutionService.setUpCustomSSLContext(p12Certificate);

        discoveryDocumentUrl = idpBaseUrl + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;

        idpClient.init(clientId, redirectUrl, discoveryDocumentUrl, true);
        idpClient.initializeClient();

        PkiIdentity identity = cardCertificateReaderService.retrieveCardCertIdentity(clientId,
                clientSystem, workplace, cardHandle, "00");

        IdpTokenResult idpTokenResult = idpClient.login(identity);

        Assertions.assertNotNull(idpTokenResult, "Idp Token result present.");
        Assertions.assertNotNull(idpTokenResult.getAccessToken(), "Access Token present");
        Assertions.assertNotNull(idpTokenResult.getIdToken(), "Id Token present");
    }
}
