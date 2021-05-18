package health.ere.ps.service.gematik;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.xml.datatype.Duration;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.BindingProvider;
import javax.ws.rs.core.Response;

import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.parser.XMLParserException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7.ComfortSignatureStatusEnum;
import de.gematik.ws.conn.signatureservice.v7.DocumentType;
import de.gematik.ws.conn.signatureservice.v7.SessionInfo;
import de.gematik.ws.conn.signatureservice.v7.SignRequest;
import de.gematik.ws.conn.signatureservice.v7.SignRequest.OptionalInputs;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;
import de.gematik.ws.conn.signatureservice.v7.SignatureModeEnum;
import de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureService;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortType;
import health.ere.ps.event.BundlesWithAccessCodeEvent;
import health.ere.ps.event.SignAndUploadBundlesEvent;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import de.gematik.ws.conn.eventservice.v7.GetCards;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventService;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;

@ApplicationScoped
public class ERezeptWorkflowService {

    private static Logger log = Logger.getLogger(ERezeptWorkflowService.class.getName());

    FhirContext fhirContext = FhirContext.forR4();

    @ConfigProperty(name = "prescriptionserver.url", defaultValue = "")
    String prescriptionserverUrl;

    @ConfigProperty(name = "event-service.endpointAddress", defaultValue = "")
    String eventServiceEndpointAddress;

    @ConfigProperty(name = "signature-service.endpointAddress", defaultValue = "")
    String signatureServiceEndpointAddress;

    @ConfigProperty(name = "signature-service.cardHandle", defaultValue = "")
    String signatureServiceCardHandle;

    @ConfigProperty(name = "signature-service.crypt", defaultValue = "")
    String signatureServiceCrypt;

    @ConfigProperty(name = "signature-service.context.mandantId", defaultValue = "")
    String signatureServiceContextMandantId;

    @ConfigProperty(name = "signature-service.context.clientSystemId", defaultValue = "")
    String signatureServiceContextClientSystemId;

    @ConfigProperty(name = "signature-service.context.workplaceId", defaultValue = "")
    String signatureServiceContextWorkplaceId;

    @ConfigProperty(name = "signature-service.context.userId", defaultValue = "")
    String signatureServiceContextUserId;

    @ConfigProperty(name = "signature-service.tvMode", defaultValue = "")
    String signatureServiceTvMode;

    SignatureServicePortType signatureService;
    EventServicePortType eventService;

    @Inject
    Event<BundlesWithAccessCodeEvent> bundlesWithAccessCodeEvent;

    SSLContext customSSLContext = null;

    public static final String EREZEPT_IDENTIFIER_SYSTEM = "https://gematik.de/fhir/NamingSystem/PrescriptionID";

    static {
        org.apache.xml.security.Init.init();
    }

    @PostConstruct
    public void init() {
        signatureService = new SignatureService().getSignatureServicePort();
        /* Set endpoint to configured endpoint */
        BindingProvider bp = (BindingProvider)signatureService;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, signatureServiceEndpointAddress);
        if(customSSLContext != null) {
            bp.getRequestContext().put("com.sun.xml.ws.transport.https.client.SSLSocketFactory", customSSLContext.getSocketFactory());
        }
        
        eventService = new EventService().getEventServicePort();
        /* Set endpoint to configured endpoint */
        bp = (BindingProvider)eventService;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, eventServiceEndpointAddress);
        if(customSSLContext != null) {
            bp.getRequestContext().put("com.sun.xml.ws.transport.https.client.SSLSocketFactory", customSSLContext.getSocketFactory());
        }
    }


    /**
     * This function catches the sign and upload bundle events and does the necessary processing
     * @param signAndUploadBundlesEvent
     */
    public void onSignAndUploadBundlesEvent(@ObservesAsync SignAndUploadBundlesEvent signAndUploadBundlesEvent) {
        List<List<BundleWithAccessCodeOrThrowable>> bundleWithAccessCodeOrThrowable = new ArrayList<>();
        for(List<Bundle> bundles : signAndUploadBundlesEvent.listOfListOfBundles) {
            bundleWithAccessCodeOrThrowable.add(createMultipleERezeptsOnPrescriptionServer(signAndUploadBundlesEvent.bearerToken, bundles));
        }
        bundlesWithAccessCodeEvent.fireAsync(new BundlesWithAccessCodeEvent(bundleWithAccessCodeOrThrowable));
    }

    public List<BundleWithAccessCodeOrThrowable> createMultipleERezeptsOnPrescriptionServer(String bearerToken, List<Bundle> bundles) {
        return createMultipleERezeptsOnPrescriptionServer(bearerToken, bundles, false);
    }

    /**
     * This function tries to create BundleWithAccessCodes for all given bundles.
     * 
     * When an error is thrown it create an object that contains this error.
     */
    public List<BundleWithAccessCodeOrThrowable> createMultipleERezeptsOnPrescriptionServer(String bearerToken, List<Bundle> bundles, boolean comfortSignature) {
        List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodes = new ArrayList<>();
        try {
            if(comfortSignature) {
                this.activateComfortSignature();
            }
            for(Bundle bundle : bundles) {
                try {
                    bundleWithAccessCodes.add(createERezeptOnPrescriptionServer(bearerToken, bundle));
                } catch(Throwable t) {
                    bundleWithAccessCodes.add(new BundleWithAccessCodeOrThrowable(t));
                }
            }
            if(comfortSignature) {
                this.deactivateComfortSignature();
            }
        } catch (Throwable t) {
            bundleWithAccessCodes.add(new BundleWithAccessCodeOrThrowable(t));
        }
        return bundleWithAccessCodes;
    }

    /**
     * A typical muster 16 form can contain up to 3 e prescriptions This function
     * has to be called multiple times
     * 
     * This function takes a bundle e.g.
     * https://github.com/ere-health/ere-ps-app/blob/main/src/test/resources/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml
     * 
     * @param bundle
     * @return
     * @throws IOException
     * @throws FaultMessage
     * @throws CanonicalizationException
     * @throws XMLParserException
     * @throws InvalidCanonicalizerException
     */
    public BundleWithAccessCodeOrThrowable createERezeptOnPrescriptionServer(String bearerToken, Bundle bundle)
            throws InvalidCanonicalizerException, XMLParserException, CanonicalizationException, FaultMessage,
            IOException {

        log.fine("Bearer Token: " + bearerToken);

        // Example: src/test/resources/gematik/Task-4711.xml
        Task task = createERezeptTask(bearerToken);

        // Example:
        // src/test/resources/gematik/Bundle-4fe2013d-ae94-441a-a1b1-78236ae65680.xml
        BundleWithAccessCodeOrThrowable bundleWithAccessCode = updateBundleWithTask(task, bundle);
        SignResponse signedDocument = signBundleWithIdentifiers(bundleWithAccessCode.bundle);

        updateERezeptTask(bearerToken, task, bundleWithAccessCode.accessCode, signedDocument.getSignatureObject().getBase64Signature().getValue());

        return bundleWithAccessCode;
    }

    /**
     * This function adds the E-Rezept to the previously created task.
     * 
     * @param task
     * @param signedDocument
     */
    public void updateERezeptTask(String bearerToken, Task task, String accessCode,
            byte[] signedBytes) {
        Client client = ClientBuilder.newBuilder().build();

        Parameters parameters = new Parameters();
        ParametersParameterComponent ePrescriptionParameter = new ParametersParameterComponent();
        ePrescriptionParameter.setName("ePrescription");
        Binary binary = new Binary();
        binary.setContentType("application/pkcs7-mime");
        binary.setContent(signedBytes);
        ePrescriptionParameter.setResource(binary);
        parameters.addParameter(ePrescriptionParameter);
        String s = client.target(prescriptionserverUrl).path("/Task").path("/" + task.getId()).path("/$activate")
                .request().header("Authorization", "Bearer " + bearerToken)
                .header("X-AccessCode", accessCode)
                .post(Entity.entity(parameters, "application/fhir+xml; charset=UTF-8")).readEntity(String.class);
        log.fine(s);
    }

    /**
     * Adds the identifiers to the bundle.
     * 
     * @param task
     * @param bundle
     */
    public BundleWithAccessCodeOrThrowable updateBundleWithTask(Task task, Bundle bundle) {
        String prescriptionID = task.getIdentifier().stream().filter(id -> id.getSystem().equals(EREZEPT_IDENTIFIER_SYSTEM))
                .findFirst().get().getValue();
        Identifier identifier = new Identifier();
        identifier.setSystem(EREZEPT_IDENTIFIER_SYSTEM);
        identifier.setValue(prescriptionID);
        bundle.setIdentifier(identifier);

        String accessCode = ERezeptWorkflowService.getAccessCode(task);
        return new BundleWithAccessCodeOrThrowable(bundle, accessCode);
    }

    /**
     * Extracts the access code from a task
     */
    static String getAccessCode(Task task) {
        return task.getIdentifier().stream()
                .filter(id -> id.getSystem().equals("https://gematik.de/fhir/NamingSystem/AccessCode")).findFirst()
                .get().getValue();
    }

    public SignResponse signBundleWithIdentifiers(Bundle bundle) throws FaultMessage, InvalidCanonicalizerException,
            XMLParserException, CanonicalizationException, IOException {
                return signBundleWithIdentifiers(bundle, true);
    }

    /**
     * This function signs the bundle with the signatureService.signDocument from
     * the connector.
     * 
     * @return
     * @throws FaultMessage
     * @throws InvalidCanonicalizerException
     * @throws IOException
     * @throws CanonicalizationException
     * @throws XMLParserException
     */
    public SignResponse signBundleWithIdentifiers(Bundle bundle, boolean requestJobNumber) throws FaultMessage, InvalidCanonicalizerException,
            XMLParserException, CanonicalizationException, IOException {

        String bundleXml = fhirContext.newXmlParser().encodeResourceToString(bundle);

        log.fine(bundleXml);

        Canonicalizer canon = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N11_OMIT_COMMENTS);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        canon.canonicalize(bundleXml.getBytes(), baos, false);
        byte[] canonXmlBytes = baos.toByteArray();


        log.fine("Canonical: "+new String(canonXmlBytes));

        SignRequest signRequest = new SignRequest();
        DocumentType document = new DocumentType();
        Base64Data base64Data = new Base64Data();
        base64Data.setMimeType("text/plain; charset=utf-8");
        base64Data.setValue(canonXmlBytes);
        document.setBase64Data(base64Data);
        OptionalInputs optionalInputs = new OptionalInputs();
        optionalInputs.setSignatureType("urn:ietf:rfc:5652");
        optionalInputs.setIncludeEContent(true);
        signRequest.setRequestID(UUID.randomUUID().toString());
        signRequest.setDocument(document);
        signRequest.setIncludeRevocationInfo(true);
        List<SignRequest> signRequests = Arrays.asList(signRequest);

        ContextType contextType =createContextType();

        String jobNumber = requestJobNumber ? signatureService.getJobNumber(contextType) : "KON-001";

        List<SignResponse> signResponse = signatureService.signDocument(signatureServiceCardHandle,
                signatureServiceCrypt, contextType, signatureServiceTvMode, jobNumber, signRequests);
        return signResponse.get(0);
    }

    /**
     * Create a context type.
     */
    ContextType createContextType() {
        ContextType contextType = new ContextType();
        contextType.setMandantId(signatureServiceContextMandantId);
        contextType.setClientSystemId(signatureServiceContextClientSystemId);
        contextType.setWorkplaceId(signatureServiceContextWorkplaceId);
        contextType.setUserId(signatureServiceContextUserId);
        return contextType;
    }

    /**
     * This function creates an empty task based on workflow 160 (Muster 16) on the
     * prescription server.
     * 
     * @return
     */
    public Task createERezeptTask(String bearerToken) {

        // https://github.com/gematik/api-erp/blob/master/docs/erp_bereitstellen.adoc#e-rezept-erstellen
        // POST to https://prescriptionserver.telematik/Task/$create

        Parameters parameters = new Parameters();
        ParametersParameterComponent workflowTypeParameter = new ParametersParameterComponent();
        workflowTypeParameter.setName("workflowType");
        Coding valueCoding = (Coding) workflowTypeParameter.addChild("valueCoding");
        valueCoding.setSystem("https://gematik.de/fhir/CodeSystem/Flowtype");
        valueCoding.setCode("160");
        parameters.addParameter(workflowTypeParameter);

        Client client = ClientBuilder.newBuilder().build();

        String parameterString = fhirContext.newXmlParser().encodeResourceToString(parameters);
        log.fine("Parameter String: " + parameterString);

        Response response = client.target(prescriptionserverUrl).path("/Task/$create").request()
                .header("Authorization", "Bearer " + bearerToken)
                .post(Entity.entity(parameterString, "application/fhir+xml; charset=UTF-8"));

        String taskString = response.readEntity(String.class);
        if(Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
            // OperationOutcome operationOutcome = fhirContext.newXmlParser().parseResource(OperationOutcome.class, new StringReader(taskString));
            throw new RuntimeException(taskString);
        }

        log.info("Task Response: " + taskString);
        Task task = fhirContext.newXmlParser().parseResource(Task.class, new StringReader(taskString));
        return task;
    }

    /**
     * This function creates an empty task based on workflow 160 (Muster 16) on the
     * prescription server.
     * 
     * @return
     */
    public void abortERezeptTask(String bearerToken, BundleWithAccessCodeOrThrowable bundleWithAccessCode) {
        String prescriptionID = bundleWithAccessCode.bundle.getIdentifier().getValue();
        Client client = ClientBuilder.newBuilder().build();
        String s = client.target(prescriptionserverUrl).path("/Task").path("/" + prescriptionID).path("/$abort")
                .request().header("Authorization", "Bearer " + bearerToken)
                .header("X-AccessCode", bundleWithAccessCode.accessCode)
                .post(Entity.entity("", "application/fhir+xml; charset=UTF-8")).readEntity(String.class);
        log.fine(s);
    }

    /**
     * @throws FaultMessage
     */
    public void activateComfortSignature() throws FaultMessage {
        final Holder<Status> status = new Holder<>();
        final Holder<SignatureModeEnum> signatureMode = new Holder<>();
        ContextType contextType =createContextType();
        signatureService.activateComfortSignature(signatureServiceCardHandle, contextType, status, signatureMode);
    }

    /**
     * @throws FaultMessage
     */
    public void getSignatureMode() throws FaultMessage {
        Holder<Status> status = new Holder<>();
        Holder<ComfortSignatureStatusEnum> comfortSignatureStatus = new Holder<>();
        Holder<Integer> comfortSignatureMax = new Holder<>();
        Holder<Duration> comfortSignatureTimer = new Holder<>();
        Holder<SessionInfo> sessionInfo = new Holder<>();
        ContextType contextType = createContextType();
        signatureService.getSignatureMode(signatureServiceCardHandle, contextType, status, comfortSignatureStatus, comfortSignatureMax, comfortSignatureTimer, sessionInfo);
    }

    /**
     * @throws FaultMessage
     */
    public void deactivateComfortSignature() throws FaultMessage {
        signatureService.deactivateComfortSignature(Arrays.asList(signatureServiceCardHandle));
    }

    /**
     * @throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage
     */
    public GetCardsResponse getCards() throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        GetCards parameter = new GetCards();
        parameter.setContext(createContextType());
        return eventService.getCards(parameter);
    }

}
