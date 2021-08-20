package health.ere.ps.resource.gematik;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.ere.ps.service.gematik.ERezeptWorkflowService;

@Path("/workflow")
public class ERezeptWorkflowResource {

    @Inject
    ERezeptWorkflowService eRezeptWorkflowService;

    IParser parser = FhirContext.forR4().newJsonParser();
    
    @POST
    public Response createERezeptTask() {
        Task task = eRezeptWorkflowService.createERezeptTask(null);
        return Response.ok(Entity.json(parser.encodeResourceToString(task))).build();
    } 
}
