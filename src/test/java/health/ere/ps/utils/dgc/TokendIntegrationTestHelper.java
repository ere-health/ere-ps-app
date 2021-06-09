package health.ere.ps.utils.dgc;

import health.ere.ps.model.idp.client.*;
import health.ere.ps.model.idp.client.authentication.AuthenticationChallenge;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.idp.client.AuthenticatorClient;
import io.quarkus.test.junit.mockito.InjectMock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Helper for a mockup the authorization of the telematik.
 */
public class TokendIntegrationTestHelper {
    protected PkiIdentity serverIdentity;
    protected PkiIdentity rsaClientIdentity;

    @InjectMock
    private AuthenticatorClient authenticatorClient;

    @InjectMock
    private CardCertificateReaderService cardCertificateReaderService;

    protected void mockTokenCreation(String token) throws Exception {
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
