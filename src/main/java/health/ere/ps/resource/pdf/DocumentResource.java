package health.ere.ps.resource.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import javax.xml.transform.TransformerException;

import health.ere.ps.service.fhir.FHIRService;
import org.apache.fop.apps.FOPException;
import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.service.pdf.DocumentService;

@Path("/document")
public class DocumentResource {
    @Inject
    DocumentService documentService;

    private static final FhirContext fhirContext = FHIRService.getFhirContext();
    IParser jsonParser = fhirContext.newJsonParser();
    IParser xmlParser = fhirContext.newXmlParser();

    @POST
    @Path("bundles")
    public Response createAndSendPrescriptions(String bundlesString) {

        JsonArray jsonArray = Json.createReader(new StringReader(bundlesString)).readArray();

        List<BundleWithAccessCodeOrThrowable> bundles = jsonArray.stream().map(jv -> {
            return convert(jv);
        }).filter(Objects::nonNull).collect(Collectors.toList());

        ByteArrayOutputStream boas;
        try {
            boas = documentService.generateERezeptPdf(bundles);
        } catch (FOPException | IOException | TransformerException e) {
            throw new WebApplicationException(e);
        }
        return Response.ok().entity(boas.toByteArray()).type("application/pdf").build();
    }

    private BundleWithAccessCodeOrThrowable convert(JsonValue jv) {
        BundleWithAccessCodeOrThrowable bt = new BundleWithAccessCodeOrThrowable();
        if(jv instanceof JsonObject) {
            JsonObject jo = (JsonObject) jv;
            if(jo.containsKey("accessCode")) {
                bt.setAccessCode(jo.getString("accessCode"));
            }
            String mimeType = jo.getString("mimeType", "application/json");
            if("application/xml".equals(mimeType)) {
                bt.setBundle(xmlParser.parseResource(Bundle.class, jo.getJsonString("bundle").getString()));
            } else {
                bt.setBundle(jsonParser.parseResource(Bundle.class, jo.getJsonObject("bundle").toString()));
            }
        }
        return bt;
    }
}
