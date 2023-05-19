package health.ere.ps.resource.gematik;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.exception.gematik.ERezeptWorkflowException;
import health.ere.ps.service.gematik.ERezeptWorkflowService;

@Path("/workflow")
public class ERezeptWorkflowResource {

    @Inject
    ERezeptWorkflowService eRezeptWorkflowService;

    static IParser jsonParser = FhirContext.forR4().newJsonParser();
    static IParser xmlParser = FhirContext.forR4().newXmlParser();

    @Context
    HttpServletRequest httpServletRequest;

    @POST
    @Path("/task")
    public Response createERezeptTask(@HeaderParam("accept") String accept) {
        Task task = eRezeptWorkflowService.createERezeptTask(extractRuntimeConfigFromHeaders(httpServletRequest));
        if("application/xml".equals(accept)) {
            return Response.ok().entity(xmlParser.encodeResourceToString(task)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.ok().entity(jsonParser.encodeResourceToString(task)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    static RuntimeConfig extractRuntimeConfigFromHeaders(HttpServletRequest httpServletRequest) {
        for(Object name : Collections.list(httpServletRequest.getHeaderNames())) {
            if(name.toString().startsWith("X-")) {
                return new RuntimeConfig(httpServletRequest);
            }
        }
        return null;
    }

    @POST
    @Path("/sign")
    public Response signBundleWithIdentifiers(@HeaderParam("Content-Type") String contentType, String bundle) throws DataFormatException, ERezeptWorkflowException {
        Bundle bundleObject = string2bundle(contentType, bundle);
        SignResponse signResponse = eRezeptWorkflowService.signBundleWithIdentifiers(bundleObject, false, extractRuntimeConfigFromHeaders(httpServletRequest));
        String base64String = signResponse2base64String(signResponse);
        return Response.ok().entity(base64String).type(MediaType.TEXT_PLAIN).build();
    }

    static String signResponse2base64String(SignResponse signResponse) {
        return new String(Base64.getEncoder().encode(signResponse.getSignatureObject().getBase64Signature().getValue()));
    }

    static Bundle string2bundle(String contentType, String bundle) {
        Bundle bundleObject = "application/xml".equals(contentType) ? xmlParser.parseResource(Bundle.class, bundle) : jsonParser.parseResource(Bundle.class, bundle);
        return bundleObject;
    }

    @POST
    @Path("/batch-sign")
    public Response signBundlesWithIdentifiers(@HeaderParam("Content-Type") String contentType, String bundles) throws DataFormatException, ERezeptWorkflowException {
        List<Bundle> bundlesList = Arrays.asList(bundles.split("\\r?\\n")).stream().map((bundle) ->  string2bundle(contentType, bundle)).collect(Collectors.toList());
        List<SignResponse> signResponse = eRezeptWorkflowService.signBundleWithIdentifiers(bundlesList, false, extractRuntimeConfigFromHeaders(httpServletRequest));
        String responses = signResponse.stream().map(ERezeptWorkflowResource::signResponse2base64String).collect(Collectors.joining("\n"));
        return Response.ok().entity(responses).type(MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("/cards")
    public GetCardsResponse cards() {
        try {
            return eRezeptWorkflowService.getCards(extractRuntimeConfigFromHeaders(httpServletRequest));
        } catch (FaultMessage e) {
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/update")
    public Response updateERezeptTask(UpdateERezept updateERezept) {
        eRezeptWorkflowService.updateERezeptTask(updateERezept.getTaskId(), updateERezept.getAccessCode(), Base64.getDecoder().decode(updateERezept.getSignedBytes()), extractRuntimeConfigFromHeaders(httpServletRequest));
        return Response.ok().build();
    }

    @POST
    @Path("/abort")
    public Response abortERezeptTask(AbortERezept abortERezept) {
        eRezeptWorkflowService.abortERezeptTask(extractRuntimeConfigFromHeaders(httpServletRequest), abortERezept.getTaskId(), abortERezept.getAccessCode());
        return Response.noContent().build();
    }

    @POST
    @Path("/comfortsignature/activate")
    public Response activate() {
        String userId = eRezeptWorkflowService.activateComfortSignature(extractRuntimeConfigFromHeaders(httpServletRequest));
        return Response.ok(Entity.text(userId)).build();
    }

    @POST
    @Path("/comfortsignature/deactivate")
    public Response deactivate() {
        eRezeptWorkflowService.deactivateComfortSignature(extractRuntimeConfigFromHeaders(httpServletRequest));
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
