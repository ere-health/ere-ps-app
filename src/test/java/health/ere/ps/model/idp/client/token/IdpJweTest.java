package health.ere.ps.model.idp.client.token;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.jupiter.api.Test;

import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.brainPoolExtension.BrainpoolCurves;

public class IdpJweTest {
    @Test
    public void testCreateWithPayloadAndEncryptWithKey() throws IdpJoseException, IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        String payload = "Hallo Welt";
        

        Map<String, Object> discoveryClaims = TokenClaimExtraction
                .extractClaimsFromJwtBody(new String(getClass().getResourceAsStream("/openid/openid-configuration").readAllBytes()));
        String  uri_puk_idp_enc_json = new String(getClass().getResourceAsStream("/openid/uri_puk_idp_enc").readAllBytes());

        JsonWebToken jsonWebToken = new JsonWebToken(uri_puk_idp_enc_json);
        JsonObject keyObject;

        try (JsonReader jsonReader =
                     Json.createReader(new StringReader(jsonWebToken.getRawString()))) {
            keyObject = jsonReader.readObject();
        }

        final java.security.spec.ECPoint ecPoint = new java.security.spec.ECPoint(
                new BigInteger(Base64.getUrlDecoder().decode(keyObject.getString("x"))),
                new BigInteger(Base64.getUrlDecoder().decode(keyObject.getString("y"))));
        final ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, BrainpoolCurves.BP256);

        java.security.PublicKey idpEncPublicKey = KeyFactory.getInstance("EC").generatePublic(keySpec);

        IdpJwe.createWithPayloadAndEncryptWithKey(payload, idpEncPublicKey, "text/plain");
    }
}
