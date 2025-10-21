package health.ere.ps.resource.xml;

import health.ere.ps.event.BundlesEvent;
import health.ere.ps.service.fhir.prescription.PrescriptionService;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.hl7.fhir.r4.model.Bundle;

import java.util.Arrays;

@Path("xmlPrescription")
public class XmlPrescriptionResource {

    @Inject
    Event<BundlesEvent> bundleEvent;

    @Inject
    PrescriptionService prescriptionService;

    @POST
    public Response post(String xml) {
        Bundle[] bundle = prescriptionService.parseFromString(xml);
        bundleEvent.fireAsync(new BundlesEvent(Arrays.asList(bundle)));
        return Response.ok().build();
    }
}
