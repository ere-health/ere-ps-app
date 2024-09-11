package health.ere.ps.service.connector.auth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.crypto.signers.StandardDSAEncoding;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.CompactSerializer;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.StringUtil;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.gematik.ws.conn.authsignatureservice.wsdl.v7.FaultMessage;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7.BinaryDocumentType;
import de.gematik.ws.conn.signatureservice.v7.ExternalAuthenticate;
import de.gematik.ws.conn.signatureservice.v7.ExternalAuthenticateResponse;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.xml.ws.Holder;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;

@Dependent
public class SmcbAuthenticatorService {

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;
    
    @Inject
    ConnectorCardsService connectorCardsService;

    @Inject
    AppConfig appConfig;

    private X509Certificate x509Certificate;

    public String signIdpChallenge(Pair<String, String> jwtPair, RuntimeConfig runtimeConfig) {
        JsonWebSignatureWithExternalAuthentication jws = new JsonWebSignatureWithExternalAuthentication(runtimeConfig);
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
            jws.setCertificateChainHeaderValue(x509Certificate);
            return jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new IllegalStateException("Error during encryption", e);
        }
    }

    public void setX509Certificate(X509Certificate x509Certificate) {
        this.x509Certificate = x509Certificate;
    }

    /**
     * This extension for the jose4j JsonWebSignature signs the payload
     * with the function ExternalAuthenticate from the AuthSignatureServicePortType.
     *
     * @see <a href="https://github.com/gematik/api-telematik/blob/bb3ac703c2df619b54b2fbf4ab91337a66b395b4/conn/AuthSignatureService.wsdl#L44">...</a>
     */
    private class JsonWebSignatureWithExternalAuthentication extends JsonWebSignature {

        RuntimeConfig runtimeConfig;

        public JsonWebSignatureWithExternalAuthentication(RuntimeConfig runtimeConfig) {
            this.runtimeConfig = runtimeConfig;
        }

        /**
         * Compute the JWS signature.
         *
         * @throws JoseException if an error condition is encountered during the signing process
         */
        @Override
        public void sign() throws JoseException {
            if (getKey() != null) {
                super.sign();
            } else {
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
                    String smcbCardHandle = (this.runtimeConfig != null && this.runtimeConfig.getSMCBHandle() != null) ?
                        this.runtimeConfig.getSMCBHandle() : connectorCardsService.getConnectorCardHandle(
                        ConnectorCardsService.CardHandleType.SMC_B, runtimeConfig);
                    boolean isECC = this.getAlgorithmHeaderValue().equalsIgnoreCase("BP256R1");

                    signatureBytes = externalAuthenticateInternal(encodedhash,
                            smcbCardHandle, isECC);
                } catch (ConnectorCardsException e) {
                    throw new IllegalStateException("Cannot access the SMC-B card-handle info to " +
                            "compute the json web token signature!", e);
                }
                setSignature(signatureBytes);
            }
        }
    
        public byte[] externalAuthenticateInternal(byte[] sha265Hash, String smcbCardHandle, boolean isECC) throws JoseException {
            return externalAuthenticate(sha265Hash, smcbCardHandle, this.runtimeConfig, isECC);
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

    public byte[] externalAuthenticate(byte[] sha265Hash, String smcbCardHandle, RuntimeConfig runtimeConfig, boolean isECC) throws JoseException {
        ExternalAuthenticate.OptionalInputs optionalInputs = new ExternalAuthenticate.OptionalInputs();

        // optionalInputs.setSignatureSchemes("RSASSA-PSS");
        if(isECC) {
            optionalInputs.setSignatureType("urn:bsi:tr:03111:ecdsa");
        } else {
            optionalInputs.setSignatureType("urn:ietf:rfc:3447");
        }

        BinaryDocumentType binaryDocumentType = new BinaryDocumentType();
        Base64Data base64Data = new Base64Data();
        base64Data.setMimeType("application/octet-stream");
        base64Data.setValue(sha265Hash);
        binaryDocumentType.setBase64Data(base64Data);

        ExternalAuthenticateResponse response;

        try {
            // Titus Bug:  Client received SOAP Fault from server: No enum constant
            // de.gematik.ti.signenc.authsignature.SignatureScheme.RSASSA-PSS Please see the
            // server log to find more detail regarding exact cause of the failure.
            response = doExternalAuthenticate(smcbCardHandle,
                    runtimeConfig, optionalInputs,
                    binaryDocumentType);
        } catch (FaultMessage e) {
            throw new JoseException("Could not call externalAuthenticate", e);
        }
        byte[] value = response.getSignatureObject().getBase64Signature().getValue();

        if(isECC) {
            byte[] concatenated = convertDerECDSAtoConcated(value);
            return concatenated;
        } else {
            return value;
        }
    }

    public static byte[] convertDerECDSAtoConcated(byte[] derSignature) throws JoseException {
        try {
            BigInteger[] signInt = StandardDSAEncoding.INSTANCE.decode(ECNamedCurveTable.getParameterSpec("brainpoolp256r1").getN(), derSignature);
            ByteBuffer buffer = ByteBuffer.allocate(64);
            byte[] rArray = signInt[0].toByteArray();
            if(rArray.length == 32) {
                buffer.put(rArray);
            } else {
                buffer.put(Arrays.copyOfRange(rArray, 1, 33));
            }
            byte[] sArray = signInt[1].toByteArray();
            if(sArray.length == 32) {
                buffer.put(sArray);
            } else {
                buffer.put(Arrays.copyOfRange(sArray, 1, 33));
            }
            return buffer.array();
        } catch (Exception e) {
            throw new JoseException("Error converting DER to concatenated signature: " + e.getMessage(), e);
        }
    }


    public ExternalAuthenticateResponse doExternalAuthenticate(String cardHandle, RuntimeConfig runtimeConfig,
                                                               ExternalAuthenticate.OptionalInputs optionalInputs,
                                                               BinaryDocumentType binaryDocumentType) throws FaultMessage {

        ContextType contextType = connectorServicesProvider.getContextType(runtimeConfig);
        Holder<Status> statusHolder = new Holder<>();
        Holder<SignatureObject> signatureObjectHolder = new Holder<>();
        ExternalAuthenticateResponse response = new ExternalAuthenticateResponse();

        try {
            connectorServicesProvider.getAuthSignatureServicePortType(runtimeConfig).externalAuthenticate(cardHandle, contextType, optionalInputs,
                    binaryDocumentType, statusHolder, signatureObjectHolder);
        } catch (FaultMessage faultMessage) {
            // Zugriffsbedingungen nicht erfüllt
            boolean code4085 = faultMessage.getFaultInfo().getTrace().stream().anyMatch(t ->
                    t.getCode().equals(BigInteger.valueOf(4085L)));

            if (code4085 && appConfig.triggerSmcbPinVerification()) {
                Holder<Status> status = new Holder<>();
                Holder<PinResultEnum> pinResultEnum = new Holder<>();
                Holder<BigInteger> error = new Holder<>();
                try {
                    connectorServicesProvider.getCardServicePortType(runtimeConfig).verifyPin(contextType, cardHandle, "PIN.SMC", status, pinResultEnum, error);
                    connectorServicesProvider.getAuthSignatureServicePortType(runtimeConfig).externalAuthenticate(cardHandle, contextType, optionalInputs,
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
