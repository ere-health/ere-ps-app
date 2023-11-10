package health.ere.ps.resource.kbv;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.ere.ps.service.kbv.XSLTService;

@Path("/kbv")
public class XSLTResource {
    @Inject
    XSLTService xsltService;

    IParser jsonParser = FhirContext.forR4().newJsonParser();
    IParser xmlParser = FhirContext.forR4().newXmlParser();

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
