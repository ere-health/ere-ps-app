package health.ere.ps.resource.xml;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.hl7.fhir.r4.model.Bundle;

import health.ere.ps.event.BundlesEvent;
import health.ere.ps.service.fhir.XmlPrescriptionProcessor;

@Path("xmlPrescription")
public class XmlPrescriptionResource {

    @Inject
    Event<BundlesEvent> bundleEvent;

    @POST
    public Response post(String xml) {
        Bundle[] bundle = XmlPrescriptionProcessor.parseFromString(xml);
        bundleEvent.fireAsync(new BundlesEvent(bundle));
        return Response.ok().build();
    }
}
