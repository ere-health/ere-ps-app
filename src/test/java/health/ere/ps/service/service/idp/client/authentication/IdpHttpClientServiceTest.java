package health.ere.ps.service.idp.client.authentication;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import health.ere.ps.service.idp.client.IdpHttpClientService;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class IdpHttpClientServiceTest {

    @Mock
    private IdpHttpClientService idpService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDoGenericGetRequest() {
        when(idpService.doGenericGetRequest()).thenReturn(Response.ok().build());
        Response response = idpService.doGenericGetRequest();
        verify(idpService, times(1)).doGenericGetRequest();
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
    }

    @Test
    public void testDoAuthorizationRequest() {
        String scope = "openid";
        String responseType = "code";
        String redirectUri = "https://example.com/callback";
        String state = "state";
        String codeChallengeMethod = "S256";
        String nonce = "nonce";
        String clientId = "client123";
        String codeChallenge = "code_challenge";

        when(idpService.doAuthorizationRequest(scope, responseType, redirectUri, state, codeChallengeMethod, nonce, clientId, codeChallenge))
                .thenReturn(Response.ok().build());

        Response response = idpService.doAuthorizationRequest(scope, responseType, redirectUri, state, codeChallengeMethod, nonce, clientId, codeChallenge);

        verify(idpService, times(1)).doAuthorizationRequest(scope, responseType, redirectUri, state, codeChallengeMethod, nonce, clientId, codeChallenge);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
    }

    @Test
    public void testDoAccessTokenRequest() {
        String grantType = "authorization_code";
        String clientId = "client123";
        String code = "authcode";
        String keyVerifier = "key_verifier";
        String redirectUri = "https://example.com/callback";

        when(idpService.doAccessTokenRequest(grantType, clientId, code, keyVerifier, redirectUri))
                .thenReturn(Response.ok().build());

        Response response = idpService.doAccessTokenRequest(grantType, clientId, code, keyVerifier, redirectUri);

        verify(idpService, times(1)).doAccessTokenRequest(grantType, clientId, code, keyVerifier, redirectUri);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
    }

    @Test
    public void testDoAuthenticationRequest() {
        String signedChallenge = "signed_challenge";

        when(idpService.doAuthenticationRequest(signedChallenge)).thenReturn(Response.ok().build());

        Response response = idpService.doAuthenticationRequest(signedChallenge);

        verify(idpService, times(1)).doAuthenticationRequest(signedChallenge);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDoAuthenticationRequestWithSsoToken() {
        String ssoToken = "ssoToken";
        String unsignedChallenge = "unsigned_challenge";

        when(idpService.doAuthenticationRequestWithSsoToken(ssoToken, unsignedChallenge)).thenReturn(Response.ok().build());

        Response response = idpService.doAuthenticationRequestWithSsoToken(ssoToken, unsignedChallenge);

        verify(idpService, times(1)).doAuthenticationRequestWithSsoToken(ssoToken, unsignedChallenge);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
    }
}
