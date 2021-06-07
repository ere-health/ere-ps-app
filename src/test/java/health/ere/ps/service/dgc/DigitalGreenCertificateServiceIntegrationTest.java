package health.ere.ps.service.dgc;

import com.github.tomakehurst.wiremock.WireMockServer;
import health.ere.ps.LocalOfflineQuarkusTestProfile;
import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.model.dgc.Nam;
import health.ere.ps.model.dgc.V;
import health.ere.ps.model.dgc.VaccinationCertificateRequest;
import health.ere.ps.model.idp.client.AuthenticationResponse;
import health.ere.ps.model.idp.client.AuthorizationRequest;
import health.ere.ps.model.idp.client.AuthorizationResponse;
import health.ere.ps.model.idp.client.DiscoveryDocumentResponse;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.client.authentication.AuthenticationChallenge;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.model.idp.crypto.PkiKeyResolver;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.idp.client.AuthenticatorClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(LocalOfflineQuarkusTestProfile.class)
@ExtendWith(PkiKeyResolver.class)
class DigitalGreenCertificateServiceIntegrationTest {
    @InjectMock
    private AuthenticatorClient authenticatorClient;

    @InjectMock
    private CardCertificateReaderService cardCertificateReaderService;

    @Inject
    private DigitalGreenCertificateService digitalGreenCertificateService;

    @ConfigProperty(name = "digital-green-certificate-service.issuerAPIUrl")
    private String issuerApiUrl;

    private PkiIdentity serverIdentity;

    private PkiIdentity rsaClientIdentity;

    private WireMockServer wireMockServer;

    private String mockPath;

    @BeforeEach
    void startup(@PkiKeyResolver.Filename("ecc") final PkiIdentity serverIdentity,
                 @PkiKeyResolver.Filename("C_CH_AUT_R2048") final PkiIdentity rsaClientIdentity) throws MalformedURLException {
        this.serverIdentity = serverIdentity;
        this.rsaClientIdentity = rsaClientIdentity;

        URL url = new URL(issuerApiUrl);

        if (!"localhost".equals(url.getHost())) {
            throw new RuntimeException("Testing is only possible for localhost urls");
        }
        mockPath = url.getPath();
        wireMockServer = new WireMockServer(wireMockConfig().port(url.getPort()).bindAddress("localhost"));
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void issueVaccinationCertificate() throws IdpClientException, IdpException, ConnectorCardCertificateReadException, IdpCryptoException, IdpJoseException {
        // mock setup for token
        String token = "testToken";

        mockTokenCreation(token);

        // mock response

        String dob = "1921-01-01";

        String name = "Testname Lastname";

        String givenName = "Testgiven Name";

        String id = "testId";

        String tg = "testTg";

        String vp = "testVp";

        String mp = "testMp";

        String ma = "testMa";

        int dn = 123;

        int sd = 345;

        String dt = "2021-01-01";

        byte[] response = new byte[]{};

        wireMockServer.stubFor(post(mockPath)
                .withHeader("Authorization", equalTo("Bearer " + token))
                .withHeader("Accept", equalTo("application/pdf"))
                .withRequestBody(equalToJson("{\"nam\":{" +
                        "\"fn\": \"" + name + "\"," +
                        "\"gn\": \"" + givenName + "\"" +
                        "}," +
                        "\"dob\": \"" + dob + "\"," +
                        "\"v\": [{" +
                        "\"id\": \"" + id + "\"," +
                        "\"tg\": \"" + tg + "\"," +
                        "\"vp\": \"" + vp + "\"," +
                        "\"mp\": \"" + mp + "\"," +
                        "\"ma\": \"" + ma + "\"," +
                        "\"dn\": " + dn + "," +
                        "\"sd\": " + sd + "," +
                        "\"dt\": \"" + dt + "\"" +
                        "}]}"))
                .willReturn(ok()
                        .withBody(response)));

        VaccinationCertificateRequest vaccinationCertificateRequest = new VaccinationCertificateRequest();

        Nam nam = new Nam();

        nam.fn = name;
        nam.gn = givenName;

        vaccinationCertificateRequest.nam = nam;
        vaccinationCertificateRequest.dob = "1921-01-01";

        V v = new V();

        v.id = id;
        v.tg = tg;
        v.vp = vp;
        v.mp = mp;
        v.ma = ma;
        v.dn = dn;
        v.sd = sd;
        v.dt = dt;

        vaccinationCertificateRequest.v = Collections.singletonList(v);

        byte[] actualResponse = digitalGreenCertificateService.issue(vaccinationCertificateRequest);

        assertNotNull(actualResponse);
        assertArrayEquals(response, actualResponse);
    }

    private void mockTokenCreation(String token) throws ConnectorCardCertificateReadException, IdpCryptoException, IdpJoseException, IdpClientException, IdpException {
        when(cardCertificateReaderService.retrieveCardCertIdentity(any(), any(), any(), any())).thenReturn(rsaClientIdentity);

        DiscoveryDocumentResponse discoveryDocumentResponse = mock(DiscoveryDocumentResponse.class);

        AuthorizationResponse authorizationResponse = mock(AuthorizationResponse.class);

        AuthenticationChallenge authenticationChallenge = mock(AuthenticationChallenge.class);

        JsonWebToken challengeToken = mock(JsonWebToken.class);

        AuthenticationResponse authenticationResponse = mock(AuthenticationResponse.class);

        IdpTokenResult idpTokenResult = mock(IdpTokenResult.class);

        JsonWebToken accessToken = mock(JsonWebToken.class);

        when(authenticatorClient.retrieveDiscoveryDocument(any())).thenReturn(discoveryDocumentResponse);
        when(discoveryDocumentResponse.getAuthorizationEndpoint()).thenReturn("nonEmpty");
        when(discoveryDocumentResponse.getTokenEndpoint()).thenReturn("nonEmpty");
        when(discoveryDocumentResponse.getIdpEnc()).thenReturn(serverIdentity.getCertificate().getPublicKey());
        when(authenticatorClient.doAuthorizationRequest(any())).thenAnswer((invocation) -> {
            AuthorizationRequest authorizationRequest = invocation.getArgument(0);

            when(authenticationResponse.getLocation()).thenReturn("http://localhost?state=" + authorizationRequest.getState());
            return authorizationResponse;
        });
        when(authorizationResponse.getAuthenticationChallenge()).thenReturn(authenticationChallenge);
        when(authenticationChallenge.getChallenge()).thenReturn(challengeToken);
        when(challengeToken.getRawString()).thenReturn("testString");
        when(authenticatorClient.performAuthentication(any())).thenReturn(authenticationResponse);

        when(authenticatorClient.retrieveAccessToken(any())).thenReturn(idpTokenResult);
        when(idpTokenResult.getAccessToken()).thenReturn(accessToken);

        when(accessToken.getRawString()).thenReturn(token);
    }
}
