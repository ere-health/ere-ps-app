package health.ere.ps.resource.kbv;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.ere.ps.service.fhir.FHIRService;
import health.ere.ps.service.kbv.XSLTService;
import jakarta.inject.Inject;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.hl7.fhir.r4.model.Bundle;

import javax.xml.transform.TransformerException;
import java.io.IOException;

import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.TEXT_HTML_TYPE;

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
        try {
            String htmlPreview = xsltService.generateHtmlForBundle(string2bundle(contentType, bundle));
            return Response.ok(htmlPreview).type(TEXT_HTML_TYPE).build();
        } catch (IOException | TransformerException e) {
            throw new WebApplicationException(e);
        }
    }

    Bundle string2bundle(String contentType, String bundle) {
        IParser parser = APPLICATION_XML.equals(contentType) ? xmlParser : jsonParser;
        return parser.parseResource(Bundle.class, bundle);
    }
}
