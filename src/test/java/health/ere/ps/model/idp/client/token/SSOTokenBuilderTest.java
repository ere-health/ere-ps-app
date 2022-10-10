package health.ere.ps.model.idp.client.token;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.logging.LogManager;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.connector.endpoint.SSLUtilities;

public class SSOTokenBuilderTest {

	
    @Inject
    ConnectorCardsService connectorCardsService;
    
    @Inject
    CardCertificateReaderService cardCertificateReaderService;
    
    @Inject
    SsoTokenBuilder builder;
    
    @Inject
    RuntimeConfig config;
	
	  
	  @Test
	  public void buildSSOTokenTest() throws ConnectorCardCertificateReadException, ConnectorCardsException, IdpJoseException, IdpCryptoException
	  {
		  
		  ConnectorCardsService csd = new ConnectorCardsService();
		
	  String cardHandle = csd.getConnectorCardHandle(
              ConnectorCardsService.CardHandleType.UNKNOWN, config);

      X509Certificate x509Certificate = cardCertificateReaderService.retrieveSmcbCardCertificate(cardHandle, null);
      
 
      
      ZonedDateTime givenTime = ZonedDateTime.now();
	IdpJwe idpJwe = builder.buildSsoToken(x509Certificate, givenTime);
      
      assertEquals(idpJwe.getAuthenticationCertificate(), x509Certificate);
      assertEquals(idpJwe.getExpiresAt(), givenTime.plusHours(12));
      assertEquals(idpJwe.getHeaderClaims().get(ClaimName.TYPE.getJoseName()), "JWT");
      
      
      
      
      
      
	  }
      
      
}
