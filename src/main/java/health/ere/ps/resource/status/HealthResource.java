package health.ere.ps.resource.status;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.health.HealthChecker;
import health.ere.ps.service.health.HealthInfo;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Collections;

@Path("/health")
public class HealthResource {

    @Inject
    HealthChecker healthChecker;

    @Context
    HttpServletRequest httpServletRequest;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response status() {
        HealthInfo healthInfo = healthChecker.getHealthInfo(extractRuntimeConfigFromHeaders());
        return Response.ok(healthInfo).build();
    }

    @GET
    @Path("uptime")
    public Response uptime() {
        HealthInfo healthInfo = healthChecker.getHealthInfo(extractRuntimeConfigFromHeaders());
        Response.Status status = healthInfo.status().equals("UP") ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR;
        return Response.status(status).build();
    }

    RuntimeConfig extractRuntimeConfigFromHeaders() {
        for (Object name : Collections.list(httpServletRequest.getHeaderNames())) {
            if (name.toString().startsWith("X-")) {
                return new RuntimeConfig(httpServletRequest);
            }
        }
        return null;
    }
}
