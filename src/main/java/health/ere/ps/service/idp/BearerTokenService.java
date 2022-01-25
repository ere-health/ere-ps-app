package health.ere.ps.service.idp;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.websocket.Session;

import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.idp.client.IdpClient;
import health.ere.ps.service.idp.client.IdpHttpClientService;
import health.ere.ps.websocket.ExceptionWithReplyToExcetion;
import io.quarkus.runtime.Startup;

@ApplicationScoped
@Startup
public class BearerTokenService {
    private static final Logger log = Logger.getLogger(BearerTokenService.class.getName());

    @Inject
    AppConfig appConfig;
    @Inject
    Provider<IdpClient> providerIdpClient;
    @Inject
    CardCertificateReaderService cardCertificateReaderService;
    @Inject
    ConnectorCardsService connectorCardsService;
    @Inject
    Event<Exception> exceptionEvent;

    Map<RuntimeConfig, IdpClient> idpClientForRuntimeConfig = new HashMap<>();

    
    public IdpClient getIdpClient(RuntimeConfig runtimeConfig) {

    	if(idpClientForRuntimeConfig.containsKey(runtimeConfig)) {
    		return idpClientForRuntimeConfig.get(runtimeConfig);
    	} else {
	        String idpBaseUrl = appConfig.getIdpBaseURL();
	        String idpAuthRequestRedirectURL = appConfig.getIdpAuthRequestRedirectURL();
	        String idpClientId = appConfig.getIdpClientId();
	        
	        boolean verifyHostname = true;
	        boolean replaceUrlsInDiscoveryDocument = false;
	
	        if(runtimeConfig != null && runtimeConfig.getIdpBaseURL() != null) {
	            idpBaseUrl = runtimeConfig.getIdpBaseURL();
	            log.fine("Setting idp base url to: "+runtimeConfig.getIdpBaseURL());
	            verifyHostname = false;
	            replaceUrlsInDiscoveryDocument = true;
	        }
	
	        if(runtimeConfig != null && runtimeConfig.getIdpAuthRequestRedirectURL() != null) {
	            idpAuthRequestRedirectURL = runtimeConfig.getIdpAuthRequestRedirectURL();
	            log.fine("Setting idp auth request redirect url to: "+runtimeConfig.getIdpAuthRequestRedirectURL());
	        }
	
	        if(runtimeConfig != null && runtimeConfig.getIdpClientId() != null) {
	            idpClientId = runtimeConfig.getIdpClientId();
	            log.fine("Setting idp client id to: "+runtimeConfig.getIdpClientId());
	        }
	
	        String discoveryDocumentUrl = idpBaseUrl + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;
	        try {
	        	IdpClient idpClient = providerIdpClient.get();
	            idpClient.init(idpClientId, idpAuthRequestRedirectURL, discoveryDocumentUrl, true, verifyHostname);
	            idpClient.initializeClient(verifyHostname, replaceUrlsInDiscoveryDocument);
	            idpClientForRuntimeConfig.put(runtimeConfig, idpClient);
	            return idpClient;
	        } catch (Exception e) {
	            log.log(Level.WARNING, "Idp init did not work", e);
	            return null;
	        }
    	}
    }

    public String requestBearerToken() {
        return requestBearerToken(null);
    }

    public String requestBearerToken(RuntimeConfig runtimeConfig) {
        return requestBearerToken(runtimeConfig, null, null);
    }

    public String requestBearerToken(RuntimeConfig runtimeConfig, Session replyTo, String replyToMessageId) {
        IdpClient idpClient = getIdpClient(runtimeConfig);
        if(idpClient == null) {
        	throw new RuntimeException("Could not retrieve idpClient.");
        }
        try {
            String cardHandle = (runtimeConfig!= null && runtimeConfig.getSMCBHandle() != null) ?  runtimeConfig.getSMCBHandle(): connectorCardsService.getConnectorCardHandle(
                    ConnectorCardsService.CardHandleType.SMC_B, runtimeConfig);

            X509Certificate x509Certificate =
                    cardCertificateReaderService.retrieveSmcbCardCertificate(cardHandle, runtimeConfig);
            IdpTokenResult idpTokenResult = idpClient.login(x509Certificate, runtimeConfig);

            return idpTokenResult.getAccessToken().getRawString();
        } catch (Exception e) {
            log.log(Level.WARNING, "Idp login did not work, couldn't request bearer token", e);
            exceptionEvent.fireAsync(new ExceptionWithReplyToExcetion(e, replyTo, replyToMessageId));
            throw new RuntimeException(e);
        }
    }
}
