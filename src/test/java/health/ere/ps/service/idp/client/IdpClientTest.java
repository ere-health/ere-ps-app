package health.ere.ps.service.idp.client;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.LogManager;

import javax.inject.Inject;

import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.connector.auth.SmcbAuthenticatorService;
import health.ere.ps.service.connector.certificate.CardCertReadExecutionService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class IdpClientTest {

    @Inject
    IdpClient idpClient;

    @Inject
    SmcbAuthenticatorService smcbAuthenticatorService;

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

//    @ConfigProperty(name = "idp.connector.cert.auth.store.file.password")
//    String connectorCertAuthPassword;

    @ConfigProperty(name = "idp.base.url")
    String idpBaseUrl;

    String discoveryDocumentUrl;

    @ConfigProperty(name = "idp.auth.request.redirect.url")
    String redirectUrl;

    @BeforeAll
    public static void init() {

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

    @Test
    public void test_Successful_Idp_Login_With_Connector_Smcb() throws IdpJoseException,
            IdpClientException, IdpException, ConnectorCardCertificateReadException,
            SecretsManagerException, IdpCryptoException {
        //TODO: This is a very hacky test just to get some kind of response from Titus which is
        // still confusing.  Specifically, the AuthSignatureService.externalAuthenticate()
        // doesn't return anything useful - i.e. no signed data, but the response is successful.
        // This will not hold up in production. Need to get more details from Gematik about how
        // to handle this situation in a meaningful production ready manner.
        discoveryDocumentUrl = idpBaseUrl + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;

        idpClient.init(clientId, redirectUrl, discoveryDocumentUrl, true);
        idpClient.initializeClient();

        PkiIdentity identity = cardCertificateReaderService.retrieveCardCertIdentity(clientId,
                clientSystem, workplace, cardHandle);

        //TODO: Find better option to disable mock certificate.
        cardCertificateReaderService.setMockCertificate(null);

        X509Certificate x509Certificate = cardCertificateReaderService.retrieveCardCertificate(clientId,
                clientSystem, workplace, cardHandle);

        identity.setCertificate(x509Certificate);

        smcbAuthenticatorService.setPkiIdentity(identity);

        IdpTokenResult idpTokenResult = idpClient.login(x509Certificate,
                smcbAuthenticatorService::signIdpChallenge);

        Assertions.assertNotNull(idpTokenResult, "Idp Token result present.");
        Assertions.assertNotNull(idpTokenResult.getAccessToken(), "Access Token present");
        Assertions.assertNotNull(idpTokenResult.getIdToken(), "Id Token present");
    }

    @Disabled("Enable when connector.simulator.smcbIdentityCertificate app property is being used.")
    @Test
    public void test_Successful_Idp_Login_With_Gematik_Card()
            throws ConnectorCardCertificateReadException, IdpException,
            IdpClientException, IdpCryptoException, IdpJoseException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, SecretsManagerException {


        InputStream p12Certificate = CardCertificateReaderService.class.getResourceAsStream("/ps_erp_incentergy_01.p12");
        cardCertReadExecutionService.setUpCustomSSLContext(p12Certificate);

        discoveryDocumentUrl = idpBaseUrl + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;

        idpClient.init(clientId, redirectUrl, discoveryDocumentUrl, true);
        idpClient.initializeClient();

        PkiIdentity identity = cardCertificateReaderService.retrieveCardCertIdentity(clientId,
                clientSystem, workplace, cardHandle);

        IdpTokenResult idpTokenResult = idpClient.login(identity);

        Assertions.assertNotNull(idpTokenResult, "Idp Token result present.");
        Assertions.assertNotNull(idpTokenResult.getAccessToken(), "Access Token present");
        Assertions.assertNotNull(idpTokenResult.getIdToken(), "Id Token present");
    }

    @Test
    public void test_Successful_Idp_Login()
            throws ConnectorCardCertificateReadException, IdpException,
            IdpClientException, IdpCryptoException, IdpJoseException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, SecretsManagerException {

        InputStream inStream = CardCertificateReaderService.class.getResourceAsStream("/certs/1-2-ARZT-WaltrautDrombusch01-80276001011699910223-C_SMCB_AUT_R2048_X509.p12");

        /*KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(inStream, "00".toCharArray());  
        
        String alias = ks.aliases().nextElement();
        cardCertificateReaderService.setMockCertificate(((X509Certificate) ks.getCertificate(alias)).getEncoded());*/

        cardCertificateReaderService.setMockCertificate(inStream.readAllBytes());

        InputStream p12Certificate = CardCertificateReaderService.class.getResourceAsStream("/ps_erp_incentergy_01.p12");
        cardCertReadExecutionService.setUpCustomSSLContext(p12Certificate);
        AuthenticatorClient authenticatorClient = new AuthenticatorClient();

        discoveryDocumentUrl = idpBaseUrl + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;

        idpClient.init(clientId, redirectUrl, discoveryDocumentUrl, true);
        idpClient.initializeClient();

        PkiIdentity identity = cardCertificateReaderService.retrieveCardCertIdentity(clientId,
                clientSystem, workplace, cardHandle);

        IdpTokenResult idpTokenResult = idpClient.login(identity);

        Assertions.assertNotNull(idpTokenResult, "Idp Token result present.");
        Assertions.assertNotNull(idpTokenResult.getAccessToken(), "Access Token present");
        Assertions.assertNotNull(idpTokenResult.getIdToken(), "Id Token present");
    }
}
