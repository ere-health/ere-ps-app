package health.ere.ps.service.idp.client;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Dependent
@RegisterRestClient
public interface IdpHttpClientService {
    String DISCOVERY_DOCUMENT_URI = "/.well-known/openid-configuration";


    String USER_AGENT = "ere.health/1.0.0 IncentergyGmbH/GEMIncenereS2QmFN83P";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name=HttpHeaders.USER_AGENT, value=USER_AGENT)
    Response doGenericGetRequest();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name=HttpHeaders.USER_AGENT, value=USER_AGENT)
    Response doAuthorizationRequest(@QueryParam("scope") String scope,
                                    @QueryParam("response_type") String responseType,
                                    @QueryParam("redirect_uri") String redirect_uri,
                                    @QueryParam("state") String state,
                                    @QueryParam("code_challenge_method") String code_challenge_method,
                                    @QueryParam("nonce") String nonce,
                                    @QueryParam("client_id") String client_id,
                                    @QueryParam("code_challenge") String code_challenge);

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name=HttpHeaders.USER_AGENT, value=USER_AGENT)
    Response doAccessTokenRequest(@FormParam("grant_type") String grantType,
                                  @FormParam("client_id") String clientId,
                                  @FormParam("code") String code,
                                  @FormParam("key_verifier") String keyVerifier,
                                  @FormParam("redirect_uri") String redirectUri);

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ClientHeaderParam(name=HttpHeaders.USER_AGENT, value=USER_AGENT)
    Response doAuthenticationRequest(@FormParam("signed_challenge") String signedChallenge);

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name=HttpHeaders.USER_AGENT, value=USER_AGENT)
    Response doAuthenticationRequestWithSsoToken(@FormParam("ssotoken") String ssoToken,
                                                 @FormParam("unsigned_challenge") String unsignedChallenge);
}
