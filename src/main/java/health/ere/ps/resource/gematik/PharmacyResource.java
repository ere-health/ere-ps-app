package health.ere.ps.resource.gematik;

import jakarta.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;

import org.hl7.fhir.r4.model.Bundle;

import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import health.ere.ps.config.UserConfig;
import health.ere.ps.service.gematik.PharmacyService;

@Path("/pharmacy")
public class PharmacyResource {

    @Inject
    PharmacyService pharmacyService;

    @Context
    HttpServletRequest httpServletRequest;

    @Inject
    UserConfig userConfig;

    @GET
    @Path("Task")
    public Bundle task(@QueryParam("egkHandle") String egkHandle, @QueryParam("smcbHandle") String smcbHandle) throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        return pharmacyService.getEPrescriptionsForCardHandle(egkHandle, smcbHandle, ERezeptWorkflowResource.extractRuntimeConfigFromHeaders(httpServletRequest, userConfig));
    }

    @GET
    @Path("Accept")
    public Bundle ePrescription(@QueryParam("token") String token) throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        return pharmacyService.accept(token, ERezeptWorkflowResource.extractRuntimeConfigFromHeaders(httpServletRequest, userConfig));
    }

}
