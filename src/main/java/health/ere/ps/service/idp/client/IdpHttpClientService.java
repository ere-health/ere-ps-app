package health.ere.ps.service.idp.client;

import javax.enterprise.context.Dependent;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
