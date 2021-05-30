package health.ere.ps.service.idp.client;

import com.diffplug.common.base.Errors;
import com.diffplug.common.base.Throwing;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jose4j.jwt.JwtClaims;

import java.io.StringReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.ws.rs.core.Response;

import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.AuthenticationRequest;
import health.ere.ps.model.idp.client.AuthenticationResponse;
import health.ere.ps.model.idp.client.AuthorizationRequest;
import health.ere.ps.model.idp.client.AuthorizationResponse;
import health.ere.ps.model.idp.client.DiscoveryDocumentResponse;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.client.TokenRequest;
import health.ere.ps.model.idp.client.authentication.AuthenticationChallenge;
import health.ere.ps.model.idp.client.brainPoolExtension.BrainpoolCurves;
import health.ere.ps.model.idp.client.data.UserConsent;
import health.ere.ps.model.idp.client.field.IdpScope;
import health.ere.ps.model.idp.client.token.IdpJwe;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import health.ere.ps.model.idp.client.token.TokenClaimExtraction;
import health.ere.ps.service.idp.client.authentication.UriUtils;

import static health.ere.ps.model.idp.client.field.ClaimName.CODE_VERIFIER;
import static health.ere.ps.model.idp.client.field.ClaimName.TOKEN_KEY;
import static health.ere.ps.model.idp.client.field.ClaimName.X509_CERTIFICATE_CHAIN;
import static health.ere.ps.service.idp.client.authentication.UriUtils.extractParameterValue;
import static health.ere.ps.service.idp.crypto.CryptoLoader.getCertificateFromPem;

@ApplicationScoped
public class AuthenticatorClient {

    public AuthenticatorClient() {

    }

    public AuthorizationResponse doAuthorizationRequest(
            AuthorizationRequest authorizationRequest)
            throws IdpClientException, IdpException {
        final String scope = authorizationRequest.getScopes().stream()
                .map(IdpScope::getJwtValue)
                .collect(Collectors.joining(" "));

        IdpHttpClientService idpHttpClientService =
                getIdpHttpClientInstanceByUrl(authorizationRequest.getLink());

        JsonObject jsonObject;

        try(Response response = idpHttpClientService.doAuthorizationRequest(scope, "code",
                authorizationRequest.getRedirectUri(), authorizationRequest.getState(),
                "S256", authorizationRequest.getNonce(),
                authorizationRequest.getClientId(), authorizationRequest.getCodeChallenge())) {

            checkResponseForErrorsAndThrowIfAny(response);

            jsonObject = getJsonObject(response);
        }

        return new AuthorizationResponse.AuthorizationResponseBuilder().authenticationChallenge(
                new AuthenticationChallenge(
                        new JsonWebToken(jsonObject.getString("challenge")),
                        new UserConsent(toMap(jsonObject.getJsonObject("user_consent").getJsonObject(
                                    "requested_scopes")),
                                toMap(jsonObject.getJsonObject("user_consent").getJsonObject(
                                        "requested_claims"))))).build();
    }

    protected Map<String, String> toMap(JsonObject jsonObject) {
        Map<String, String> map = new HashMap<>(1);

        if (jsonObject != null) {
            java.util.Set<String> keySet = jsonObject.keySet();

            if (keySet != null && keySet.size() > 0) {
                keySet.forEach(key -> map.put(key, jsonObject.getString(key)));
            }
        }

        return map;
    }

    public health.ere.ps.model.idp.client.AuthenticationResponse performAuthentication(
            final AuthenticationRequest authenticationRequest) throws IdpClientException, IdpException {

        IdpHttpClientService idpHttpClientService =
                getIdpHttpClientInstanceByUrl(authenticationRequest.getAuthenticationEndpointUrl());

        String location;

        try(Response response =
                idpHttpClientService.doAuthenticationRequest(
                    authenticationRequest.getSignedChallenge().getRawString())) {

            checkResponseForErrorsAndThrowIfAny(response);

            location = retrieveLocationFromResponse(response);
        }

        return AuthenticationResponse.builder()
                .code(extractParameterValue(location, "code"))
                .location(location)
                /*.ssoToken(extractParameterValue(location, "ssotoken"))*/
                .build();
    }

    private void checkResponseForErrorsAndThrowIfAny(final Response loginResponse)
            throws IdpException, IdpClientException {
        if (loginResponse.getStatus() == 302) {
            checkForForwardingExceptionAndThrowIfPresent((String) loginResponse.getHeaders().getFirst(
                    "Location"));
        }
        if (loginResponse.getStatus() / 100 == 4) {
            throw new IdpClientException(
                    "Unexpected Server-Response: " + loginResponse.getStatus() + " " +
                            loginResponse.readEntity(String.class));
        }
    }

    private void checkForForwardingExceptionAndThrowIfPresent(final String location)
            throws IdpException {
        UriUtils.extractParameterValueOptional(location, "error")
                .ifPresent(Errors.rethrow().wrap((Throwing.Consumer<String>) errorCode -> {
                    throw new IdpClientException("Server-Error with message: " +
                            UriUtils.extractParameterValueOptional(location, "gematik_code")
                                    .map(code -> code + ": ")
                                    .orElse("") +
                            UriUtils.extractParameterValueOptional(location, "error_description")
                                    .orElse(errorCode));
                }));
    }

    public AuthenticationResponse performAuthenticationWithSsoToken(
            final AuthenticationRequest authenticationRequest)
            throws IdpException, IdpClientException {
        IdpHttpClientService idpHttpClientService =
                getIdpHttpClientInstanceByUrl(authenticationRequest.getAuthenticationEndpointUrl());

        String location;

        try(Response response =
                idpHttpClientService.doAuthenticationRequestWithSsoToken(
                        authenticationRequest.getSsoToken(),
                        authenticationRequest.getChallengeToken().getRawString())) {

            checkResponseForErrorsAndThrowIfAny(response);

            location = retrieveLocationFromResponse(response);
        }

        return AuthenticationResponse.builder()
                .code(extractParameterValue(location, "code"))
                .location(location)
                .build();
    }

    private String retrieveLocationFromResponse(final Response response)
            throws IdpClientException {
        if (response.getStatus() != 302) {
            throw new IdpClientException("Unexpected status code in response: " + response.getStatus());
        }
        return (String) response.getHeaders().getFirst("Location");
    }

    public IdpTokenResult retrieveAccessToken(
            final TokenRequest tokenRequest) throws IdpClientException, IdpException {
        final byte[] tokenKeyBytes = RandomStringUtils.randomAlphanumeric(256 / 8).getBytes();
        final SecretKey tokenKey = new SecretKeySpec(tokenKeyBytes, "AES");
        final IdpJwe keyVerifierToken = buildKeyVerifierToken(tokenKeyBytes, tokenRequest.getCodeVerifier(),
                tokenRequest.getIdpEnc());

        IdpHttpClientService idpHttpClientService =
                getIdpHttpClientInstanceByUrl(tokenRequest.getTokenUrl());

        JsonObject jsonObject;

        try(Response response = idpHttpClientService.doAccessTokenRequest("authorization_code",
                tokenRequest.getClientId(), tokenRequest.getCode(),
                keyVerifierToken.getRawString(), tokenRequest.getRedirectUrl())) {

            checkResponseForErrorsAndThrowIfAny(response);

            jsonObject = getJsonObject(response);
        }

        final String tokenType = jsonObject.getString("token_type");
        final int expiresIn = jsonObject.getInt("expires_in");

        return IdpTokenResult.builder()
                .tokenType(tokenType)
                .expiresIn(expiresIn)
                .accessToken(decryptToken(tokenKey, jsonObject.get("access_token")))
                .idToken(decryptToken(tokenKey, jsonObject.get("id_token")))
                .ssoToken(new IdpJwe(tokenRequest.getSsoToken()))
                .build();
    }

    private JsonWebToken decryptToken(final SecretKey tokenKey, final Object tokenValue)
            throws IdpClientException {
        String tokenValueClean = tokenValue.toString();
        if(tokenValue instanceof JsonString) {
            tokenValueClean = ((JsonString) tokenValue).getString();
        } else {
            tokenValueClean = tokenValue.toString();
        }
        return Optional.ofNullable(tokenValueClean)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(IdpJwe::new)
                .map(jwe -> jwe.decryptNestedJwt(tokenKey))
                .orElseThrow(() -> new IdpClientException("Unable to extract Access-Token from response!"));
    }

    private IdpJwe buildKeyVerifierToken(final byte[] tokenKeyBytes, final String codeVerifier,
                                         final PublicKey idpEnc) {
        final JwtClaims claims = new JwtClaims();
        claims.setStringClaim(TOKEN_KEY.getJoseName(),
                new String(Base64.getUrlEncoder().withoutPadding().encode(tokenKeyBytes)));
        claims.setStringClaim(CODE_VERIFIER.getJoseName(), codeVerifier);

        return IdpJwe.createWithPayloadAndEncryptWithKey(claims.toJson(), idpEnc, "JSON");
    }

    public DiscoveryDocumentResponse retrieveDiscoveryDocument(final String discoveryDocumentUrl)
            throws IdpClientException, IdpException, IdpJoseException {
        IdpHttpClientService idpHttpClientService = getIdpHttpClientInstanceByUrl(discoveryDocumentUrl);

        Map<String, Object> discoveryClaims;

        try(Response response = idpHttpClientService.doGenericGetRequest()) {

            checkResponseForErrorsAndThrowIfAny(response);

            discoveryClaims = TokenClaimExtraction
                    .extractClaimsFromJwtBody(response.readEntity(String.class));
        }

        return DiscoveryDocumentResponse.builder()
                .authorizationEndpoint(discoveryClaims.get("authorization_endpoint").toString())
                .tokenEndpoint(discoveryClaims.get("token_endpoint").toString())
                .idpSig(retrieveServerCertFromLocation(discoveryClaims.get("uri_puk_idp_sig").toString()))
                .idpEnc(retrieveServerPuKFromLocation(discoveryClaims.get("uri_puk_idp_enc").toString()))
                .build();
    }

    protected X509Certificate retrieveServerCertFromLocation(final String url)
            throws IdpException, IdpClientException {
        //TODO: Add connection retry strategy for failed connection attempts. E.g. exponential
        // backoff for retries.
        IdpHttpClientService idpHttpClientService = getIdpHttpClientInstanceByUrl(url);

        String jsonString;

        try(Response response = idpHttpClientService.doGenericGetRequest()) {

            checkResponseForErrorsAndThrowIfAny(response);

            jsonString = response.readEntity(String.class);
        }

        JsonWebToken jsonWebToken = new JsonWebToken(jsonString);
        String verificationCertificate;

        try (JsonReader jsonReader =
                     Json.createReader(new StringReader(jsonWebToken.getRawString()))) {
            verificationCertificate = jsonReader.readObject().getJsonArray(
                    X509_CERTIFICATE_CHAIN.getJoseName()).getString(0);
        }

        return getCertificateFromPem(Base64.getDecoder().decode(verificationCertificate));
    }

    protected PublicKey retrieveServerPuKFromLocation(final String url)
            throws IdpClientException, IdpException {

        IdpHttpClientService idpHttpClientService = getIdpHttpClientInstanceByUrl(url);

        String jsonString;

        try(Response response = idpHttpClientService.doGenericGetRequest()) {

            checkResponseForErrorsAndThrowIfAny(response);

            jsonString = response.readEntity(String.class);
        }

        JsonWebToken jsonWebToken = new JsonWebToken(jsonString);
        JsonObject keyObject;

        try (JsonReader jsonReader =
                     Json.createReader(new StringReader(jsonWebToken.getRawString()))) {
            keyObject = jsonReader.readObject();
        }

        final java.security.spec.ECPoint ecPoint = new java.security.spec.ECPoint(
                new BigInteger(Base64.getUrlDecoder().decode(keyObject.getString("x"))),
                new BigInteger(Base64.getUrlDecoder().decode(keyObject.getString("y"))));
        final ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, BrainpoolCurves.BP256);

        try {
            return KeyFactory.getInstance("EC").generatePublic(keySpec);
        } catch (final InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IdpClientException(
                    "Unable to construct public key from given URL: " + url, e);
        }
    }

    public IdpHttpClientService getIdpHttpClientInstanceByUrl(String url)
            throws IdpClientException {
        IdpHttpClientService idpHttpClientService;

        try {
            idpHttpClientService = RestClientBuilder.newBuilder()
                    .baseUrl(new URL(url))
                    .build(IdpHttpClientService.class);
        } catch (MalformedURLException e) {
            throw new IdpClientException("Bad URL: " + url, e);
        }

        return idpHttpClientService;
    }

    public JsonObject getJsonObject(Response response) {
        String jsonString = response.readEntity(String.class);
        JsonObject jsonObject = JsonObject.EMPTY_JSON_OBJECT;

        if(StringUtils.isNotBlank(jsonString)) {
            try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
                jsonObject = jsonReader.readObject();
            }
        }

        return jsonObject;
    }
}
