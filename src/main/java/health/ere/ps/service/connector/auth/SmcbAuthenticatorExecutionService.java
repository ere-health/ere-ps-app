package health.ere.ps.service.connector.auth;

import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureServicePortType;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7.FaultMessage;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardServicePortType;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7.BinaryDocumentType;
import de.gematik.ws.conn.signatureservice.v7.ExternalAuthenticate;
import de.gematik.ws.conn.signatureservice.v7.ExternalAuthenticateResponse;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.ws.Holder;
import java.math.BigInteger;

@ApplicationScoped
public class SmcbAuthenticatorExecutionService {

    @Inject
    CardServicePortType cardService;
    @Inject
    AuthSignatureServicePortType authSignatureService;


    public ExternalAuthenticateResponse doExternalAuthenticate(String cardHandle, ContextType contextType,
                                                               ExternalAuthenticate.OptionalInputs optionalInputs,
                                                               BinaryDocumentType binaryDocumentType) throws FaultMessage {
        Holder<Status> statusHolder = new Holder<>();
        Holder<SignatureObject> signatureObjectHolder = new Holder<>();
        ExternalAuthenticateResponse response = new ExternalAuthenticateResponse();
        try {
            authSignatureService.externalAuthenticate(cardHandle, contextType, optionalInputs,
                    binaryDocumentType, statusHolder, signatureObjectHolder);
        } catch (FaultMessage faultMessage) {
            // Zugriffsbedingungen nicht erfüllt
            boolean code4085 = faultMessage.getFaultInfo().getTrace().stream().anyMatch(t ->
                    t.getCode().equals(BigInteger.valueOf(4085L)));

            if (code4085) {
                Holder<Status> status = new Holder<>();
                Holder<PinResultEnum> pinResultEnum = new Holder<>();
                Holder<BigInteger> error = new Holder<>();
                try {
                    cardService.verifyPin(contextType, cardHandle, "PIN.SMC", status, pinResultEnum, error);
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
