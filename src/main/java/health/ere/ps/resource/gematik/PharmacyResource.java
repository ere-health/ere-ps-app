package health.ere.ps.resource.gematik;

import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.service.gematik.PharmacyService;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/pharmacy")
public class PharmacyResource {

    @Inject
    PharmacyService pharmacyService;

    @Context
    HttpServletRequest httpServletRequest;

    @Inject
    UserConfig userConfig;

    @GET
    @Path("Subscribe")
    public String subscribe() throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        return pharmacyService.subscribe(ERezeptWorkflowResource.extractRuntimeConfigFromHeaders(httpServletRequest, userConfig), httpServletRequest.getServerName());
        
    }

    @GET
    @Path("Unsubscribe")
    public Response unsubscribe() throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        pharmacyService.unsubscribeAll(ERezeptWorkflowResource.extractRuntimeConfigFromHeaders(httpServletRequest, userConfig), httpServletRequest.getServerName());
        return Response.ok(httpServletRequest.getServerName()).build();
        
    }
 
    @GET
    @Path("Task")
    public Bundle task(@QueryParam("egkHandle") String egkHandle, @QueryParam("smcbHandle") String smcbHandle) throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        RuntimeConfig runtimeConfig = ERezeptWorkflowResource.extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        Pair<Bundle, String> pair = pharmacyService.getEPrescriptionsForCardHandle(egkHandle, smcbHandle, runtimeConfig);
        return pair.getKey();
    }

    @GET
    @Path("Accept")
    public Bundle ePrescription(@QueryParam("token") String token) throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        return pharmacyService.accept(token, ERezeptWorkflowResource.extractRuntimeConfigFromHeaders(httpServletRequest, userConfig));
    }

}
