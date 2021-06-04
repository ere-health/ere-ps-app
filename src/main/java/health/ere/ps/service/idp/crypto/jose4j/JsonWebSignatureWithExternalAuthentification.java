package health.ere.ps.service.idp.crypto.jose4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.ws.Holder;

import org.jose4j.jwa.CryptoPrimitive;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.CompactSerializer;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.StringUtil;

import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureServicePortType;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7.FaultMessage;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7.BinaryDocumentType;
import de.gematik.ws.conn.signatureservice.v7.ExternalAuthenticate;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;

/**
 * This extension for the jose4j JsonWebSignature signs the payload
 * with the function ExternalAuthenticate from the AuthSignatureServicePortType.
 * 
 * @see https://github.com/gematik/api-telematik/blob/bb3ac703c2df619b54b2fbf4ab91337a66b395b4/conn/AuthSignatureService.wsdl#L44
 */
public class JsonWebSignatureWithExternalAuthentification extends JsonWebSignature {
   
    String smcbCardHandle;
    ContextType contextType;

    public JsonWebSignatureWithExternalAuthentification(AuthSignatureServicePortType service, String smcbCardHandle, ContextType contextType) {
        this.service = service;
        this.smcbCardHandle = smcbCardHandle;
        this.contextType = contextType;
    }

    /**
     * Compute the JWS signature.
     * @throws JoseException if an error condition is encountered during the signing process
     */
    @Override
    public void sign() throws JoseException
    {
        byte[] inputBytes = getSigningInputBytes();
        byte[] signatureBytes = externalAuthenticate(inputBytes, smcbCardHandle);
        setSignature(signatureBytes);
    }

    public byte[] externalAuthenticate(byte[] sha265Hash, String smcbCardHandle) throws JoseException {

        ExternalAuthenticate.OptionalInputs optionalInputs = new ExternalAuthenticate.OptionalInputs();

        optionalInputs.setSignatureSchemes("RSASSA-PSS");
        optionalInputs.setSignatureType("urn:ietf:rfc:3447");

        BinaryDocumentType binaryDocumentType = new BinaryDocumentType();
        Base64Data base64Data = new Base64Data();
        base64Data.setMimeType("application/octet-stream");
        base64Data.setValue(sha265Hash);
        binaryDocumentType.setBase64Data(base64Data);

        Holder<SignatureObject> signatureObjectHolder = new Holder<>(); 
        Holder<Status> statusHolder = new Holder<>(); 

        try {
            service.externalAuthenticate(smcbCardHandle, contextType, optionalInputs, binaryDocumentType, statusHolder, signatureObjectHolder);
        } catch (FaultMessage e) {
            throw new JoseException("Could not call externalAuthenticate", e);
        }

        return signatureObjectHolder.value.getBase64Signature().getValue();

    }


    private byte[] getSigningInputBytes() throws JoseException
    {
        /*
           https://tools.ietf.org/html/rfc7797#section-3
           +-------+-----------------------------------------------------------+
           | "b64" | JWS Signing Input Formula                                 |
           +-------+-----------------------------------------------------------+
           | true  | ASCII(BASE64URL(UTF8(JWS Protected Header)) || '.' ||     |
           |       | BASE64URL(JWS Payload))                                   |
           |       |                                                           |
           | false | ASCII(BASE64URL(UTF8(JWS Protected Header)) || '.') ||    |
           |       | JWS Payload                                               |
           +-------+-----------------------------------------------------------+
        */

        if (!isRfc7797UnencodedPayload())
        {
            String signingInputString = CompactSerializer.serialize(getEncodedHeader(), getEncodedPayload());
            return StringUtil.getBytesAscii(signingInputString);
        }
        else
        {
            try
            {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                os.write(StringUtil.getBytesAscii(getEncodedHeader()));
                os.write(0x2e); // ascii for "."
                os.write(getUnverifiedPayloadBytes());
                return os.toByteArray();
            }
            catch (IOException e)
            {
                throw new JoseException("This should never happen from a ByteArrayOutputStream", e);
            }
        }
    }
}
