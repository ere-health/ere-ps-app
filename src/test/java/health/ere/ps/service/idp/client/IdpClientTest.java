package health.ere.ps.service.idp.client;

import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.profile.RUTestProfile;
import health.ere.ps.profile.TitusTestProfile;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

@QuarkusTest
@TestProfile(RUTestProfile.class)
public class IdpClientTest {

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
}
