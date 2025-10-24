package health.ere.ps.resource.gematik;

import de.gematik.ws.conn.vsds.kvkservice.v4.FaultMessage;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.service.gematik.KVKService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import java.util.Base64;

import static health.ere.ps.resource.gematik.Extractors.extractRuntimeConfigFromHeaders;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static jakarta.ws.rs.core.Response.Status.CONFLICT;
import static jakarta.ws.rs.core.Response.Status.OK;

@RequestScoped
@Path("/read-kvk")
public class ReadKVKResource {

    @Context
    HttpServletRequest httpServletRequest;

    @Inject
    UserConfig userConfig;

    @Inject
    KVKService kvkService;

    @GET
    public Response readKvk(
        @NotNull @QueryParam("kvkHandle") String kvkHandle
    ) {
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        try {
            byte[] bytes = kvkService.readKVK(kvkHandle, runtimeConfig);
            String kvk = Base64.getEncoder().encodeToString(bytes);
            return Response.status(OK).entity(kvk).type(TEXT_PLAIN).build();
        } catch (FaultMessage e) {
            return Response.status(CONFLICT).entity(e.getFaultInfo()).type(APPLICATION_XML).build();
        }
    }
}