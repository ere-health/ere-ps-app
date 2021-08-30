package health.ere.ps.resource.pdf;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.service.pdf.DocumentService;

@Path("/document")
public class DocumentResource {
    @Inject
    DocumentService documentService;

    IParser parser = FhirContext.forR4().newJsonParser();

    @Path("/bundles")
    public Response createAndSendPrescriptions(List<BundleWithAccessCodeOrThrowable> bundles) {
        ByteArrayOutputStream boas = documentService.generateERezeptPdf(bundles);
        return Response.ok(Entity.entity(boas.toByteArray(), "application/pdf")).build();
    }
}
