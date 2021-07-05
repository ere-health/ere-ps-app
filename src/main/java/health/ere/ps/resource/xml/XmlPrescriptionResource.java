package health.ere.ps.resource.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;
import health.ere.ps.event.BundlesEvent;
import health.ere.ps.event.SignAndUploadBundlesEvent;
import health.ere.ps.service.fhir.bundle.EreBundle;

@Path("xmlPrescription")
public class XmlPrescriptionResource {

    FhirContext fhirContext = FhirContext.forR4();

    // Get <Bundle> tag including content
    private static Pattern p = Pattern.compile(".*(<Bundle[^>]*>.*</Bundle>).*", Pattern.DOTALL);

    @Inject
    Event<BundlesEvent> bundleEvent;

    @POST
    public Response post(String xml) {
        Matcher m = p.matcher(xml);
        if (!m.matches()) {
            return Response.status(Status.NOT_ACCEPTABLE).entity(Entity.text("Could not extract inner text")).build();
        } else {
            String bundleXml = m.group(1);
            Bundle bundle = fhirContext.newXmlParser().parseResource(Bundle.class, bundleXml);
            bundleEvent.fireAsync(new BundlesEvent(bundle));
        }
        return Response.ok().build();
    }
}
