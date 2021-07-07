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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.brainPoolExtension.BrainpoolCurves;

public class IdpJweTest {

    @Disabled
    @Test
    public void testCreateWithPayloadAndEncryptWithKey() throws IdpJoseException, IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        String payload = "{\"njwt\":\"eyJ0eXAiOiJKV1QiLCJjdHkiOiJOSldUIiwiYWxnIjoiQlAyNTZSMSIsIng1YyI6WyJNSUlEYmpDQ0F4U2dBd0lCQWdJSEF0NkZLTXN3TmpBS0JnZ3Foa2pPUFFRREFqQ0JtVEVMTUFrR0ExVUVCaE1DUkVVeEh6QWRCZ05WQkFvTUZtZGxiV0YwYVdzZ1IyMWlTQ0JPVDFRdFZrRk1TVVF4U0RCR0JnTlZCQXNNUDBsdWMzUnBkSFYwYVc5dUlHUmxjeUJIWlhOMWJtUm9aV2wwYzNkbGMyVnVjeTFEUVNCa1pYSWdWR1ZzWlcxaGRHbHJhVzVtY21GemRISjFhM1IxY2pFZk1CMEdBMVVFQXd3V1IwVk5MbE5OUTBJdFEwRTVJRlJGVTFRdFQwNU1XVEFlRncweU1ERXhNamN3TURBd01EQmFGdzB5TXpFeE1UWXdNREF3TURCYU1JR0VNUXN3Q1FZRFZRUUdFd0pFUlRFY01Cb0dBMVVFQ2d3VE1qQXlNVEV3TVRJeUlFNVBWQzFXUVV4SlJERVNNQkFHQTFVRUJBd0pSSEp2YldKMWMyTm9NUkV3RHdZRFZRUXFEQWhYWVd4MGNtRjFkREV3TUM0R0ExVUVBd3duUVhKNmRIQnlZWGhwY3lCWFlXeDBjbUYxZENCRWNtOXRZblZ6WTJnZ1ZFVlRWQzFQVGt4Wk1Gb3dGQVlIS29aSXpqMENBUVlKS3lRREF3SUlBUUVIQTBJQUJJcUVEM2Q5OGR3VHNYczFiTjFpNjZiQnJXK25RWDc1OTN2WG1ZNnlxbmsrVEVidzJDRmt4enpjM0o1NFEyZDUwUk12SmFGV3JxVE8xNUdTdCtDa0hWaWpnZ0ZYTUlJQlV6QTRCZ2dyQmdFRkJRY0JBUVFzTUNvd0tBWUlLd1lCQlFVSE1BR0dIR2gwZEhBNkx5OWxhR05oTG1kbGJXRjBhV3N1WkdVdmIyTnpjQzh3RXdZRFZSMGxCQXd3Q2dZSUt3WUJCUVVIQXdJd1ZBWUZLeVFJQXdNRVN6QkpNRWN3UlRCRE1FRXdGZ3dVUW1WMGNtbGxZbk56ZE1Pa2RIUmxJRUZ5ZW5Rd0NRWUhLb0lVQUV3RU1oTWNNUzB5TFVGU1dsUXRWMkZzZEhKaGRYUkVjbTl0WW5WelkyZ3dNVEFnQmdOVkhTQUVHVEFYTUFvR0NDcUNGQUJNQklFak1Ba0dCeXFDRkFCTUJFMHdId1lEVlIwakJCZ3dGb0FVWW9pYXhONzhvL09UT2N1ZmtPY1RtajJKekhVd0RBWURWUjBUQVFIL0JBSXdBREFPQmdOVkhROEJBZjhFQkFNQ0I0QXdIUVlEVlIwT0JCWUVGUG5rWFpDOFlSa2F4aFZMY2RYVzVaK3R1bUlMTUN3R0ExVWRId1FsTUNNd0lhQWZvQjJHRzJoMGRIQTZMeTlsYUdOaExtZGxiV0YwYVdzdVpHVXZZM0pzTHpBS0JnZ3Foa2pPUFFRREFnTklBREJGQWlBNndBTU11dDVnQzBRY2ZzRUtreFNUclN3Y0xYVVdObERzaGJ6MGdtODN1d0loQUliTmEvaElpS1htOElwTXpsNTJWaGJ4Z3NnMzBWU200U3E1ZTlpdTNnVXkiXX0.eyJuand0IjoiZXlKaGJHY2lPaUpDVURJMU5sSXhJaXdpZEhsd0lqb2lTbGRVSWl3aWEybGtJam9pY0hWclgybGtjRjl6YVdjaWZRLmV5SnBjM01pT2lKb2RIUndjem92TDJsa2NDNTZaVzUwY21Gc0xtbGtjQzV6Y0d4cGRHUnVjeTUwYVMxa2FXVnVjM1JsTG1SbElpd2ljbVZ6Y0c5dWMyVmZkSGx3WlNJNkltTnZaR1VpTENKemJtTWlPaUpaZVVkbVFsRk5WbmM1U0VKME9YRm9aSEpJTlhOMU9VUkViSGRLTkd0MlNVVmZORkZCWDI0eU4zaHZJaXdpWTI5a1pWOWphR0ZzYkdWdVoyVmZiV1YwYUc5a0lqb2lVekkxTmlJc0luUnZhMlZ1WDNSNWNHVWlPaUpqYUdGc2JHVnVaMlVpTENKdWIyNWpaU0k2SW10dlVYZGlZbWsxY205MlUxUjFRVGRQVDNOYUlpd2lZMnhwWlc1MFgybGtJam9pWjJWdFlYUnBhMVJsYzNSUWN5SXNJbk5qYjNCbElqb2laUzF5WlhwbGNIUWdiM0JsYm1sa0lpd2ljM1JoZEdVaU9pSk5PVzFPY1RJME5rZFZWSE5SVmtsUlF6QmlWQ0lzSW5KbFpHbHlaV04wWDNWeWFTSTZJbWgwZEhBNkx5OTBaWE4wTFhCekxtZGxiV0YwYVdzdVpHVXZaWEpsZW1Wd2RDSXNJbVY0Y0NJNk1UWXlNekUxTmpFME1Dd2lhV0YwSWpveE5qSXpNVFUxT1RZd0xDSmpiMlJsWDJOb1lXeHNaVzVuWlNJNklucHNRMFIwVTFCMFExUnVRVmhHWTFWTlYzaGFjV3BQTjAxSlFrNUNZM3BXYldaVWNVTmFjMmhpUWxraUxDSnFkR2tpT2lJM1pUYzROV1psWVdGallXUXlPVE5oSW4wLmpxY3JrLVN6RzFqbUxpN0JCRUVhRDNVQmxERWl3TlpFMnFFOWd6X1pqcXRvYnl5ZldjUnhIS0NiWmF1TXZYZkw1Y0JGUFVJQk43alBhNmNISVJpeWxnIn0.KbRsP5OpvPqGlqUDjRKt0ON3DsYOBnz_1aB32eigWdKkD-knxqe16A41I3CQQoNP3s-zIq8mqXRYh9CmlA-ahw\"}";


        Map<String, Object> discoveryClaims = TokenClaimExtraction
                .extractClaimsFromJwtBody(new String(getClass().getResourceAsStream("/openid/openid-configuration").readAllBytes()));
        String  uri_puk_idp_enc_json = new String(getClass().getResourceAsStream("/openid/puk_idp_enc").readAllBytes());

        JsonWebToken jsonWebToken = new JsonWebToken(uri_puk_idp_enc_json);
        JsonObject keyObject;

        try (JsonReader jsonReader =
                     Json.createReader(new StringReader(jsonWebToken.getRawString()))) {
            keyObject = jsonReader.readObject();
        }

        final java.security.spec.ECPoint ecPoint = new java.security.spec.ECPoint(
                new BigInteger(1, Base64.getUrlDecoder().decode(keyObject.getString("x"))),
                new BigInteger(1, Base64.getUrlDecoder().decode(keyObject.getString("y"))));
        final ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, BrainpoolCurves.BP256);

        java.security.PublicKey idpEncPublicKey = KeyFactory.getInstance("EC").generatePublic(keySpec);

        String s = IdpJwe.createWithPayloadAndEncryptWithKey(payload, idpEncPublicKey, "NJWT").getRawString();

        System.out.println(s);
    }
}