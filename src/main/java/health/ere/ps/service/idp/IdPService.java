package health.ere.ps.service.idp;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import health.ere.ps.event.RequestBearerTokenFromIdpEvent;
import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.idp.client.IdpClient;
import health.ere.ps.service.idp.client.IdpHttpClientService;

@ApplicationScoped
public class IdPService {

    private static final Logger log = Logger.getLogger(IdPService.class.getName());
    
    @Inject
    IdpClient idpClient;

    @Inject
    CardCertificateReaderService cardCertificateReaderService;

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

    @ConfigProperty(name = "idp.auth.request.redirect.url")
    String redirectUrl;

    SSLContext customSSLContext = null;

    @Inject
    Event<Exception> exceptionEvent;
    
    public void requestBearerToken(@Observes RequestBearerTokenFromIdpEvent requestBearerTokenFromIdpEvent) {
        try {
            String discoveryDocumentUrl = idpBaseUrl + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;
            idpClient.init(clientId, redirectUrl, discoveryDocumentUrl, true);
            idpClient.initializeClient();

//            PkiIdentity identity = cardCertificateReaderService.retrieveCardCertIdentity(clientId,
//                    clientSystem, workplace, cardHandle, connectorCertAuthPassword);
            PkiIdentity identity = cardCertificateReaderService.retrieveCardCertIdentity(clientId,
                    clientSystem, workplace, cardHandle);

            IdpTokenResult idpTokenResult = idpClient.login(identity);
            requestBearerTokenFromIdpEvent.setBearerToken(idpTokenResult.getAccessToken().getRawString());
        } catch(IdpClientException | IdpException | IdpJoseException |
                ConnectorCardCertificateReadException |
                IdpCryptoException | SecretsManagerException e) {
            log.log(Level.WARNING, "Idp login did not work", e);
            exceptionEvent.fireAsync(e);
        }
    }
}
