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
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;

import java.util.Base64;

import static health.ere.ps.resource.gematik.Extractors.extractRuntimeConfigFromHeaders;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

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
    @Produces(TEXT_PLAIN)
    public String readKvk(
        @NotNull @QueryParam("kvkHandle") String kvkHandle
    ) throws FaultMessage {
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        byte[] bytes = kvkService.readKVK(kvkHandle, runtimeConfig);
        return Base64.getEncoder().encodeToString(bytes);
    }
}