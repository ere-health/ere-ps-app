package health.ere.ps.service.idp.client;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient
public interface IdpHttpClientService {
    String DISCOVERY_DOCUMENT_URI = "/.well-known/openid-configuration";
    String USER_AGENT = "IdP-Client";

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
}
