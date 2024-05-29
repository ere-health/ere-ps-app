package health.ere.ps.resource.gematik;


import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.service.cetp.SubscriptionManager;
import health.ere.ps.service.gematik.PharmacyService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;

import java.util.List;

import static health.ere.ps.resource.gematik.Extractors.extractRuntimeConfigFromHeaders;

@Path("/pharmacy")
public class PharmacyResource {

    @Inject
    UserConfig userConfig;

    @Inject
    PharmacyService pharmacyService;

    @Inject
    SubscriptionManager subscriptionManager;

    @Context
    HttpServletRequest httpServletRequest;

    @GET
    @Path("Subscribe")
    public List<String> subscribe(
        @QueryParam("host") String host,
        @QueryParam("useCetp") Boolean useCetp
    ) {
        boolean forceCetp = useCetp != null && useCetp;
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        return subscriptionManager.manage(runtimeConfig, host, httpServletRequest.getServerName(), forceCetp, true);
    }

    @GET
    @Path("Unsubscribe")
    public List<String> unsubscribe(
        @QueryParam("host") String host,
        @QueryParam("useCetp") Boolean useCetp
    ) {
        boolean forceCetp = useCetp != null && useCetp;
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        return subscriptionManager.manage(runtimeConfig, host, httpServletRequest.getServerName(), forceCetp, false);
    }
 
    @GET
    @Path("Task")
    public Bundle task(@QueryParam("egkHandle") String egkHandle, @QueryParam("smcbHandle") String smcbHandle) throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        Pair<Bundle, String> pair = pharmacyService.getEPrescriptionsForCardHandle(egkHandle, smcbHandle, runtimeConfig);
        return pair.getKey();
    }

    @GET
    @Path("Accept")
    public Bundle ePrescription(@QueryParam("token") String token) throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        return pharmacyService.accept(token, extractRuntimeConfigFromHeaders(httpServletRequest, userConfig));
    }
}
