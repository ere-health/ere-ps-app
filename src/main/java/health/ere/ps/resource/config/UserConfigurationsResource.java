package health.ere.ps.resource.config;


import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.service.config.UserConfigurationService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/config")
public class UserConfigurationsResource {

    @Inject
    UserConfigurationService userConfigService;

    @PUT
    public Response updateConfigurations(UserConfigurations userConfigurations) {
        userConfigService.updateConfig(userConfigurations);
        return Response.ok().build();
    }

    @GET
    public Response getConfigurations() {
        return Response.ok(userConfigService.getConfig()).build();
    }
}
