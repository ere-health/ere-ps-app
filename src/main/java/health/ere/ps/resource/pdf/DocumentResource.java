package health.ere.ps.resource.pdf;

import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.service.pdf.DocumentService;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonReader;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.apache.fop.apps.FOPException;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;

@Path("/document")
public class DocumentResource {

    @Inject
    DocumentService documentService;

    @POST
    @Path("bundles")
    public Response createAndSendPrescriptions(String bundlesString) {
        JsonReader reader = Json.createReader(new StringReader(bundlesString));
        JsonArray jsonArray = reader.readArray();
        List<BundleWithAccessCodeOrThrowable> bundles = jsonArray.stream()
            .map(jv -> documentService.convert(jv))
            .filter(Objects::nonNull)
            .toList();

        try {
            ByteArrayOutputStream os = documentService.generateERezeptPdf(bundles);
            return Response.ok().entity(os.toByteArray()).type("application/pdf").build();
        } catch (FOPException | IOException | TransformerException e) {
            throw new WebApplicationException(e);
        }
    }
}