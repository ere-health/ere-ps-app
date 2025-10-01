package health.ere.ps.service.idp.client;

import com.diffplug.common.base.Errors;
import com.diffplug.common.base.Throwing;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.model.idp.client.AuthenticationRequest;
import health.ere.ps.model.idp.client.AuthenticationResponse;
import health.ere.ps.model.idp.client.AuthorizationRequest;
import health.ere.ps.model.idp.client.AuthorizationResponse;
import health.ere.ps.model.idp.client.DiscoveryDocumentResponse;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.client.TokenRequest;
import health.ere.ps.model.idp.client.field.CodeChallengeMethod;
import health.ere.ps.model.idp.client.field.IdpScope;
import health.ere.ps.model.idp.client.token.IdpJwe;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.connector.auth.SmcbAuthenticatorService;
import health.ere.ps.service.idp.IDPClient;
import health.ere.ps.service.idp.client.authentication.UriUtils;
import health.ere.ps.service.idp.crypto.KeyAnalysis;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static health.ere.ps.model.idp.client.brainPoolExtension.BrainpoolAlgorithmSuiteIdentifiers.BRAINPOOL256_USING_SHA256;
import static health.ere.ps.model.idp.client.field.ClaimName.NESTED_JWT;
import static health.ere.ps.service.idp.client.IdpHttpClientService.DISCOVERY_DOCUMENT_URI;
import static org.jose4j.jws.AlgorithmIdentifiers.RSA_PSS_USING_SHA256;

@ApplicationScoped
@Startup
public class IdpClient implements IDPClient {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final Set<IdpScope> scopes = Set.of(IdpScope.OPENID, IdpScope.EREZEPT);
    private final Set<IdpScope> scopes_rudev = Set.of(IdpScope.OPENID, IdpScope.EREZEPTDEV);

    @ConfigProperty(name = "ere.workflow-service.prescription.server.url")
    String prescriptionServiceURL;

    @ConfigProperty(name = "ere.konnektor.sign.challenge.retry", defaultValue = "5")
    int signChallengeRetry;

    private final CodeChallengeMethod codeChallengeMethod = CodeChallengeMethod.S256;

    @Inject
    AppConfig appConfig;
    @Inject
    AuthenticatorClient authenticatorClient;
    @Inject
    SmcbAuthenticatorService smcbAuthenticatorService;
    @Inject
    Logger logger;

    private String discoveryDocumentUrl;
    private DiscoveryDocumentResponse discoveryDocumentResponse;

    @Override
    @PostConstruct
    public void initializeClient() throws IdpClientException, IdpException, IdpCryptoException, IdpJoseException {
        discoveryDocumentUrl = appConfig.getIdpBaseURL() + DISCOVERY_DOCUMENT_URI;
        logger.info("Initializing using url: " + discoveryDocumentUrl);
        discoveryDocumentResponse = authenticatorClient.retrieveDiscoveryDocument(discoveryDocumentUrl);
    }

    public String getDiscoveryDocumentUrl() {
        return discoveryDocumentUrl;
    }

    private String signServerChallenge(
        String challengeToSign,
        X509Certificate certificate,
        Function<Pair<String, String>, String> contentSigner
    ) throws IdpJoseException {
        final JwtClaims claims = new JwtClaims();
        claims.setClaim(NESTED_JWT.getJoseName(), challengeToSign);

        final JsonWebSignature jsonWebSignature = new JsonWebSignature();
        jsonWebSignature.setPayload(claims.toJson());
        jsonWebSignature.setHeader("typ", "JWT");
        jsonWebSignature.setHeader("cty", "NJWT");

        if (KeyAnalysis.isEcKey(certificate.getPublicKey())) {
            jsonWebSignature.setAlgorithmHeaderValue(BRAINPOOL256_USING_SHA256);
        } else {
            jsonWebSignature.setAlgorithmHeaderValue(RSA_PSS_USING_SHA256);
        }

        PublicKey idpPublicKey = discoveryDocumentResponse.getIdpEnc();
        JsonWebToken jwt = new JsonWebToken(
                contentSigner.apply(Pair.of(
                        jsonWebSignature.getHeaders().getEncodedHeader(),
                        jsonWebSignature.getEncodedPayload())));

        jwt.getHeaderClaims().remove("alg");
        jwt.getHeaderClaims().put("alg", BRAINPOOL256_USING_SHA256);

        return jwt.encrypt(idpPublicKey).getRawString();
    }

    public IdpTokenResult login(final PkiIdentity idpIdentity) throws IdpException, IdpClientException,
            IdpJoseException {
        assertThatIdpIdentityIsValid(idpIdentity);

        return login(idpIdentity.getCertificate(),
                Errors.rethrow().wrap((Throwing.Function<Pair<String, String>, String>) jwtPair -> {
                    final JsonWebSignature jws = new JsonWebSignature();
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

                    jws.setCertificateChainHeaderValue(idpIdentity.getCertificate());
                    jws.setKey(idpIdentity.getPrivateKey());
                    try {
                        return jws.getCompactSerialization();
                    } catch (JoseException e) {
                        throw new IdpClientException("Error during encryption", e);
                    }
                }));
    }
    public IdpTokenResult login(X509Certificate x509Certificate) throws IdpJoseException, IdpClientException, IdpException {
        return login(x509Certificate, (RuntimeConfig) null);
    }

    public IdpTokenResult login(X509Certificate x509Certificate, RuntimeConfig runtimeConfig) throws IdpJoseException,
            IdpClientException, IdpException {
        return login(x509Certificate, (pair) -> smcbAuthenticatorService.signIdpChallenge(pair, runtimeConfig, x509Certificate));
    }

    private IdpTokenResult login(
        X509Certificate certificate,
        Function<Pair<String, String>, String> contentSigner
    ) throws IdpClientException, IdpException, IdpJoseException {
        assertThatClientIsInitialized();

        final String codeVerifier = ClientUtilities.generateCodeVerifier();
        final String nonce = RandomStringUtils.randomAlphanumeric(20);

        // Authorization
        final String state = RandomStringUtils.randomAlphanumeric(20);
        logger.debug("Performing Authorization with remote-URL: " + discoveryDocumentResponse.getAuthorizationEndpoint());
        final AuthorizationResponse authorizationResponse =
                authenticatorClient
                        .doAuthorizationRequest(AuthorizationRequest.builder()
                                .clientId(appConfig.getIdpClientId())
                                .link(discoveryDocumentResponse.getAuthorizationEndpoint())
                                .codeChallenge(ClientUtilities.generateCodeChallenge(codeVerifier))
                                .codeChallengeMethod(codeChallengeMethod)
                                .redirectUri(appConfig.getIdpAuthRequestRedirectURL())
                                .state(state)
                                .scopes(prescriptionServiceURL.equals("https://erp-dev.zentral.erp.splitdns.ti-dienste.de") ? scopes_rudev : scopes)
                                .nonce(nonce)
                                .build());

        String rawString = authorizationResponse.getAuthenticationChallenge().getChallenge().getRawString();

        int counter = 0;
        String exceptionMessage = null;
        AuthenticationResponse authenticationResponse = null;
        while (authenticationResponse == null && counter++ < signChallengeRetry) {
            try {
                IdpJwe idpJwe = new IdpJwe(signServerChallenge(rawString, certificate, contentSigner));
                logger.debug("Performing Authentication with remote-URL: " + discoveryDocumentResponse.getAuthorizationEndpoint());
                AuthenticationRequest request = AuthenticationRequest.builder()
                    .authenticationEndpointUrl(discoveryDocumentResponse.getAuthorizationEndpoint())
                    .signedChallenge(idpJwe)
                    .build();
                authenticationResponse = authenticatorClient.performAuthentication(request);
            } catch (IdpClientException e) {
                exceptionMessage = e.getMessage();
                logger.error(String.format("[%s] SIGN RETRY: %d", Thread.currentThread().getName(), counter));
            }
        }
        if (authenticationResponse == null) {
            throw new IdpClientException(exceptionMessage);
        }
        
        String stringInTokenUrl = UriUtils.extractParameterValue(authenticationResponse.getLocation(), "state");
        if (!state.equals(stringInTokenUrl)) {
            throw new IdpClientException("state-parameter unexpected changed");
        }

        // get Token
        logger.debug("Performing getToken with remote-URL: " + discoveryDocumentResponse.getTokenEndpoint());
        return authenticatorClient.retrieveAccessToken(TokenRequest.builder()
                .tokenUrl(discoveryDocumentResponse.getTokenEndpoint())
                .clientId(appConfig.getIdpClientId())
                .code(authenticationResponse.getCode())
                .ssoToken(authenticationResponse.getSsoToken())
                .redirectUrl(appConfig.getIdpAuthRequestRedirectURL())
                .codeVerifier(codeVerifier)
                .idpEnc(discoveryDocumentResponse.getIdpEnc())
                .build());
    }

    private void assertThatIdpIdentityIsValid(final PkiIdentity idpIdentity) {
        Objects.requireNonNull(idpIdentity);
        Objects.requireNonNull(idpIdentity.getCertificate());
    }

    private void assertThatClientIsInitialized() throws IdpClientException {
        logger.debug("Verifying IDP-Client initialization...");

        if (discoveryDocumentResponse == null ||
                StringUtils.isEmpty(discoveryDocumentResponse.getAuthorizationEndpoint()) ||
                StringUtils.isEmpty(discoveryDocumentResponse.getTokenEndpoint())) {
            throw new IdpClientException(
                    "IDP-Client not initialized correctly! Call .initialize() before performing an actual operation.");
        }
    }
}
