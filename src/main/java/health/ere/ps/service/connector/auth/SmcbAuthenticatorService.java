package health.ere.ps.service.connector.auth;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.gematik.ws.conn.authsignatureservice.wsdl.v7.FaultMessage;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7.BinaryDocumentType;
import de.gematik.ws.conn.signatureservice.v7.ExternalAuthenticateResponse;

import org.apache.commons.lang3.tuple.Pair;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;

import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.common.security.SecretsManagerService;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;

@Dependent
public class SmcbAuthenticatorService {

    @Inject
    AppConfig appConfig;

    @Inject
    SecretsManagerService secretsManagerService;

    @Inject
    SmcbAuthenticatorExecutionService smcbAuthExecutionService;

    private PkiIdentity pkiIdentity;

    public String signIdpChallenge(Pair<String, String> jwtPair) {
        String signedChallenge;

        try {
            JwtClaims claims = JwtClaims.parse(
                    new String(Base64.getUrlDecoder().decode(jwtPair.getRight())));
            signedChallenge =
                    (String) claims.getClaimValue(ClaimName.NESTED_JWT.getJoseName());

        } catch (InvalidJwtException e) {
            throw new IllegalStateException("Error trying to extract Idp challenge value from " +
                    "JSON claims object.", e);
        }

        BinaryDocumentType binaryDocumentType = new BinaryDocumentType();
        Base64Data base64Data = new Base64Data();

        base64Data.setValue(signedChallenge.getBytes());

        binaryDocumentType.setBase64Data(base64Data);

        ContextType contextType = new ContextType();

        contextType.setClientSystemId(appConfig.getClientSystem());
        contextType.setMandantId(appConfig.getClientId());
        contextType.setWorkplaceId(appConfig.getWorkplace());

        ExternalAuthenticateResponse response;

        try {
            // TODO: The response is OK but useless. No signed data present.
            response = smcbAuthExecutionService.doExternalAuthenticate(
                    appConfig.getCardHandle(), contextType, null, binaryDocumentType);
        } catch (FaultMessage faultMessage) {
            throw new IllegalStateException("Error occured while trying to get signed Idp " +
                    "challenge response.", faultMessage);
        }

        JsonWebSignature jws = new JsonWebSignature();
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
            jws.setCertificateChainHeaderValue(getPkiIdentity().getCertificate());
            jws.setKey(getPkiIdentity().getPrivateKey());
            return jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new IllegalStateException("Error during encryption", e);
        }

    }

    public PkiIdentity getPkiIdentity() {
        return pkiIdentity;
    }

    public void setPkiIdentity(PkiIdentity pkiIdentity) {
        this.pkiIdentity = pkiIdentity;
    }
}
