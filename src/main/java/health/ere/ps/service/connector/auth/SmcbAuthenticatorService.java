package health.ere.ps.service.connector.auth;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.gematik.ws.conn.authsignatureservice.wsdl.v7.FaultMessage;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7.BinaryDocumentType;
import de.gematik.ws.conn.signatureservice.v7.ExternalAuthenticate;
import de.gematik.ws.conn.signatureservice.v7.ExternalAuthenticateResponse;

import org.apache.commons.lang3.tuple.Pair;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.CompactSerializer;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;

@Dependent
public class SmcbAuthenticatorService {

    @Inject
    AppConfig appConfig;

    @Inject
    ConnectorCardsService connectorCardsService;

    @Inject
    SmcbAuthenticatorExecutionService smcbAuthExecutionService;

    private X509Certificate x509Certificate;

    public String signIdpChallenge(Pair<String, String> jwtPair) {

        JsonWebSignatureWithExternalAuthentification jws =
                new JsonWebSignatureWithExternalAuthentification();
        jws.setPayload(new String(Base64.getUrlDecoder().decode(jwtPair.getRight())));

        Optional.ofNullable(jwtPair.getLeft())
                .map(b64Header -> new String(Base64.getUrlDecoder().decode(b64Header)))
                .map(JsonParser::parseString)
                .map(JsonElement::getAsJsonObject)
                .map(JsonObject::entrySet)
                .stream()
                .flatMap(Set::stream)
                .forEach(entry -> jws.setHeader(entry.getKey(),
                        entry.getValue().getAsString()));

        try {
            jws.setCertificateChainHeaderValue(getX509Certificate());
            return jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new IllegalStateException("Error during encryption", e);
        }
    }

    public X509Certificate getX509Certificate() {
        return x509Certificate;
    }

    public void setX509Certificate(X509Certificate x509Certificate) {
        this.x509Certificate = x509Certificate;
    }

    /**
     * This extension for the jose4j JsonWebSignature signs the payload
     * with the function ExternalAuthenticate from the AuthSignatureServicePortType.
     *
     * @see https://github.com/gematik/api-telematik/blob/bb3ac703c2df619b54b2fbf4ab91337a66b395b4/conn/AuthSignatureService.wsdl#L44
     */
    private class JsonWebSignatureWithExternalAuthentification extends JsonWebSignature {

        /**
         * Compute the JWS signature.
         *
         * @throws JoseException if an error condition is encountered during the signing process
         */
        @Override
        public void sign() throws JoseException {
            if(getKey() != null) {
                super.sign();
            }
            else {
                // otherwise use the connector for signing
                byte[] inputBytes = getSigningInputBytes();

                MessageDigest digest;
                try {
                    digest = MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                    throw new JoseException("Could not apply SHA-256 to signing bytes", e);
                }
                byte[] encodedhash = digest.digest(inputBytes);

                byte[] signatureBytes;

                try {
                    signatureBytes = externalAuthenticate(encodedhash,
                            connectorCardsService.getConnectorCardHandle(
                                    ConnectorCardsService.CardHandleType.SMC_B));
                } catch (ConnectorCardsException e) {
                    throw new IllegalStateException("Cannot access the SMC-B card-handle info to " +
                            "compute the json web token signature!", e);
                }
                setSignature(signatureBytes);
            }
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

            ContextType contextType = new ContextType();

            contextType.setClientSystemId(appConfig.getClientSystem());
            contextType.setMandantId(appConfig.getMandantId());
            contextType.setWorkplaceId(appConfig.getWorkplace());

            ExternalAuthenticateResponse response;

            try {
                // Titus Bug:  Client received SOAP Fault from server: No enum constant
                // de.gematik.ti.signenc.authsignature.SignatureScheme.RSASSA-PSS Please see the
                // server log to find more detail regarding exact cause of the failure.
                response = smcbAuthExecutionService.doExternalAuthenticate(smcbCardHandle,
                        contextType, optionalInputs,
                        binaryDocumentType);
            } catch (FaultMessage e) {
                throw new JoseException("Could not call externalAuthenticate", e);
            }

            return response.getSignatureObject().getBase64Signature().getValue();
        }


        private byte[] getSigningInputBytes() throws JoseException {
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

            if (!isRfc7797UnencodedPayload()) {
                String signingInputString = CompactSerializer.serialize(getEncodedHeader(),
                        getEncodedPayload());
                return StringUtil.getBytesAscii(signingInputString);
            } else {
                try {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    os.write(StringUtil.getBytesAscii(getEncodedHeader()));
                    os.write(0x2e); // ascii for "."
                    os.write(getUnverifiedPayloadBytes());
                    return os.toByteArray();
                } catch (IOException e) {
                    throw new JoseException("This should never happen from a ByteArrayOutputStream",
                            e);
                }
            }
        }
    }
}
