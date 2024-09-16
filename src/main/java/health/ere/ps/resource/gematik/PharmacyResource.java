package health.ere.ps.resource.gematik;


import static health.ere.ps.resource.gematik.Extractors.extractRuntimeConfigFromHeaders;

import org.hl7.fhir.r4.model.Bundle;

import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import health.ere.ps.config.UserConfig;
import health.ere.ps.service.gematik.PharmacyService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;

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
        return pharmacyService.getEPrescriptionsForCardHandle(egkHandle, smcbHandle, extractRuntimeConfigFromHeaders(httpServletRequest, userConfig));
    }

    @GET
    @Path("Accept")
    public Bundle ePrescription(@QueryParam("token") String token) throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        return pharmacyService.accept(token, extractRuntimeConfigFromHeaders(httpServletRequest, userConfig));
    }

}
