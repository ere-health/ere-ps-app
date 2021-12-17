package health.ere.ps.resource.status;

import io.smallrye.common.annotation.Blocking;

import java.util.Collections;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.status.StatusService;

@Path("/status")
public class StatusResource {
    @Inject
    StatusService statusService;

    @Context
    HttpServletRequest httpServletRequest;
  
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Blocking
    public Response status() {
        return Response.ok(statusService.getStatus(extractRuntimeConfigFromHeaders())).build();
    }

    RuntimeConfig extractRuntimeConfigFromHeaders() {
        for(Object name : Collections.list(httpServletRequest.getHeaderNames())) {
            if(name.toString().startsWith("X-")) {
                return new RuntimeConfig(httpServletRequest);
            }
        }
        return null;
    }
}

