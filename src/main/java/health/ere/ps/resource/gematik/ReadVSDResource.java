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

import static health.ere.ps.resource.gematik.Extractors.extractRuntimeConfigFromHeaders;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;

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
    public ReadVSDResponse readVsd(
        @QueryParam("egkHandle") String egkHandle,
        @QueryParam("smcbHandle") String smcbHandle,
        @QueryParam("performOnlineCheck") String performOnlineCheck,
        @QueryParam("readOnlineReceipt") String readOnlineReceipt
    ) throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        if (runtimeConfig == null) {
            runtimeConfig = new RuntimeConfig();
        }
        runtimeConfig.setSMCBHandle(smcbHandle);
        return vsdService.readVSD(
            runtimeConfig,
            egkHandle,
            smcbHandle,
            Boolean.parseBoolean(performOnlineCheck),
            Boolean.parseBoolean(readOnlineReceipt)
        );
    }
}