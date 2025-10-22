package health.ere.ps.resource.gematik;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.event.GetSignatureModeResponseEvent;
import health.ere.ps.exception.gematik.ERezeptWorkflowException;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.service.fhir.FHIRService;
import health.ere.ps.service.gematik.ERezeptWorkflowService;
import health.ere.ps.service.gematik.PrefillPrescriptionService;
import health.ere.ps.service.pdf.DocumentService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import org.apache.fop.apps.FOPException;
import org.bouncycastle.crypto.CryptoException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Task;

import javax.naming.InvalidNameException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static health.ere.ps.resource.gematik.Extractors.extractRuntimeConfigFromHeaders;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/workflow")
public class ERezeptWorkflowResource {

    private static final Logger log = Logger.getLogger(ERezeptWorkflowResource.class.getName());

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
    public Response createERezeptTask(
        @HeaderParam("accept") String accept,
        @QueryParam("flowtype") String flowtype
    ) {
        if (flowtype == null) {
            flowtype = "160";
        }
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        Task task = eRezeptWorkflowService.createERezeptTask(true, runtimeConfig, flowtype);
        if (APPLICATION_XML.equals(accept)) {
            return Response.ok().entity(xmlParser.encodeResourceToString(task)).type(APPLICATION_XML).build();
        } else {
            return Response.ok().entity(jsonParser.encodeResourceToString(task)).type(APPLICATION_JSON).build();
        }
    }

    @POST
    @Path("sign")
    public Response signBundleWithIdentifiers(
        @HeaderParam("Content-Type") String contentType,
        String bundle
    ) throws DataFormatException, ERezeptWorkflowException {
        Bundle bundleObject = string2bundle(contentType, bundle);
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        SignResponse signResponse = eRezeptWorkflowService.signBundleWithIdentifiers(bundleObject, false, runtimeConfig, null, null);
        String base64String = signResponse2base64String(signResponse);
        return Response.ok().entity(base64String).type(TEXT_PLAIN).build();
    }

    static String signResponse2base64String(SignResponse signResponse) {
        return new String(Base64.getEncoder().encode(signResponse.getSignatureObject().getBase64Signature().getValue()));
    }

    Bundle string2bundle(String contentType, String bundle) {
        IParser parser = APPLICATION_XML.equals(contentType) ? xmlParser : jsonParser;
        return parser.parseResource(Bundle.class, bundle);
    }

    @POST
    @Path("batch-sign")
    public Response signBundlesWithIdentifiers(@HeaderParam("Content-Type") String contentType, String bundles) throws DataFormatException, ERezeptWorkflowException {
        List<Bundle> bundlesList = Arrays.asList(bundles.split("\\r?\\n")).stream().map((bundle) -> string2bundle(contentType, bundle)).collect(Collectors.toList());
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        List<SignResponse> signResponse = eRezeptWorkflowService.signBundleWithIdentifiers(
            bundlesList, false, runtimeConfig, null, null, true
        );
        String responses = signResponse.stream().map(ERezeptWorkflowResource::signResponse2base64String).collect(Collectors.joining("\n"));
        return Response.ok().entity(responses).type(TEXT_PLAIN).build();
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
        String taskId = updateERezept.getTaskId();
        String accessCode = updateERezept.getAccessCode();
        byte[] signedBytes = Base64.getDecoder().decode(updateERezept.getSignedBytes());
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        eRezeptWorkflowService.updateERezeptTask(taskId, accessCode, signedBytes, true, runtimeConfig, null, null);
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

    @GET
    @Path("signature-mode")
    public GetSignatureModeResponseEvent signatureMode() {
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        return eRezeptWorkflowService.getSignatureMode(runtimeConfig, null, null);
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
        }
        bundle.getIdentifier().setValue(taskId);

        SignResponse signResponse;
        try {
            signResponse = eRezeptWorkflowService.signBundleWithIdentifiers(bundle, false, runtimeConfig, null, null);
        } catch (ERezeptWorkflowException e) {
            throw new WebApplicationException(e);
        }
        String base64String = signResponse2base64String(signResponse);

        byte[] signedBytes = Base64.getDecoder().decode(base64String);
        eRezeptWorkflowService.updateERezeptTask(taskId, accessCode, signedBytes, true, runtimeConfig, null, null);

        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = new BundleWithAccessCodeOrThrowable(accessCode);
        try {
            bundleWithAccessCodeOrThrowable.setBundle(bundle);
        } catch (Throwable t) {
            log.log(Level.WARNING, "Could not extract taskId and/or medicationRequest Id from Bundle", t);
        }

        List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowableList = List.of(bundleWithAccessCodeOrThrowable);
        ByteArrayOutputStream os = documentService.generateERezeptPdf(bundleWithAccessCodeOrThrowableList);
        return Response.ok().entity(os.toByteArray()).type("application/pdf").build();
    }
}