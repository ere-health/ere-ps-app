package health.ere.ps.service.idp.client;

import com.diffplug.common.base.Errors;
import com.diffplug.common.base.Throwing;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureService;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureServicePortType;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.crypto.Data;
import javax.xml.ws.BindingProvider;

import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.AuthenticationRequest;
import health.ere.ps.model.idp.client.AuthenticationResponse;
import health.ere.ps.model.idp.client.AuthorizationRequest;
import health.ere.ps.model.idp.client.AuthorizationResponse;
import health.ere.ps.model.idp.client.DiscoveryDocumentResponse;
import health.ere.ps.model.idp.client.IdpConstants;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.client.ImpfnachweisAuthenticationRequest;
import health.ere.ps.model.idp.client.ImpfnachweisAuthorizationResponse;
import health.ere.ps.model.idp.client.TokenRequest;
import health.ere.ps.model.idp.client.TokenRequest.TokenRequestBuilder;
import health.ere.ps.model.idp.client.authentication.AuthenticationChallenge;
import health.ere.ps.model.idp.client.authentication.AuthenticationResponseBuilder;
import health.ere.ps.model.idp.client.brainPoolExtension.BrainpoolAlgorithmSuiteIdentifiers;
import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.model.idp.client.field.CodeChallengeMethod;
import health.ere.ps.model.idp.client.field.IdpScope;
import health.ere.ps.model.idp.client.token.IdpJwe;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.idp.client.authentication.UriUtils;
import health.ere.ps.service.idp.crypto.KeyAnalysis;
import health.ere.ps.service.idp.crypto.jose4j.JsonWebSignatureWithExternalAuthentification;

import static org.jose4j.jws.AlgorithmIdentifiers.RSA_PSS_USING_SHA256;

@Dependent
public class IdpClient implements IIdpClient {

    @Inject
    AuthenticatorClient authenticatorClient;

    @Inject
    Logger logger;

    @ConfigProperty(name = "connector.simulator.titusClientCertificate", defaultValue = "!")
    String titusClientCertificate;

    @ConfigProperty(name = "auth-signature-service.endpointAddress", defaultValue = "")
    String authSignatureServiceEndpointAddress;

    @ConfigProperty(name = "auth-signature-service.smbcCardHandle", defaultValue = "")
    String authSignatureServiceSmbcCardHandle;

    @ConfigProperty(name = "signature-service.context.mandantId", defaultValue = "")
    String signatureServiceContextMandantId;

    @ConfigProperty(name = "signature-service.context.clientSystemId", defaultValue = "")
    String signatureServiceContextClientSystemId;

    @ConfigProperty(name = "signature-service.context.workplaceId", defaultValue = "")
    String signatureServiceContextWorkplaceId;

    @ConfigProperty(name = "signature-service.context.userId", defaultValue = "")
    String signatureServiceContextUserId;

    SSLContext customSSLContext = null;

    private String clientId;
    private String redirectUrl;
    private String discoveryDocumentUrl;
    private boolean shouldVerifyState;
    private Set<IdpScope> scopes = Set.of(IdpScope.OPENID/*, IdpScope.EREZEPT*/);
    private CodeChallengeMethod codeChallengeMethod = CodeChallengeMethod.S256;

    private DiscoveryDocumentResponse discoveryDocumentResponse;

    AuthSignatureServicePortType authSignatureService;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @PostConstruct
    public void initAuthSignatureService() {
        try {
            if (titusClientCertificate != null && !("".equals(titusClientCertificate))
                    && !("!".equals(titusClientCertificate))) {
                try {
                    setUpCustomSSLContext(new FileInputStream(titusClientCertificate));
                } catch(FileNotFoundException e) {
                    logger.error("Could not find titus file", e);
                }
            }

            authSignatureService = new AuthSignatureService(getClass().getResource("/AuthSignatureService.wsdl")).getAuthSignatureServicePort();
            /* Set endpoint to configured endpoint */
            BindingProvider bp = (BindingProvider) authSignatureService;
            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, authSignatureServiceEndpointAddress);
            if (customSSLContext != null) {
                bp.getRequestContext().put("com.sun.xml.ws.transport.https.client.SSLSocketFactory",
                        customSSLContext.getSocketFactory());
            }       
        } catch(Exception ex) {
            logger.error("Could not init AuthSignatureService for IdpClient", ex);
        }
    }

    public void setUpCustomSSLContext(InputStream p12Certificate) {
        customSSLContext = SecretsManagerService.setUpCustomSSLContext(p12Certificate);
    }

    public void init(String clientId, String redirectUrl, String discoveryDocumentUrl,
                     boolean shouldVerifyState) {
        this.clientId = clientId;
        this.redirectUrl = redirectUrl;
        this.discoveryDocumentUrl = discoveryDocumentUrl;
        this.shouldVerifyState = shouldVerifyState;
    }

    public IdpClient() {
    }

    /**
     * Create a context type.
     */
    ContextType createContextType() {
        ContextType contextType = new ContextType();
        contextType.setMandantId(signatureServiceContextMandantId);
        contextType.setClientSystemId(signatureServiceContextClientSystemId);
        contextType.setWorkplaceId(signatureServiceContextWorkplaceId);
        contextType.setUserId(signatureServiceContextUserId);
        return contextType;
    }

    private String signServerChallenge(final String challengeToSign, final X509Certificate certificate,
                                       final Function<Pair<String, String>, String> contentSigner)
            throws IdpJoseException {
        final JwtClaims claims = new JwtClaims();
        claims.setClaim(ClaimName.NESTED_JWT.getJoseName(), challengeToSign);
        final JsonWebSignature jsonWebSignature = new JsonWebSignature();
        jsonWebSignature.setPayload(claims.toJson());
        jsonWebSignature.setHeader("typ", "JWT");
        jsonWebSignature.setHeader("cty", "NJWT");
        if (KeyAnalysis.isEcKey(certificate.getPublicKey())) {
            // ProviderContext providerCtx = new ProviderContext();
            // providerCtx.getGeneralProviderContext().setKeyPairGeneratorProvider("BC");
            // providerCtx.getGeneralProviderContext().setKeyAgreementProvider("BC");
            // providerCtx.getSuppliedKeyProviderContext().setKeyPairGeneratorProvider("BC");
            // providerCtx.getSuppliedKeyProviderContext().setKeyAgreementProvider("BC");
            // jsonWebSignature.setProviderContext(providerCtx);
            jsonWebSignature.setAlgorithmHeaderValue(
                    BrainpoolAlgorithmSuiteIdentifiers.BRAINPOOL256_USING_SHA256);
        } else {
            jsonWebSignature.setAlgorithmHeaderValue(RSA_PSS_USING_SHA256);
        }
        PublicKey idpPublicKey = getDiscoveryDocumentResponse().getIdpEnc();
        JsonWebToken jwt = new JsonWebToken(
            contentSigner.apply(Pair.of(
                jsonWebSignature.getHeaders().getEncodedHeader(),
                jsonWebSignature.getEncodedPayload())));

        jwt.getHeaderClaims().remove("alg");
        jwt.getHeaderClaims().put("alg", BrainpoolAlgorithmSuiteIdentifiers.BRAINPOOL256_USING_SHA256);
        
        String signedServerChallengeJwt = jwt
                .encrypt(idpPublicKey)
                .getRawString();

        return signedServerChallengeJwt;
    }

    public IdpTokenResult login(final PkiIdentity idpIdentity)
            throws IdpException, IdpClientException, IdpJoseException {
        assertThatIdpIdentityIsValid(idpIdentity);
        return login(idpIdentity.getCertificate(),
            Errors.rethrow().wrap((Throwing.Function<Pair<String, String>, String>) jwtPair -> {
                final JsonWebSignatureWithExternalAuthentification jws = new JsonWebSignatureWithExternalAuthentification(authSignatureService, authSignatureServiceSmbcCardHandle, createContextType());
                jws.setPayload(new String(Base64.getUrlDecoder().decode(jwtPair.getRight())));
                Optional.ofNullable(jwtPair.getLeft())
                    .map(b64Header -> new String(Base64.getUrlDecoder().decode(b64Header)))
                    .map(com.google.gson.JsonParser::parseString)
                    .map(com.google.gson.JsonElement::getAsJsonObject)
                    .map(com.google.gson.JsonObject::entrySet)
                    .stream()
                    .flatMap(Set::stream)
                    .forEach(entry -> jws.setHeader(entry.getKey(),
                        entry.getValue().getAsString()));
                        
                jws.setCertificateChainHeaderValue(idpIdentity.getCertificate());
                jws.setKey(idpIdentity.getPrivateKey());
                try {
                    return jws.getCompactSerialization();
                } catch (JoseException e) {
                    throw new IdpClientException("Error during encryption", e);
                }
            }));
    }

    public IdpTokenResult login(final X509Certificate certificate,
        final Function<Pair<String, String>, String> contentSigner)
            throws IdpClientException, IdpException, IdpJoseException {
        assertThatClientIsInitialized();

        final String codeVerifier = ClientUtilities.generateCodeVerifier();
        final String nonce = RandomStringUtils.randomAlphanumeric(20);

        // Authorization
        final String state = RandomStringUtils.randomAlphanumeric(20);
        logger.debug("Performing Authorization with remote-URL: " +
                getDiscoveryDocumentResponse().getAuthorizationEndpoint());
        final AuthorizationResponse authorizationResponse =
            getAuthenticatorClient()
                .doAuthorizationRequest(AuthorizationRequest.builder()
                        .clientId(getClientId())
                        .link(getDiscoveryDocumentResponse().getAuthorizationEndpoint())
                        .codeChallenge(ClientUtilities.generateCodeChallenge(codeVerifier))
                        .codeChallengeMethod(getCodeChallengeMethod())
                        .redirectUri(getRedirectUrl())
                        .state(state)
                        .scopes(getScopes())
                        .nonce(nonce)
                        .build());

        // Authentication
        logger.debug("Performing Authentication with remote-URL: " +
            getDiscoveryDocumentResponse().getAuthorizationEndpoint());
        AuthenticationResponse authenticationResponse;
        if(authorizationResponse instanceof ImpfnachweisAuthorizationResponse) {
            ImpfnachweisAuthorizationResponse impfnachweisAuthorizationResponse = (ImpfnachweisAuthorizationResponse) authorizationResponse;
            ImpfnachweisAuthenticationRequest authenticationRequest = new ImpfnachweisAuthenticationRequest();
            authenticationRequest.setAuthenticationEndpointUrl(impfnachweisAuthorizationResponse.getLocation());
            
            JsonWebSignatureWithExternalAuthentification jws = new JsonWebSignatureWithExternalAuthentification(authSignatureService, authSignatureServiceSmbcCardHandle, createContextType());
            byte[] signedChallenge;
            try {
                signedChallenge = jws.signBytes(impfnachweisAuthorizationResponse.getChallenge().getBytes());
            } catch (JoseException e) {
                throw new IdpJoseException(e);
            }
            Client client = ClientBuilder.newClient();
            Response response;
            try {
                response = client.target(impfnachweisAuthorizationResponse.getLocation()).request()
                    .header("x-auth-signed-challenge", new String(Base64.getEncoder().encode(signedChallenge)))
                    .header("x-auth-certificate", new String(Base64.getEncoder().encode(certificate.getEncoded())))
                    .get();
            } catch (CertificateEncodingException e1) {
                throw new IdpClientException(e1);
            }

            URL url;
            try {
                url = new URL(response.getHeaderString("Location").replace("connector://", "http://"));
            } catch (MalformedURLException e) {
                throw new IdpException(e);
            }
            List<Map.Entry<String, String>> list = Pattern.compile("&")
                .splitAsStream(url.getQuery())
                .map(s -> Arrays.copyOf(s.split("=", 2), 2))
                .map(o -> Map.entry(decode(o[0]), decode(o[1])))
                .collect(Collectors.toList());
            String code = null;
            String sessionState = null;
            for(Map.Entry<String, String> m : list) {
                if("code".equals(m.getKey())) {
                    code = m.getValue();
                } else if("session_state".equals(m.getKey())) {
                    sessionState = m.getValue();
                }
            }

            String tokenUrl = getDiscoveryDocumentResponse().getTokenEndpoint();

            Form form = new Form();
            form.param("grant_type", "authorization_code")
                .param("redirect_uri", "connector://authenticated")
                .param("client_id", getClientId())
                .param("session_state", sessionState)
                .param("code_verifier", codeVerifier)
                .param("code", code);
     
            WebTarget target = client.target(tokenUrl);
            response.close();
            response = target.
                        request(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.form(form));   

            JsonObject jsonObject = getJsonObject(response);
            
            final String tokenType = jsonObject.getString("token_type");
            final int expiresIn = jsonObject.getInt("expires_in");
    
            return IdpTokenResult.builder()
                    .tokenType(tokenType)
                    .expiresIn(expiresIn)
                    .accessToken(new JsonWebToken(jsonObject.getString("access_token")))
                    .idToken(new JsonWebToken(jsonObject.getString("id_token")))
                    .build();

        } else {
            authenticationResponse =
                getAuthenticatorClient()
                    .performAuthentication(AuthenticationRequest.builder()
                            .authenticationEndpointUrl(
                                getDiscoveryDocumentResponse().getAuthorizationEndpoint())
                            .signedChallenge(new IdpJwe(
                                signServerChallenge(
                                    authorizationResponse.getAuthenticationChallenge().getChallenge().getRawString(),
                                    certificate, contentSigner)))
                            .build());
        }
        if (isShouldVerifyState()) {
            final String stringInTokenUrl = UriUtils
                .extractParameterValue(authenticationResponse.getLocation(), "state");
            if (!state.equals(stringInTokenUrl)) {
                throw new IdpClientException("state-parameter unexpected changed");
            }
        }

        // get Token
        logger.debug("Performing getToken with remote-URL: " +
                getDiscoveryDocumentResponse().getTokenEndpoint());
        return getAuthenticatorClient().retrieveAccessToken(TokenRequest.builder()
                .tokenUrl(getDiscoveryDocumentResponse().getTokenEndpoint())
                .clientId(getClientId())
                .code(authenticationResponse.getCode())
                .ssoToken(authenticationResponse.getSsoToken())
                .redirectUrl(getRedirectUrl())
                .codeVerifier(codeVerifier)
                .idpEnc(getDiscoveryDocumentResponse().getIdpEnc())
                .build());
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

    private static String decode(final String encoded) {
        return Optional.ofNullable(encoded)
                       .map(e -> URLDecoder.decode(e, StandardCharsets.UTF_8))
                       .orElse(null);
    }

    public IdpTokenResult loginWithSsoToken(final IdpJwe ssoToken) throws IdpClientException, IdpException {
        assertThatClientIsInitialized();

        final String codeVerifier = ClientUtilities.generateCodeVerifier();
        final String nonce = RandomStringUtils.randomAlphanumeric(20);

        // Authorization
        final String state = RandomStringUtils.randomAlphanumeric(20);
        logger.debug("Performing Authorization with remote-URL: " +
            getDiscoveryDocumentResponse().getAuthorizationEndpoint());
        final AuthorizationResponse authorizationResponse =
            getAuthenticatorClient()
                .doAuthorizationRequest(AuthorizationRequest.builder()
                        .clientId(getClientId())
                        .link(getDiscoveryDocumentResponse().getAuthorizationEndpoint())
                        .codeChallenge(ClientUtilities.generateCodeChallenge(codeVerifier))
                        .codeChallengeMethod(getCodeChallengeMethod())
                        .redirectUri(getRedirectUrl())
                        .state(state)
                        .scopes(getScopes())
                        .nonce(nonce)
                        .build());

        // Authentication
        final String ssoChallengeEndpoint = getDiscoveryDocumentResponse().getAuthorizationEndpoint().replace(
            IdpConstants.BASIC_AUTHORIZATION_ENDPOINT, IdpConstants.SSO_ENDPOINT);
        logger.debug("Performing Sso-Authentication with remote-URL: " + ssoChallengeEndpoint);
        final AuthenticationResponse authenticationResponse =
            getAuthenticatorClient()
                .performAuthenticationWithSsoToken(AuthenticationRequest.builder()
                        .authenticationEndpointUrl(ssoChallengeEndpoint)
                        .ssoToken(ssoToken.getRawString())
                        .challengeToken(authorizationResponse.getAuthenticationChallenge().getChallenge())
                        .build());
        if (isShouldVerifyState()) {
            final String stringInTokenUrl = UriUtils
                .extractParameterValue(authenticationResponse.getLocation(), "state");
            if (!state.equals(stringInTokenUrl)) {
                throw new IdpClientException("state-parameter unexpected changed");
            }
        }

        // Get Token
        logger.debug("Performing getToken with remote-URL: " +
                getDiscoveryDocumentResponse().getTokenEndpoint());

                
        return getAuthenticatorClient().retrieveAccessToken(TokenRequest.builder()
                .tokenUrl(getDiscoveryDocumentResponse().getTokenEndpoint())
                .clientId(getClientId())
                .code(authenticationResponse.getCode())
                .ssoToken(ssoToken.getRawString())
                .redirectUrl(getRedirectUrl())
                .codeVerifier(codeVerifier)
                .idpEnc(getDiscoveryDocumentResponse().getIdpEnc())
                .build());
    }

    private void assertThatIdpIdentityIsValid(final PkiIdentity idpIdentity) {
        Objects.requireNonNull(idpIdentity);
        Objects.requireNonNull(idpIdentity.getCertificate());
        // Objects.requireNonNull(idpIdentity.getPrivateKey());
    }

    private IdpJwe signChallenge(
        final AuthenticationChallenge authenticationChallenge,
        final PkiIdentity idpIdentity) throws IdpJoseException {
        return AuthenticationResponseBuilder.builder().build()
            .buildResponseForChallenge(authenticationChallenge, idpIdentity)
            .getSignedChallenge()
            .encrypt(getDiscoveryDocumentResponse().getIdpEnc());
    }

    private void assertThatClientIsInitialized() throws IdpClientException {
        logger.debug("Verifying IDP-Client initialization...");
        if (getDiscoveryDocumentResponse() == null ||
            StringUtils.isEmpty(getDiscoveryDocumentResponse().getAuthorizationEndpoint()) ||
            StringUtils.isEmpty(getDiscoveryDocumentResponse().getTokenEndpoint())) {
            throw new IdpClientException(
                "IDP-Client not initialized correctly! Call .initialize() before performing an actual operation.");
        }
    }

    @Override
    public IIdpClient initializeClient() throws IdpClientException, IdpException, IdpJoseException {
        logger.info("Initializing using url: " + getDiscoveryDocumentUrl());
        setDiscoveryDocumentResponse(getAuthenticatorClient()
            .retrieveDiscoveryDocument(getDiscoveryDocumentUrl()));
        return this;
    }

    public void verifyAuthTokenToken(final IdpTokenResult authToken) throws IdpJoseException {
        authToken.getAccessToken()
            .verify(getDiscoveryDocumentResponse().getIdpSig().getPublicKey());
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getDiscoveryDocumentUrl() {
        return discoveryDocumentUrl;
    }

    public void setDiscoveryDocumentUrl(String discoveryDocumentUrl) {
        this.discoveryDocumentUrl = discoveryDocumentUrl;
    }

    public boolean isShouldVerifyState() {
        return shouldVerifyState;
    }

    public void setShouldVerifyState(boolean shouldVerifyState) {
        this.shouldVerifyState = shouldVerifyState;
    }

    public Set<IdpScope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<IdpScope> scopes) {
        this.scopes = scopes;
    }

    public AuthenticatorClient getAuthenticatorClient() {
        return authenticatorClient;
    }

    public void setAuthenticatorClient(AuthenticatorClient authenticatorClient) {
        this.authenticatorClient = authenticatorClient;
    }

    public CodeChallengeMethod getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public void setCodeChallengeMethod(CodeChallengeMethod codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
    }

    public DiscoveryDocumentResponse getDiscoveryDocumentResponse() {
        return discoveryDocumentResponse;
    }

    public void setDiscoveryDocumentResponse(DiscoveryDocumentResponse discoveryDocumentResponse) {
        this.discoveryDocumentResponse = discoveryDocumentResponse;
    }
}
