package health.ere.ps.resource.gematik;

import health.ere.ps.service.kbv.XSLTService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import javax.xml.transform.TransformerException;
import java.io.IOException;

@Path("/preview")
public class PreviewResource {
    
    @Inject
    XSLTService xsltService;
    
    @POST
    @Path("/generate")
    public String post(String content) throws IOException, TransformerException {
        return xsltService.generateHtmlForString(content);
    }
}