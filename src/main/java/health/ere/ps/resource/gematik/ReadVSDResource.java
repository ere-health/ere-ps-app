package health.ere.ps.resource.gematik;

import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import de.gematik.ws.conn.vsds.vsdservice.v5.ReadVSDResponse;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.service.gematik.VSDService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import static health.ere.ps.resource.gematik.Extractors.extractRuntimeConfigFromHeaders;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.Response.Status.CONFLICT;
import static jakarta.ws.rs.core.Response.Status.OK;

@RequestScoped
@Path("/read-vsd")
public class ReadVSDResource {

    @Context
    HttpServletRequest httpServletRequest;

    @Inject
    UserConfig userConfig;

    @Inject
    VSDService vsdService;

    @GET
    @Produces(APPLICATION_XML)
    public Response readVsd(
        @QueryParam("egkHandle") String egkHandle,
        @QueryParam("smcbHandle") String smcbHandle,
        @QueryParam("performOnlineCheck") String performOnlineCheck,
        @QueryParam("readOnlineReceipt") String readOnlineReceipt
    ) {
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        if (runtimeConfig == null) {
            runtimeConfig = new RuntimeConfig();
        }
        runtimeConfig.setSMCBHandle(smcbHandle);
        try {
            ReadVSDResponse readVSDResponse = vsdService.readVSD(
                runtimeConfig,
                egkHandle,
                smcbHandle,
                Boolean.parseBoolean(performOnlineCheck),
                Boolean.parseBoolean(readOnlineReceipt)
            );
            return Response.status(OK).entity(readVSDResponse).build();
        } catch (FaultMessage e) {
            return Response.status(CONFLICT).entity(e.getFaultInfo()).build();
        } catch (de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage  e) {
            return Response.status(CONFLICT).entity(e.getFaultInfo()).build();
        }
    }
}