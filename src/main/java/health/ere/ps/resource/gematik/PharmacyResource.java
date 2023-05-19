package health.ere.ps.resource.gematik;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.hl7.fhir.r4.model.Bundle;

import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import health.ere.ps.service.gematik.PharmacyService;

@Path("/pharmacy")
public class PharmacyResource {

    @Inject
    PharmacyService pharmacyService;

    @Context
    HttpServletRequest httpServletRequest;

    @GET
    @Path("/Task")
    public Bundle task(@QueryParam("egkHandle") String egkHandle, @QueryParam("smbcHandle") String smbcHandle) throws FaultMessage {
        return pharmacyService.getEPrescriptionsForCardHandle(egkHandle, smbcHandle, ERezeptWorkflowResource.extractRuntimeConfigFromHeaders(httpServletRequest));
    } 
}
