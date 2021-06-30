package health.ere.ps.service.connector.auth;

import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureService;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureServicePortType;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7.FaultMessage;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardService;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardServicePortType;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7.BinaryDocumentType;
import de.gematik.ws.conn.signatureservice.v7.ExternalAuthenticate;
import de.gematik.ws.conn.signatureservice.v7.ExternalAuthenticateResponse;

import java.io.FileNotFoundException;
import java.math.BigInteger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.connector.endpoint.EndpointDiscoveryService;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;

@ApplicationScoped
public class SmcbAuthenticatorExecutionService {

    @Inject
    SecretsManagerService secretsManagerService;

    @Inject
    AppConfig appConfig;

    @Inject
    EndpointDiscoveryService endpointDiscoveryService;

    private AuthSignatureServicePortType authSignatureService;

    CardServicePortType cardService;
    
    @PostConstruct
    void init() throws SecretsManagerException, FileNotFoundException {
        authSignatureService = new AuthSignatureService(getClass().getResource(
                "/AuthSignatureService_v7_4_1.wsdl")).getAuthSignatureServicePort();
        BindingProvider bp = (BindingProvider) authSignatureService;

        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                appConfig.getIdpConnectorAuthSignatureEndpointAddress());

        secretsManagerService.configureSSLTransportContext(appConfig.getIdpConnectorTlsCertTrustStore(),
                 appConfig.getIdpConnectorTlsCertTustStorePwd(), SecretsManagerService.SslContextType.TLS,
                 SecretsManagerService.KeyStoreType.PKCS12, bp);

        cardService = new CardService(getClass().getResource("/CardService.wsdl")).getCardServicePort();
        // Set endpoint to configured endpoint
        bp = (BindingProvider) cardService;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpointDiscoveryService.getCardServiceEndpointAddress());
        endpointDiscoveryService.configureSSLTransportContext(bp);
    }

    public ExternalAuthenticateResponse doExternalAuthenticate(String cardHandle, ContextType contextType,
                                       ExternalAuthenticate.OptionalInputs optionalInputs,
                                       BinaryDocumentType binaryDocumentType) throws FaultMessage {

        Holder<Status> statusHolder = new Holder<Status>();
        Holder<SignatureObject> signatureObjectHolder = new Holder<>();
        ExternalAuthenticateResponse response = new ExternalAuthenticateResponse();
        try {
                authSignatureService.externalAuthenticate(cardHandle, contextType, optionalInputs,
                        binaryDocumentType, statusHolder, signatureObjectHolder);
        } catch (FaultMessage faultMessage) {
                FaultMessage authSignatureFaultMessage = faultMessage;
                // Zugriffsbedingungen nicht erfüllt
                boolean code4085 = authSignatureFaultMessage.getFaultInfo().getTrace().stream().anyMatch(t -> t.getCode().equals(BigInteger.valueOf(4085l)));
                if(code4085) {

                        Holder<Status> status = new Holder<>();
                        Holder<PinResultEnum> pinResultEnum = new Holder<>();
                        Holder<BigInteger> error = new Holder<>();
                        try {
                            cardService.verifyPin(contextType,
                                    cardHandle,
                                    "PIN.SMC", status, pinResultEnum, error);
                                    authSignatureService.externalAuthenticate(cardHandle, contextType, optionalInputs,
                                    binaryDocumentType, statusHolder, signatureObjectHolder);
                        } catch (de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage e) {
                            throw new RuntimeException("Could not verify pin", faultMessage);
                        }
                } else {
                    throw new RuntimeException("Could not get external authenticate", faultMessage);
                }
        }
        response.setStatus(statusHolder.value);
        response.setSignatureObject(signatureObjectHolder.value);
        
        return response;
    }
}
