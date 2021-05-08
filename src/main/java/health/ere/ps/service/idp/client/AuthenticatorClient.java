package health.ere.ps.service.idp.client;

import health.ere.ps.model.idp.client.AuthenticationRequest;
import health.ere.ps.model.idp.client.AuthenticationResponse;
import health.ere.ps.model.idp.client.AuthorizationRequest;
import health.ere.ps.model.idp.client.AuthorizationResponse;
import health.ere.ps.model.idp.client.DiscoveryDocumentResponse;
import health.ere.ps.model.idp.client.IdpClientRuntimeException;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.client.TokenRequest;
import health.ere.ps.model.idp.client.authentication.AuthenticationChallenge;
import health.ere.ps.service.idp.client.authentication.UriUtils;
import health.ere.ps.model.idp.client.brainPoolExtension.BrainpoolCurves;

import health.ere.ps.model.idp.client.field.IdpScope;
import health.ere.ps.model.idp.client.token.IdpJwe;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import health.ere.ps.model.idp.client.token.TokenClaimExtraction;

import org.apache.commons.lang3.RandomStringUtils;
import org.jose4j.jwt.JwtClaims;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import kong.unirest.BodyPart;
import kong.unirest.GetRequest;
import kong.unirest.Header;
import kong.unirest.HttpRequest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.MultipartBody;
import kong.unirest.Unirest;
import kong.unirest.jackson.JacksonObjectMapper;
import kong.unirest.json.JSONObject;

import static health.ere.ps.service.idp.client.authentication.UriUtils.extractParameterValue;
import static health.ere.ps.service.idp.crypto.CryptoLoader.getCertificateFromPem;
import static health.ere.ps.model.idp.client.field.ClaimName.CLIENT_ID;
import static health.ere.ps.model.idp.client.field.ClaimName.CODE_CHALLENGE;
import static health.ere.ps.model.idp.client.field.ClaimName.CODE_CHALLENGE_METHOD;
import static health.ere.ps.model.idp.client.field.ClaimName.CODE_VERIFIER;
import static health.ere.ps.model.idp.client.field.ClaimName.REDIRECT_URI;
import static health.ere.ps.model.idp.client.field.ClaimName.RESPONSE_TYPE;
import static health.ere.ps.model.idp.client.field.ClaimName.SCOPE;
import static health.ere.ps.model.idp.client.field.ClaimName.STATE;
import static health.ere.ps.model.idp.client.field.ClaimName.TOKEN_KEY;
import static health.ere.ps.model.idp.client.field.ClaimName.X509_CERTIFICATE_CHAIN;

public class AuthenticatorClient {

    private static final String USER_AGENT = "IdP-Client";

    public AuthenticatorClient() {
        Unirest.config().reset();
        Unirest.config().followRedirects(false);
        Unirest.config().setObjectMapper(new JacksonObjectMapper());
    }

    public static Map<String, String> getAllHeaderElementsAsMap(final HttpRequest request) {
        return request.getHeaders().all().stream()
            .collect(Collectors.toMap(Header::getName, Header::getValue));
    }

    public static Map<String, Object> getAllFieldElementsAsMap(final MultipartBody request) {
        return request.multiParts().stream()
            .collect(Collectors.toMap(BodyPart::getName, BodyPart::getValue));
    }

    public AuthorizationResponse doAuthorizationRequest(
        final AuthorizationRequest authorizationRequest,
        final Function<GetRequest, GetRequest> beforeCallback,
        final Consumer<HttpResponse<AuthenticationChallenge>> afterCallback
    ) {
        final String scope = authorizationRequest.getScopes().stream()
            .map(IdpScope::getJwtValue)
            .collect(Collectors.joining(" "));

        final GetRequest request = Unirest.get(authorizationRequest.getLink())
            .queryString(CLIENT_ID.getJoseName(), authorizationRequest.getClientId())
            .queryString(RESPONSE_TYPE.getJoseName(), "code")
            .queryString(REDIRECT_URI.getJoseName(), authorizationRequest.getRedirectUri())
            .queryString(STATE.getJoseName(), authorizationRequest.getState())
            .queryString(CODE_CHALLENGE.getJoseName(), authorizationRequest.getCodeChallenge())
            .queryString(CODE_CHALLENGE_METHOD.getJoseName(),
                authorizationRequest.getCodeChallengeMethod())
            .queryString(SCOPE.getJoseName(), scope)
            .queryString("nonce", authorizationRequest.getNonce())
            .header(HttpHeaders.USER_AGENT, USER_AGENT)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);

        final HttpResponse<AuthenticationChallenge> authorizationResponse = beforeCallback
            .apply(request)
            .asObject(AuthenticationChallenge.class);
        afterCallback.accept(authorizationResponse);
        checkResponseForErrorsAndThrowIfAny(authorizationResponse);
        return AuthorizationResponse.builder()
            .authenticationChallenge(authorizationResponse.getBody())
            .build();
    }

    public health.ere.ps.model.idp.client.AuthenticationResponse performAuthentication(
        final AuthenticationRequest authenticationRequest,
        final Function<MultipartBody, MultipartBody> beforeAuthenticationCallback,
        final Consumer<HttpResponse<String>> afterAuthenticationCallback) {

        final MultipartBody request = Unirest
            .post(authenticationRequest.getAuthenticationEndpointUrl())
            .field("signed_challenge", authenticationRequest.getSignedChallenge().getRawString())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header(HttpHeaders.USER_AGENT, USER_AGENT);

        final HttpResponse<String> loginResponse = beforeAuthenticationCallback.apply(request).asString();
        afterAuthenticationCallback.accept(loginResponse);
        checkResponseForErrorsAndThrowIfAny(loginResponse);
        final String location = retrieveLocationFromResponse(loginResponse);

        return AuthenticationResponse.builder()
            .code(extractParameterValue(location, "code"))
            .location(location)
            .ssoToken(extractParameterValue(location, "ssotoken"))
            .build();
    }

    private void checkResponseForErrorsAndThrowIfAny(final HttpResponse<?> loginResponse) {
        if (loginResponse.getStatus() == 302) {
            checkForForwardingExceptionAndThrowIfPresent(loginResponse.getHeaders().getFirst("Location"));
        }
        if (loginResponse.getStatus() / 100 == 4) {
            throw new IdpClientRuntimeException(
                "Unexpected Server-Response " + loginResponse.getStatus() + " " + loginResponse.mapError(String.class));
        }
    }

    private void checkForForwardingExceptionAndThrowIfPresent(final String location) {
        UriUtils.extractParameterValueOptional(location, "error")
            .ifPresent(errorCode -> {
                throw new IdpClientRuntimeException("Server-Error with message: " +
                    UriUtils.extractParameterValueOptional(location, "gematik_code")
                        .map(code -> code + ": ")
                        .orElse("") +
                    UriUtils.extractParameterValueOptional(location, "error_description")
                        .orElse(errorCode));
            });
    }

    public AuthenticationResponse performAuthenticationWithSsoToken(
        final AuthenticationRequest authenticationRequest,
        final Function<MultipartBody, MultipartBody> beforeAuthenticationCallback,
        final Consumer<HttpResponse<String>> afterAuthenticationCallback) {
        final MultipartBody request = Unirest.post(authenticationRequest.getAuthenticationEndpointUrl())
            .field("ssotoken", authenticationRequest.getSsoToken())
            .field("unsigned_challenge", authenticationRequest.getChallengeToken().getRawString())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header(HttpHeaders.USER_AGENT, USER_AGENT)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        final HttpResponse<String> loginResponse = beforeAuthenticationCallback.apply(request).asString();
        afterAuthenticationCallback.accept(loginResponse);
        checkResponseForErrorsAndThrowIfAny(loginResponse);
        final String location = retrieveLocationFromResponse(loginResponse);
        return AuthenticationResponse.builder()
            .code(extractParameterValue(location, "code"))
            .location(location)
            .build();
    }

    private String retrieveLocationFromResponse(final HttpResponse<String> response) {
        if (response.getStatus() != 302) {
            throw new IdpClientRuntimeException("Unexpected status code in response: " + response.getStatus());
        }
        return response.getHeaders().getFirst("Location");
    }

    public IdpTokenResult retrieveAccessToken(
        final TokenRequest tokenRequest,
        final Function<MultipartBody, MultipartBody> beforeTokenCallback,
        final Consumer<HttpResponse<JsonNode>> afterTokenCallback) {
        final byte[] tokenKeyBytes = RandomStringUtils.randomAlphanumeric(256 / 8).getBytes();
        final SecretKey tokenKey = new SecretKeySpec(tokenKeyBytes, "AES");
        final IdpJwe keyVerifierToken = buildKeyVerifierToken(tokenKeyBytes, tokenRequest.getCodeVerifier(),
            tokenRequest.getIdpEnc());

        final MultipartBody request = Unirest.post(tokenRequest.getTokenUrl())
            .field("grant_type", "authorization_code")
            .field("client_id", tokenRequest.getClientId())
            .field("code", tokenRequest.getCode())
            .field("key_verifier", keyVerifierToken.getRawString())
            .field("redirect_uri", tokenRequest.getRedirectUrl())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header(HttpHeaders.USER_AGENT, USER_AGENT)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);

        final HttpResponse<JsonNode> tokenResponse = beforeTokenCallback.apply(request)
            .asJson();
        afterTokenCallback.accept(tokenResponse);
        checkResponseForErrorsAndThrowIfAny(tokenResponse);
        final JSONObject jsonObject = tokenResponse.getBody().getObject();

        final String tokenType = tokenResponse.getBody().getObject().getString("token_type");
        final int expiresIn = tokenResponse.getBody().getObject().getInt("expires_in");

        return IdpTokenResult.builder()
            .tokenType(tokenType)
            .expiresIn(expiresIn)
            .accessToken(decryptToken(tokenKey, jsonObject.get("access_token")))
            .idToken(decryptToken(tokenKey, jsonObject.get("id_token")))
            .ssoToken(new IdpJwe(tokenRequest.getSsoToken()))
            .build();
    }

    private JsonWebToken decryptToken(final SecretKey tokenKey, final Object tokenValue) {
        return Optional.ofNullable(tokenValue)
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .map(IdpJwe::new)
            .map(jwe -> jwe.decryptNestedJwt(tokenKey))
            .orElseThrow(() -> new IdpClientRuntimeException("Unable to extract Access-Token from response!"));
    }

    private IdpJwe buildKeyVerifierToken(final byte[] tokenKeyBytes, final String codeVerifier,
        final PublicKey idpEnc) {
        final JwtClaims claims = new JwtClaims();
        claims.setStringClaim(TOKEN_KEY.getJoseName(),
            new String(Base64.getUrlEncoder().withoutPadding().encode(tokenKeyBytes)));
        claims.setStringClaim(CODE_VERIFIER.getJoseName(), codeVerifier);

        return IdpJwe.createWithPayloadAndEncryptWithKey(claims.toJson(), idpEnc, "JSON");
    }

    public DiscoveryDocumentResponse retrieveDiscoveryDocument(final String discoveryDocumentUrl) {
        //TODO aufräumen, checks hinzufügen...
        final HttpResponse<String> discoveryDocumentResponse = Unirest.get(discoveryDocumentUrl)
            .header(HttpHeaders.USER_AGENT, USER_AGENT)
            .asString();
        final Map<String, Object> discoveryClaims = TokenClaimExtraction
            .extractClaimsFromJwtBody(discoveryDocumentResponse.getBody());

        final HttpResponse<JsonNode> pukAuthResponse = Unirest
            .get(discoveryClaims.get("uri_puk_idp_sig").toString())
            .header(HttpHeaders.USER_AGENT, USER_AGENT)
            .asJson();
        final JSONObject keyObject = pukAuthResponse.getBody().getObject();

        return DiscoveryDocumentResponse.builder()
            .authorizationEndpoint(discoveryClaims.get("authorization_endpoint").toString())
            .tokenEndpoint(discoveryClaims.get("token_endpoint").toString())

            .idpSig(retrieveServerCertFromLocation(discoveryClaims.get("uri_puk_idp_sig").toString()))
            .idpEnc(retrieveServerPuKFromLocation(discoveryClaims.get("uri_puk_idp_enc").toString()))

            .build();
    }

    protected X509Certificate retrieveServerCertFromLocation(final String uri) {
        final HttpResponse<JsonNode> pukAuthResponse = Unirest
            .get(uri)
            .header(HttpHeaders.USER_AGENT, USER_AGENT)
            .asJson();
        final JSONObject keyObject = pukAuthResponse.getBody().getObject();
        final String verificationCertificate = keyObject.getJSONArray(X509_CERTIFICATE_CHAIN.getJoseName())
            .getString(0);
        return getCertificateFromPem(Base64.getDecoder().decode(verificationCertificate));
    }

    private PublicKey retrieveServerPuKFromLocation(final String uri) {

        final HttpResponse<JsonNode> pukAuthResponse = Unirest
            .get(uri)
            .header(HttpHeaders.USER_AGENT, USER_AGENT)
            .asJson();
        final JSONObject keyObject = pukAuthResponse.getBody().getObject();
        final java.security.spec.ECPoint ecPoint = new java.security.spec.ECPoint(
            new BigInteger(Base64.getUrlDecoder().decode(keyObject.getString("x"))),
            new BigInteger(Base64.getUrlDecoder().decode(keyObject.getString("y"))));
        final ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, BrainpoolCurves.BP256);
        try {
            return KeyFactory.getInstance("EC").generatePublic(keySpec);
        } catch (final InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IdpClientRuntimeException(
                "Unable to construct public key from given uri '" + uri + "', got " + e.getMessage());
        }
    }
}
