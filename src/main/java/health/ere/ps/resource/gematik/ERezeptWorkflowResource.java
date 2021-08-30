package health.ere.ps.resource.gematik;

import java.util.Base64;

import javax.inject.Inject;
import javax.ws.rs.GET;
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

    IParser parser = FhirContext.forR4().newJsonParser();

    @POST
    @Path("/task")
    public Response createERezeptTask() {
        Task task = eRezeptWorkflowService.createERezeptTask();
        return Response.ok().entity(parser.encodeResourceToString(task)).type(MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("/sign")
    public Response signBundleWithIdentifiers(String bundle) throws DataFormatException, ERezeptWorkflowException {
        SignResponse signResponse = eRezeptWorkflowService.signBundleWithIdentifiers(parser.parseResource(Bundle.class, bundle));
        return Response.ok().entity(Base64.getEncoder().encode(signResponse.getSignatureObject().getBase64Signature().getValue())).type(MediaType.TEXT_PLAIN).build();
    }

    @POST
    @Path("/update")
    public Response updateERezeptTask(UpdateERezept updateERezept) {
        eRezeptWorkflowService.updateERezeptTask(updateERezept.getTaskId(), updateERezept.getAccessCode(), Base64.getDecoder().decode(updateERezept.getSignedBytes()));
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
