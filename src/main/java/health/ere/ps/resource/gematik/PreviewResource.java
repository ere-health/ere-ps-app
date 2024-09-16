package health.ere.ps.resource.gematik;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import health.ere.ps.service.kbv.XSLTService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/preview")
public class PreviewResource {
    
    @Inject
    XSLTService xsltService;
    
    @POST
    @Path("/generate")
    public String post(String content) throws IOException, TransformerException {
        String preview = xsltService.generateHtmlForString(content);
        return preview;
    }
}

