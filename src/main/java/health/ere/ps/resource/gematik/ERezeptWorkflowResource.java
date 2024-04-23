package health.ere.ps.resource.gematik;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.naming.InvalidNameException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.apache.fop.apps.FOPException;
import org.bouncycastle.crypto.CryptoException;
import org.hl7.fhir.r4.model.*;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.exception.gematik.ERezeptWorkflowException;
import health.ere.ps.service.gematik.ERezeptWorkflowService;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.service.fhir.FHIRService;
import health.ere.ps.service.gematik.PrefillPrescriptionService;
import health.ere.ps.service.pdf.DocumentService;

@Path("/workflow")
public class ERezeptWorkflowResource {

    @Inject
    ERezeptWorkflowService eRezeptWorkflowService;

    @Inject
    PrefillPrescriptionService prefillPrescriptionService;

    @Inject
    DocumentService documentService;

    private final FhirContext fhirContext = FHIRService.getFhirContext();
    IParser jsonParser = fhirContext.newJsonParser();
    IParser xmlParser = fhirContext.newXmlParser();

    @Context
    HttpServletRequest httpServletRequest;

    @Inject
    UserConfig userConfig;

    @POST
    @Path("task")
    public Response createERezeptTask(@HeaderParam("accept") String accept, @QueryParam("flowtype") String flowtype) {

	if(flowtype == null) {
	    flowtype = "160";
	}
        Task task = eRezeptWorkflowService.createERezeptTask(true, extractRuntimeConfigFromHeaders(httpServletRequest, userConfig), flowtype);
        if("application/xml".equals(accept)) {
            return Response.ok().entity(xmlParser.encodeResourceToString(task)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.ok().entity(jsonParser.encodeResourceToString(task)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    public static RuntimeConfig extractRuntimeConfigFromHeaders(HttpServletRequest httpServletRequest, UserConfig userConfig) {
        for(Object name : Collections.list(httpServletRequest.getHeaderNames())) {
            if(name.toString().startsWith("X-")) {
                RuntimeConfig runtimeConfig = new RuntimeConfig();
                runtimeConfig.copyValuesFromUserConfig(userConfig);
                runtimeConfig.updateConfigurationsWithHttpServletRequest(httpServletRequest);
                return runtimeConfig;
            }
        }
        return null;
    }

    @POST
    @Path("sign")
    public Response signBundleWithIdentifiers(@HeaderParam("Content-Type") String contentType, String bundle) throws DataFormatException, ERezeptWorkflowException {
        Bundle bundleObject = string2bundle(contentType, bundle);
        SignResponse signResponse = eRezeptWorkflowService.signBundleWithIdentifiers(bundleObject, false, extractRuntimeConfigFromHeaders(httpServletRequest, userConfig));
        String base64String = signResponse2base64String(signResponse);
        return Response.ok().entity(base64String).type(MediaType.TEXT_PLAIN).build();
    }

    static String signResponse2base64String(SignResponse signResponse) {
        return new String(Base64.getEncoder().encode(signResponse.getSignatureObject().getBase64Signature().getValue()));
    }

    Bundle string2bundle(String contentType, String bundle) {
        Bundle bundleObject = "application/xml".equals(contentType) ? xmlParser.parseResource(Bundle.class, bundle) : jsonParser.parseResource(Bundle.class, bundle);
        return bundleObject;
    }

    @POST
    @Path("batch-sign")
    public Response signBundlesWithIdentifiers(@HeaderParam("Content-Type") String contentType, String bundles) throws DataFormatException, ERezeptWorkflowException {
        List<Bundle> bundlesList = Arrays.asList(bundles.split("\\r?\\n")).stream().map((bundle) ->  string2bundle(contentType, bundle)).collect(Collectors.toList());
        List<SignResponse> signResponse = eRezeptWorkflowService.signBundleWithIdentifiers(bundlesList, false, extractRuntimeConfigFromHeaders(httpServletRequest, userConfig));
        String responses = signResponse.stream().map(ERezeptWorkflowResource::signResponse2base64String).collect(Collectors.joining("\n"));
        return Response.ok().entity(responses).type(MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("cards")
    public GetCardsResponse cards() {
        try {
            return eRezeptWorkflowService.getCards(extractRuntimeConfigFromHeaders(httpServletRequest, userConfig));
        } catch (FaultMessage e) {
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("update")
    public Response updateERezeptTask(UpdateERezept updateERezept) {
        eRezeptWorkflowService.updateERezeptTask(updateERezept.getTaskId(), updateERezept.getAccessCode(), Base64.getDecoder().decode(updateERezept.getSignedBytes()), extractRuntimeConfigFromHeaders(httpServletRequest, userConfig));
        return Response.ok().build();
    }

    @POST
    @Path("abort")
    public Response abortERezeptTask(AbortERezept abortERezept) {
        eRezeptWorkflowService.abortERezeptTask(extractRuntimeConfigFromHeaders(httpServletRequest, userConfig), abortERezept.getTaskId(), abortERezept.getAccessCode());
        return Response.noContent().build();
    }

    @POST
    @Path("comfortsignature/activate")
    public Response activate() {
        String userId = eRezeptWorkflowService.activateComfortSignature(extractRuntimeConfigFromHeaders(httpServletRequest, userConfig));
        return Response.ok(Entity.text(userId)).build();
    }

    @POST
    @Path("comfortsignature/deactivate")
    public Response deactivate() {
        eRezeptWorkflowService.deactivateComfortSignature(extractRuntimeConfigFromHeaders(httpServletRequest, userConfig));
        return Response.ok().build();
    }

    @GET
    @Path("comfortsignature/user-id")
    public Response getUserId() {
        return Response.ok(Entity.text(eRezeptWorkflowService.getUserIdForComfortSignature())).build();
    }

    @POST
    @Path("comfortsignature/user-id")
    public Response postUserId(String userId) {
        eRezeptWorkflowService.setUserIdForComfortSignature(userId);
        return Response.ok().build();
    }

    @GET
    @Path("idp-token")
    public String idpToken() {
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        eRezeptWorkflowService.requestNewAccessTokenIfNecessary(runtimeConfig, null, null);
        return eRezeptWorkflowService.getBearerToken(runtimeConfig);
    }

    @POST
    @Path("test-prescription")
    public Response testConfigurationsByCreatingTestPrescription() throws
            FaultMessage, de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage, InvalidNameException,
            CertificateEncodingException, IOException, CryptoException, ParseException, ERezeptWorkflowException,
            FOPException, TransformerException {

        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        Bundle bundle = prefillPrescriptionService.getTestPrescriptionBundle(runtimeConfig);

        Task task = eRezeptWorkflowService.createERezeptTask(true, runtimeConfig, "160");
        String taskId = null;
        String accessCode = null;
        for (Identifier identifier : task.getIdentifier()) {
            if (identifier.getSystem().equals("https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId")) {
                taskId = identifier.getValue();
            } else if (identifier.getSystem().equals("https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_AccessCode")) {
                accessCode = identifier.getValue();
            }
        };
        bundle.getIdentifier().setValue(taskId);
        SignResponse signResponse = eRezeptWorkflowService.signBundleWithIdentifiers(bundle, false, runtimeConfig);
        String base64String = signResponse2base64String(signResponse);

        eRezeptWorkflowService.updateERezeptTask(taskId, accessCode, Base64.getDecoder().decode(base64String), runtimeConfig);

        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = new BundleWithAccessCodeOrThrowable(bundle, accessCode);
        List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowableList = Arrays.asList(bundleWithAccessCodeOrThrowable);
        ByteArrayOutputStream baos = documentService.generateERezeptPdf(bundleWithAccessCodeOrThrowableList);
        return Response.ok().entity(baos.toByteArray()).type("application/pdf").build();
    }
}
