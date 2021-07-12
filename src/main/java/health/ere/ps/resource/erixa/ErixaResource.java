package health.ere.ps.resource.erixa;


import health.ere.ps.event.erixa.ErixaSyncEvent;
import health.ere.ps.model.erixa.ErixaSyncLoad;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;


@Path("erixa")
public class ErixaResource {


    @Inject
    Event<ErixaSyncEvent> erixaEvent;

    @Deprecated
    @POST
    @Path("/sync")
    public Response handleSync(ErixaSyncLoad load) {
        ErixaSyncEvent event = new ErixaSyncEvent(load);
        erixaEvent.fireAsync(event);
        return Response.ok().build();
    }
}
