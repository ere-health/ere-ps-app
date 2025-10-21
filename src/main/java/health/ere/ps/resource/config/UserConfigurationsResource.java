package health.ere.ps.resource.config;

import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.service.config.UserConfigurationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

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
