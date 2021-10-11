package health.ere.ps.resource.gematik;

import java.util.Base64;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;
import health.ere.ps.exception.gematik.ERezeptWorkflowException;
import health.ere.ps.service.gematik.ERezeptWorkflowService;

@Path("/workflow")
public class ERezeptWorkflowResource {

    @Inject
    ERezeptWorkflowService eRezeptWorkflowService;

    IParser jsonParser = FhirContext.forR4().newJsonParser();
    IParser xmlParser = FhirContext.forR4().newXmlParser();

    @POST
    @Path("/task")
    public Response createERezeptTask(@HeaderParam("accept") String accept) {
        Task task = eRezeptWorkflowService.createERezeptTask(null);
        if("application/xml".equals(accept)) {
            return Response.ok().entity(xmlParser.encodeResourceToString(task)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.ok().entity(jsonParser.encodeResourceToString(task)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @POST
    @Path("/sign")
    public Response signBundleWithIdentifiers(@HeaderParam("Content-Type") String contentType, String bundle) throws DataFormatException, ERezeptWorkflowException {
        Bundle bundleObject = "application/xml".equals(contentType) ? xmlParser.parseResource(Bundle.class, bundle) : jsonParser.parseResource(Bundle.class, bundle);
        SignResponse signResponse = eRezeptWorkflowService.signBundleWithIdentifiers(bundleObject);
        return Response.ok().entity(Base64.getEncoder().encode(signResponse.getSignatureObject().getBase64Signature().getValue())).type(MediaType.TEXT_PLAIN).build();
    }

    @POST
    @Path("/update")
    public Response updateERezeptTask(UpdateERezept updateERezept) {
        eRezeptWorkflowService.updateERezeptTask(updateERezept.getTaskId(), updateERezept.getAccessCode(), Base64.getDecoder().decode(updateERezept.getSignedBytes()), null);
        return Response.ok().build();
    }

    @POST
    @Path("/abort")
    public Response postUserId(AbortERezept abortERezept) {
        eRezeptWorkflowService.abortERezeptTask(null, abortERezept.getTaskId(), abortERezept.getAccessCode());
        return Response.ok().build();
    }


    @GET
    @Path("/comfortsignature/user-id")
    public Response getUserId() {
        return Response.ok(Entity.text(eRezeptWorkflowService.getUserIdForComfortSignature())).build();
    }

    @POST
    @Path("/comfortsignature/user-id")
    public Response postUserId(String userId) {
        eRezeptWorkflowService.setUserIdForComfortSignature(userId);
        return Response.ok().build();
    }
}
