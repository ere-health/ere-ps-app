package health.ere.ps.resource.status;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.health.HealthChecker;
import health.ere.ps.service.health.HealthInfo;
import io.smallrye.common.annotation.Blocking;
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
    // @Blocking
    public Response status() {
        HealthInfo healthInfo = healthChecker.getHealthInfo(extractRuntimeConfigFromHeaders());
        return Response.ok(healthInfo).build();
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
