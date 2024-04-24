package health.ere.ps.resource.kbv;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javax.xml.transform.TransformerException;

import health.ere.ps.service.fhir.FHIRService;
import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.ere.ps.service.kbv.XSLTService;

@Path("/kbv")
public class XSLTResource {
    @Inject
    XSLTService xsltService;

    private static final FhirContext fhirContext = FHIRService.getFhirContext();
    IParser jsonParser = fhirContext.newJsonParser();
    IParser xmlParser = fhirContext.newXmlParser();

    @POST
    @Path("transform")
    public Response transform(@HeaderParam("Content-Type") String contentType, String bundle) {
        String htmlPreview;
        try {
            htmlPreview = xsltService.generateHtmlForBundle(string2bundle(contentType, bundle));
            return Response.ok(htmlPreview).type(MediaType.TEXT_HTML_TYPE).build();
        } catch (IOException | TransformerException e) {
            throw new WebApplicationException(e);
        }
    }

    Bundle string2bundle(String contentType, String bundle) {
        Bundle bundleObject = "application/xml".equals(contentType) ? xmlParser.parseResource(Bundle.class, bundle) : jsonParser.parseResource(Bundle.class, bundle);
        return bundleObject;
    }
}
