package health.ere.ps.service.gematik;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.parser.XMLParserException;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Task;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.v7.GetCards;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.signatureservice.v7.DocumentType;
import de.gematik.ws.conn.signatureservice.v7.SignRequest;
import de.gematik.ws.conn.signatureservice.v7.SignRequest.OptionalInputs;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;
import de.gematik.ws.conn.signatureservice.v7_5_5.ComfortSignatureStatusEnum;
import de.gematik.ws.conn.signatureservice.v7_5_5.SessionInfo;
import de.gematik.ws.conn.signatureservice.v7_5_5.SignatureModeEnum;
import de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.event.AbortTaskEntry;
import health.ere.ps.event.AbortTaskStatus;
import health.ere.ps.event.AbortTasksEvent;
import health.ere.ps.event.AbortTasksStatusEvent;
import health.ere.ps.event.ActivateComfortSignatureEvent;
import health.ere.ps.event.BundlesWithAccessCodeEvent;
import health.ere.ps.event.DeactivateComfortSignatureEvent;
import health.ere.ps.event.GetCardsEvent;
import health.ere.ps.event.GetCardsResponseEvent;
import health.ere.ps.event.GetSignatureModeEvent;
import health.ere.ps.event.GetSignatureModeResponseEvent;
import health.ere.ps.event.ReadyToSignBundlesEvent;
import health.ere.ps.event.SignAndUploadBundlesEvent;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.exception.gematik.ERezeptWorkflowException;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.fhir.FHIRService;
import health.ere.ps.vau.VAUEngine;
import health.ere.ps.websocket.ExceptionWithReplyToException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import jakarta.xml.ws.Holder;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;

@ApplicationScoped
public class ERezeptWorkflowService extends BearerTokenManageService {

    static final String EREZEPT_ACCESS_CODE_SYSTEM = "https://gematik.de/fhir/NamingSystem/AccessCode";
    static final String EREZEPT_ACCESS_CODE_SYSTEM_GEM = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_AccessCode";
    static final String EREZEPT_IDENTIFIER_SYSTEM = "https://gematik.de/fhir/NamingSystem/PrescriptionID";
    static final String EREZEPT_IDENTIFIER_SYSTEM_GEM = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId";
    private static final Logger log = Logger.getLogger(ERezeptWorkflowService.class.getName());
    private static final FhirContext fhirContext = FHIRService.getFhirContext();

    static {
        org.apache.xml.security.Init.init();
    }

    @Inject
    AppConfig appConfig;
    @Inject
    UserConfig userConfig;

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;
    @Inject
    ConnectorCardsService connectorCardsService;
    @Inject
    Event<BundlesWithAccessCodeEvent> bundlesWithAccessCodeEvent;
    @Inject
    Event<ReadyToSignBundlesEvent> readyToSignBundlesEvent;
    @Inject
    Event<Exception> exceptionEvent;
    @Inject
    Event<AbortTasksStatusEvent> abortTasksStatusEvent;
    @Inject
    Event<GetSignatureModeResponseEvent> getSignatureModeResponseEvent;
    @Inject
    Event<GetCardsResponseEvent> getCardsResponseEvent;

    private Client client;
    private String userIdForComfortSignature;


    public String getUserIdForComfortSignature() {
        return this.userIdForComfortSignature;
    }

    public void setUserIdForComfortSignature(String userIdForComfortSignature) {
        this.userIdForComfortSignature = userIdForComfortSignature;
    }

    /**
     * Extracts the access code from a task
     */
    static String getAccessCode(Task task) {
        return task.getIdentifier().stream()
            .filter(id -> id.getSystem().equals(EREZEPT_ACCESS_CODE_SYSTEM)
                || id.getSystem().equals(EREZEPT_ACCESS_CODE_SYSTEM_GEM)).findFirst()
            .orElse(new Identifier()).getValue();
    }

    @PostConstruct
    public void init() {
        client = initClientWithVAU(appConfig, exceptionEvent);
    }

    static Client initClientWithVAU(AppConfig appConfig) {
        return initClientWithVAU(appConfig, null);
    }

    static Client initClientWithVAU(AppConfig appConfig, Event<Exception> exceptionEvent) {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        if (appConfig.vauEnabled()) {
            try {
                ((ResteasyClientBuilderImpl) clientBuilder).httpEngine(new VAUEngine(appConfig.getPrescriptionServiceURL()));
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Could not enable VAU", ex);
                if (exceptionEvent != null) {
                    exceptionEvent.fireAsync(ex);
                }
            }
        }
        return clientBuilder.build();
    }

    /**
     * This function catches the sign and upload bundle events and does the
     * necessary processing
     */
    public void onSignAndUploadBundlesEvent(@ObservesAsync SignAndUploadBundlesEvent signAndUploadBundlesEvent) {
        requestNewAccessTokenIfNecessary(signAndUploadBundlesEvent.getRuntimeConfig(), signAndUploadBundlesEvent.getReplyTo(), signAndUploadBundlesEvent.getId());

        List<List<Bundle>> listOfListOfBundles = signAndUploadBundlesEvent.listOfListOfBundles;
        log.info(String.format("Received %d bundles to sign ", listOfListOfBundles.size()));
        log.info("Contents of list of bundles to sign are as follows:");
        listOfListOfBundles.forEach(bundlesList -> {
            log.info("Bundles list contents is:");
            bundlesList.forEach(bundle -> log.info("Bundle content: " + bundle.toString()));
        });

        List<List<BundleWithAccessCodeOrThrowable>> bundleWithAccessCodeOrThrowable = new ArrayList<>();
        List<Bundle> bundles = signAndUploadBundlesEvent.listOfListOfBundles.stream().flatMap(b -> b.stream()).collect(Collectors.toList());
        if (bundles.size() == 0) {
            log.warning("No bundles to sign. Returning.");
            exceptionEvent.fireAsync(new ArrayIndexOutOfBoundsException("No bundles to sign. Returning. Please see https://github.com/ere-health/ere-ps-app/blob/main/src/test/resources/websocket-messages/SignAndUploadBundles.json for an example message"));
            return;
        }
        log.info(String.format("Getting access codes for %d bundles.",
            bundles.size()));

        List<BundleWithAccessCodeOrThrowable> unflatten = createMultipleERezeptsOnPrescriptionServer(bundles, signAndUploadBundlesEvent.getFlowtype(), signAndUploadBundlesEvent.getRuntimeConfig(), signAndUploadBundlesEvent.getReplyTo(), signAndUploadBundlesEvent.getId());
        Iterator<BundleWithAccessCodeOrThrowable> it = unflatten.iterator();
        // unflatten bundles again
        for (int i = 0; i < listOfListOfBundles.size(); i++) {
            List<BundleWithAccessCodeOrThrowable> unflattenBundles = new ArrayList<>();
            for (int j = 0; j < listOfListOfBundles.get(i).size(); j++) {
                unflattenBundles.add(it.next());
            }
            bundleWithAccessCodeOrThrowable
                .add(unflattenBundles);
        }

        log.info(String.format("Firing event to create prescription receipts for %d bundles.",
            bundleWithAccessCodeOrThrowable.size()));
        BundlesWithAccessCodeEvent bundleWithAccessCode = new BundlesWithAccessCodeEvent(bundleWithAccessCodeOrThrowable, signAndUploadBundlesEvent.getReplyTo(), signAndUploadBundlesEvent.getId());
        if ("169".equals(signAndUploadBundlesEvent.getFlowtype())) {
            bundleWithAccessCode.setFlowtype(signAndUploadBundlesEvent.getFlowtype());
            bundleWithAccessCode.setToKimAddress(signAndUploadBundlesEvent.getToKimAddress());
            bundleWithAccessCode.setKimConfigMap(signAndUploadBundlesEvent.getKimConfigMap());
            bundleWithAccessCode.setNoteToPharmacy(signAndUploadBundlesEvent.getNoteToPharmacy());
        }
        bundlesWithAccessCodeEvent.fireAsync(bundleWithAccessCode);
    }


    public List<BundleWithAccessCodeOrThrowable> createMultipleERezeptsOnPrescriptionServer(List<Bundle> bundles) {
        return createMultipleERezeptsOnPrescriptionServer(bundles, null, null, null);
    }

    public List<BundleWithAccessCodeOrThrowable> createMultipleERezeptsOnPrescriptionServer(List<Bundle> bundles, RuntimeConfig runtimeConfig) {
        return createMultipleERezeptsOnPrescriptionServer(bundles, runtimeConfig, null, null);
    }

    /**
     * This function tries to create BundleWithAccessCodes for all given bundles.
     * <p>
     * When an error is thrown it create an object that contains this error.
     */
    public List<BundleWithAccessCodeOrThrowable> createMultipleERezeptsOnPrescriptionServer(List<Bundle> bundles, RuntimeConfig runtimeConfig, Session replyTo, String replyToMessageId) {
        return createMultipleERezeptsOnPrescriptionServer(bundles, "160", runtimeConfig, replyTo, replyToMessageId);
    }

    public List<BundleWithAccessCodeOrThrowable> createMultipleERezeptsOnPrescriptionServer(List<Bundle> bundles,
                                                                                            String flowtype, RuntimeConfig runtimeConfig, Session replyTo, String replyToMessageId) {
        List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodes = new ArrayList<>();
        List<Task> tasks = new ArrayList<>();
        for (Bundle bundle : bundles) {
            // Example: src/test/resources/gematik/Task-4711.xml
            try {
                try {
                    // We have a PKV prescription
                    if (flowtype.equals("160") &&
                        bundle.getEntry().stream().filter(be -> be.getResource() instanceof Coverage)
                            .map(be -> (Coverage) be.getResource())
                            .filter(c -> c.getType().getCoding().stream()
                                .filter(coding -> coding.getCode().equals("PKV")).count() > 0).count() > 0) {
                        flowtype = "200";
                    }
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Could not determine flowtype", ex);
                }
                Task task = createERezeptTask(true, runtimeConfig, flowtype);
                tasks.add(task);
                bundleWithAccessCodes.add(new BundleWithAccessCodeOrThrowable());
            } catch (Throwable t) {
                bundleWithAccessCodes.add(new BundleWithAccessCodeOrThrowable(t));
                tasks.add(null);
            }
        }
        int i = 0;
        for (Task task : tasks) {
            // Example:
            // src/test/resources/gematik/Bundle-4fe2013d-ae94-441a-a1b1-78236ae65680.xml
            if (task != null) {
                try {
                    BundleWithAccessCodeOrThrowable bundleWithAccessCode = updateBundleWithTask(task, bundles.get(i));
                    bundleWithAccessCodes.get(i).setBundle(bundleWithAccessCode.getBundle());
                    bundleWithAccessCodes.get(i).setAccessCode(bundleWithAccessCode.getAccessCode());
                } catch (Throwable t) {
                    bundleWithAccessCodes.get(i).setThrowable(t);
                }
            } else {
                if (bundleWithAccessCodes.get(i).getThrowable() == null) {
                    bundleWithAccessCodes.get(i).setThrowable(new ERezeptWorkflowException("Task is null please check log for errors."));
                }
            }
            i++;
        }
        try {
            List<SignResponse> signedDocuments = signBundleWithIdentifiers(bundles, false, runtimeConfig, replyTo, replyToMessageId);
            i = 0;
            for (SignResponse signedDocument : signedDocuments) {
                BundleWithAccessCodeOrThrowable bundleWithAccessCode = bundleWithAccessCodes.get(i);
                try {
                    Task task = tasks.get(i);
                    if (task != null) {
                        byte[] signedBundle = signedDocument.getSignatureObject().getBase64Signature().getValue();
                        bundleWithAccessCode.setSignedBundle(signedBundle);
                        updateERezeptTask(task, bundleWithAccessCode.getAccessCode(),
                            signedBundle, runtimeConfig);
                    }
                } catch (Throwable t) {
                    bundleWithAccessCode.setThrowable(t);
                }
                i++;
            }
        } catch (Throwable t) {
            bundleWithAccessCodes.stream().forEach(bundleWithAccessCode -> bundleWithAccessCode.setThrowable(t));
        }

        return bundleWithAccessCodes;
    }

    public BundleWithAccessCodeOrThrowable createERezeptOnPrescriptionServer(Bundle bundle)
        throws ERezeptWorkflowException {
        return createERezeptOnPrescriptionServer(bundle, null, null, null);
    }

    public BundleWithAccessCodeOrThrowable createERezeptOnPrescriptionServer(Bundle bundle, RuntimeConfig runtimeConfig)
        throws ERezeptWorkflowException {
        return createERezeptOnPrescriptionServer(bundle, runtimeConfig, null, null);
    }

    /**
     * A typical muster 16 form can contain up to 3 e prescriptions This function
     * has to be called multiple times
     * <p>
     * This function takes a bundle e.g.
     * https://github.com/ere-health/ere-ps-app/blob/main/src/test/resources/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml
     *
     * @return
     * @throws ERezeptWorkflowException
     */
    public BundleWithAccessCodeOrThrowable createERezeptOnPrescriptionServer(Bundle bundle, RuntimeConfig runtimeConfig, Session replyTo, String replyToMessageId)
        throws ERezeptWorkflowException {
        requestNewAccessTokenIfNecessary(runtimeConfig, replyTo, replyToMessageId);

        log.fine("Bearer Token: " + bearerToken.get(runtimeConfig));

        // Example: src/test/resources/gematik/Task-4711.xml
        Task task = createERezeptTask(runtimeConfig);

        // Example:
        // src/test/resources/gematik/Bundle-4fe2013d-ae94-441a-a1b1-78236ae65680.xml
        BundleWithAccessCodeOrThrowable bundleWithAccessCode = updateBundleWithTask(task, bundle);
        SignResponse signedDocument = signBundleWithIdentifiers(bundleWithAccessCode.getBundle(), runtimeConfig, replyTo, replyToMessageId);
        try {
            if (signedDocument == null) {
                bundleWithAccessCode.setThrowable(new RuntimeException("Could not get signed document. Please check the logs."));
            } else {
                byte[] signedBundle = signedDocument.getSignatureObject().getBase64Signature().getValue();
                bundleWithAccessCode.setSignedBundle(signedBundle);
                updateERezeptTask(task.getIdElement().getIdPart(), bundleWithAccessCode.getAccessCode(),
                    signedBundle, true, runtimeConfig, replyTo, replyToMessageId);
            }
        } catch (Exception e) {
            bundleWithAccessCode.setThrowable(e);
        }

        return bundleWithAccessCode;
    }

    public void updateERezeptTask(Task task, String accessCode, byte[] signedBytes) {
        updateERezeptTask(task.getIdElement().getIdPart(), accessCode, signedBytes, null);
    }

    public void updateERezeptTask(Task task, String accessCode, byte[] signedBytes, RuntimeConfig runtimeConfig) {
        updateERezeptTask(task.getIdElement().getIdPart(), accessCode, signedBytes, runtimeConfig);
    }

    public void updateERezeptTask(String taskId, String accessCode, byte[] signedBytes, RuntimeConfig runtimeConfig) {
        updateERezeptTask(taskId, accessCode, signedBytes, true, runtimeConfig);
    }

    public void updateERezeptTask(String taskId, String accessCode, byte[] signedBytes, boolean firstTry, RuntimeConfig runtimeConfig) {
        updateERezeptTask(taskId, accessCode, signedBytes, firstTry, runtimeConfig, null, null);
    }

    /**
     * This function adds the E-Rezept to the previously created task.
     */
    public void updateERezeptTask(String taskId, String accessCode, byte[] signedBytes, boolean firstTry, RuntimeConfig runtimeConfig, Session replyTo, String replyToMessageId) {
        requestNewAccessTokenIfNecessary(runtimeConfig, replyTo, replyToMessageId);
        Parameters parameters = new Parameters();
        ParametersParameterComponent ePrescriptionParameter = new ParametersParameterComponent();
        ePrescriptionParameter.setName("ePrescription");
        Binary binary = new Binary();
        binary.setContentType("application/pkcs7-mime");
        binary.setContent(signedBytes);
        ePrescriptionParameter.setResource(binary);
        parameters.addParameter(ePrescriptionParameter);

        try (Response response = client.target(appConfig.getPrescriptionServiceURL()).path("/Task")
            .path("/" + taskId).path("/$activate").request()
            .header("User-Agent", appConfig.getUserAgent())
            .header("Authorization", "Bearer " + bearerToken.get(runtimeConfig)).header("X-AccessCode", accessCode)
            .post(Entity.entity(fhirContext.newXmlParser().encodeResourceToString(parameters),
                "application/fhir+xml; charset=utf-8"))) {

            String taskString = new String(response.readEntity(java.io.InputStream.class).readAllBytes(), "ISO-8859-1");
            log.info("Response when trying to activate the task:" + taskString);

            if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                if (firstTry) {
                    log.warning("Was not able to $activate on first try. Status:" + response.getStatus() + " Response: " + taskString);
                    updateERezeptTask(taskId, accessCode, signedBytes, false, runtimeConfig, replyTo, replyToMessageId);
                } else {
                    Response.ResponseBuilder responseBuilder = Response.status(response.getStatus());
                    responseBuilder.entity(taskString);
                    Response responseError = responseBuilder.build();
                    throw new WebApplicationException(responseError);

                }
            }
            log.info("Task $activate Response: " + taskString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds the identifiers to the bundle.
     *
     * @param task
     * @param bundle
     */
    public static BundleWithAccessCodeOrThrowable updateBundleWithTask(Task task, Bundle bundle) {
        Identifier prescriptionID = getPrescriptionIdentifier(task);
        Identifier identifier = prescriptionID.copy();
        identifier.setUse(null);
        try {
            // This bundle uses the old KBV profile
            if (bundle.getMeta() != null && bundle.getMeta().getProfile() != null && bundle.getMeta().getProfile().stream().filter(pf -> pf.getValue().equals("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.2")).count() > 0) {
                identifier.setSystem(EREZEPT_IDENTIFIER_SYSTEM);
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Could not identify profile", ex);
        }
        bundle.setIdentifier(identifier);

        String accessCode = ERezeptWorkflowService.getAccessCode(task);
        return new BundleWithAccessCodeOrThrowable(bundle, accessCode);
    }

    static String getPrescriptionId(Task task) {
        String prescriptionID = getPrescriptionIdentifier(task).getValue();
        return prescriptionID;
    }

    static Identifier getPrescriptionIdentifier(Task task) {
        return task.getIdentifier().stream()
            .filter(id -> id.getSystem().equals(EREZEPT_IDENTIFIER_SYSTEM) ||
                id.getSystem().equals(ERezeptWorkflowService.EREZEPT_IDENTIFIER_SYSTEM_GEM)).findFirst().orElse(new Identifier());
    }

    public SignResponse signBundleWithIdentifiers(Bundle bundle) throws ERezeptWorkflowException {
        return signBundleWithIdentifiers(bundle, null, null, null);
    }

    public SignResponse signBundleWithIdentifiers(Bundle bundle, RuntimeConfig runtimeConfig, Session replyTo, String replyToMessageId) throws ERezeptWorkflowException {
        return signBundleWithIdentifiers(bundle, false, runtimeConfig, replyTo, replyToMessageId);
    }

    public SignResponse signBundleWithIdentifiers(Bundle bundle, boolean wait10secondsAfterJobNumber) throws ERezeptWorkflowException {
        return signBundleWithIdentifiers(bundle, wait10secondsAfterJobNumber, null);
    }

    public SignResponse signBundleWithIdentifiers(Bundle bundle, boolean wait10secondsAfterJobNumber, RuntimeConfig runtimeConfig) throws ERezeptWorkflowException {
        return signBundleWithIdentifiers(bundle, wait10secondsAfterJobNumber, runtimeConfig, null, null);
    }

    public SignResponse signBundleWithIdentifiers(Bundle bundle, boolean wait10secondsAfterJobNumber, RuntimeConfig runtimeConfig, Session replyTo, String replyToMessageId)
        throws ERezeptWorkflowException {
        return signBundleWithIdentifiers(Arrays.asList(bundle), wait10secondsAfterJobNumber, runtimeConfig, replyTo, replyToMessageId).get(0);
    }

    public List<SignResponse> signBundleWithIdentifiers(List<Bundle> bundles, boolean wait10secondsAfterJobNumber)
        throws ERezeptWorkflowException {
        return signBundleWithIdentifiers(bundles, wait10secondsAfterJobNumber, null);
    }

    public List<SignResponse> signBundleWithIdentifiers(List<Bundle> bundles, boolean wait10secondsAfterJobNumber, RuntimeConfig runtimeConfig)
        throws ERezeptWorkflowException {
        return signBundleWithIdentifiers(bundles, wait10secondsAfterJobNumber, runtimeConfig, null, null);
    }


    public List<SignResponse> signBundleWithIdentifiers(List<Bundle> bundles, boolean wait10secondsAfterJobNumber, RuntimeConfig runtimeConfig, Session replyTo, String replyToMessageId)
        throws ERezeptWorkflowException {
        return signBundleWithIdentifiers(bundles, wait10secondsAfterJobNumber, runtimeConfig, replyTo, replyToMessageId, true);
    }

    /**
     * This function signs the bundle with the signatureService.signDocument from
     * the connector.
     *
     * @return
     * @throws ERezeptWorkflowException
     */
    public List<SignResponse> signBundleWithIdentifiers(List<Bundle> bundles, boolean wait10secondsAfterJobNumber, RuntimeConfig runtimeConfig, Session replyTo, String replyToMessageId, boolean firstTry)
        throws ERezeptWorkflowException {

        List<SignResponse> signResponses = null;

        if (runtimeConfig != null && runtimeConfig.isSendPreview()) {
            readyToSignBundlesEvent.fireAsync(new ReadyToSignBundlesEvent(bundles, replyTo, replyToMessageId));
        }

        try {
            OptionalInputs optionalInputs = new OptionalInputs();
            optionalInputs.setSignatureType("urn:ietf:rfc:5652");
            optionalInputs.setIncludeEContent(true);

            List<SignRequest> signRequests = bundles.stream().map(bundle -> {
                byte[] canonXmlBytes;
                try {
                    canonXmlBytes = getCanonicalXmlBytes(bundle);
                } catch (InvalidCanonicalizerException | XMLParserException | CanonicalizationException
                         | IOException e) {
                    log.log(Level.SEVERE, "Could not get canonical XML", e);
                    exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, replyTo, replyToMessageId));
                    return null;
                }
                SignRequest signRequest = new SignRequest();
                DocumentType document = new DocumentType();
                document.setShortText("E-Rezept");
                Base64Data base64Data = new Base64Data();
                base64Data.setMimeType("text/plain; charset=utf-8");
                base64Data.setValue(canonXmlBytes);
                document.setBase64Data(base64Data);
                signRequest.setOptionalInputs(optionalInputs);
                signRequest.setRequestID(UUID.randomUUID().toString());
                signRequest.setDocument(document);
                signRequest.setIncludeRevocationInfo(appConfig.includeRevocationInfoEnabled());
                return signRequest;
            }).collect(Collectors.toList());

            if (wait10secondsAfterJobNumber) {
                // Wait 10 seconds to start titus test case
                log.info(
                    "Waiting 10 seconds. Please enable titus test case on https://frontend.titus.ti-dienste.de/#/erezept/vps/testsuiterun");
                try {
                    Thread.sleep(1000 * 10);
                } catch (InterruptedException e) {
                    log.log(Level.SEVERE, "Could not wait", e);
                }
            }
            String signatureServiceCardHandle = getSignatureServiceCardHandle(runtimeConfig);
            if ("PTV4+".equals(runtimeConfig != null && runtimeConfig.getConnectorVersion() != null ? runtimeConfig.getConnectorVersion() : userConfig.getConnectorVersion())) {
                List<de.gematik.ws.conn.signatureservice.v7_5_5.SignRequest> signRequestsV755 = signRequests.stream().map(signRequest -> {
                    de.gematik.ws.conn.signatureservice.v7_5_5.SignRequest signRequestV755 = new de.gematik.ws.conn.signatureservice.v7_5_5.SignRequest();
                    de.gematik.ws.conn.signatureservice.v7_5_5.SignRequest.OptionalInputs optionalInputsC755 = new de.gematik.ws.conn.signatureservice.v7_5_5.SignRequest.OptionalInputs();
                    optionalInputsC755.setSignatureType(optionalInputs.getSignatureType());
                    optionalInputsC755.setIncludeEContent(optionalInputs.isIncludeEContent());
                    signRequestV755.setOptionalInputs(optionalInputsC755);
                    signRequestV755.setRequestID(UUID.randomUUID().toString());
                    de.gematik.ws.conn.signatureservice.v7_5_5.DocumentType documentV755 = new de.gematik.ws.conn.signatureservice.v7_5_5.DocumentType();
                    documentV755.setBase64Data(signRequest.getDocument().getBase64Data());
                    documentV755.setShortText(signRequest.getDocument().getShortText());
                    signRequestV755.setDocument(documentV755);
                    signRequestV755.setIncludeRevocationInfo(signRequest.isIncludeRevocationInfo());
                    return signRequestV755;
                }).collect(Collectors.toList());

                List<de.gematik.ws.conn.signatureservice.v7_5_5.SignResponse> signResponsesV755;
                ContextType contextType = connectorServicesProvider.getContextType(runtimeConfig);
                if (runtimeConfig != null && runtimeConfig.getUserId() != null && !"".equals(runtimeConfig.getUserId())) {
                    contextType.setUserId(runtimeConfig.getUserId());
                } else {
                    if (userIdForComfortSignature != null) {
                        contextType.setUserId(userIdForComfortSignature);
                    }
                }
                // if we don't have a user id, generate one
                if (contextType.getUserId() == null || "".equals(contextType.getUserId())) {
                    contextType.setUserId(UUID.randomUUID().toString());
                }
                if (appConfig.enableBatchSign()) {
                    String jobNumber = connectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig).getJobNumber(connectorServicesProvider.getContextType(runtimeConfig));

                    signResponsesV755 = connectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig).signDocument(signatureServiceCardHandle,
                        appConfig.getConnectorCrypt(), contextType, (runtimeConfig != null && runtimeConfig.getTvMode() != null) ? runtimeConfig.getTvMode() : userConfig.getTvMode(),
                        jobNumber, signRequestsV755);
                } else {
                    signResponsesV755 = signRequestsV755.stream().map(signRequestV755 -> {
                        String jobNumber;
                        try {
                            jobNumber = connectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig).getJobNumber(connectorServicesProvider.getContextType(runtimeConfig));

                            List<de.gematik.ws.conn.signatureservice.v7_5_5.SignResponse> list = connectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig).signDocument(signatureServiceCardHandle,
                                appConfig.getConnectorCrypt(), contextType, (runtimeConfig != null && runtimeConfig.getTvMode() != null) ? runtimeConfig.getTvMode() : userConfig.getTvMode(),
                                jobNumber, Arrays.asList(signRequestV755));
                            return list.get(0);
                        } catch (FaultMessage e) {
                            exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, replyTo, replyToMessageId));
                            return null;
                        }
                    }).collect(Collectors.toList());
                }

                List<SignResponse> signResponses744 = signResponsesV755.stream().map(signResponseV755 -> {
                    if (signResponseV755 != null) {
                        SignResponse signResponse744 = new SignResponse();
                        signResponse744.setSignatureObject(signResponseV755.getSignatureObject());
                        signResponse744.setStatus(signResponseV755.getStatus());
                        return signResponse744;
                    }
                    return null;
                }).collect(Collectors.toList());

                signResponses = signResponses744;
                // PTV4, could be PTV3 as well, to be refactored in a future task
            } else {
                if (appConfig.enableBatchSign()) {
                    signResponses = connectorServicesProvider.getSignatureServicePortType(runtimeConfig).signDocument(signatureServiceCardHandle,
                        connectorServicesProvider.getContextType(runtimeConfig), (runtimeConfig != null && runtimeConfig.getTvMode() != null) ? runtimeConfig.getTvMode() : userConfig.getTvMode(),
                        connectorServicesProvider.getSignatureServicePortType(runtimeConfig).getJobNumber(connectorServicesProvider.getContextType(runtimeConfig)), signRequests);
                } else {
                    signResponses = signRequests.stream().map(signRequest -> {
                        List<SignResponse> list;
                        try {
                            list = connectorServicesProvider.getSignatureServicePortType(runtimeConfig).signDocument(signatureServiceCardHandle,
                                connectorServicesProvider.getContextType(runtimeConfig), (runtimeConfig != null && runtimeConfig.getTvMode() != null) ? runtimeConfig.getTvMode() : userConfig.getTvMode(),
                                connectorServicesProvider.getSignatureServicePortType(runtimeConfig).getJobNumber(connectorServicesProvider.getContextType(runtimeConfig)), Arrays.asList(signRequest));
                        } catch (FaultMessage e) {
                            exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, replyTo, replyToMessageId));
                            return null;
                        }
                        return list.get(0);
                    }).collect(Collectors.toList());
                }
            }
        } catch (ConnectorCardsException | FaultMessage e) {
            if (firstTry) {
                log.log(Level.WARNING, "Exception signing bundles with identifiers on first try.", e);
                return signBundleWithIdentifiers(bundles, wait10secondsAfterJobNumber, runtimeConfig, replyTo, replyToMessageId, false);
            } else {
                throw new ERezeptWorkflowException("Exception signing bundles with identifiers.", e);
            }
        }

        if (appConfig.isWriteSignatureFile()) {
            String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ssX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
            for (int i = 0; i < signResponses.size(); i++) {
                byte[] sig = signResponses.get(i).getSignatureObject().getBase64Signature().getValue();
                try {
                    Path path = Paths.get(thisMoment + "-" + i + ".p7s");
                    log.info("Generating " + path.toAbsolutePath().toString());
                    Files.write(path, sig);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Could not generate signature files", e);
                }
            }
        }
        return signResponses;
    }

    /**
     * Gets the canonical XML for the bundle using ALGO_ID_C14N11_OMIT_COMMENTS.
     *
     * @param bundle
     * @return
     * @throws InvalidCanonicalizerException
     * @throws XMLParserException
     * @throws IOException
     * @throws CanonicalizationException
     */
    public static byte[] getCanonicalXmlBytes(Bundle bundle)
        throws InvalidCanonicalizerException, XMLParserException, IOException, CanonicalizationException {
        String bundleXml = fhirContext.newXmlParser().encodeResourceToString(bundle);

        log.fine(bundleXml);

        Canonicalizer canon = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N11_OMIT_COMMENTS);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        canon.canonicalize(bundleXml.getBytes("UTF-8"), baos, false);
        byte[] canonXmlBytes = baos.toByteArray();

        String canonicalByteString = new String(canonXmlBytes);
        log.fine("Canonical: " + canonicalByteString);
        return canonXmlBytes;
    }

    public Task createERezeptTask() {
        return createERezeptTask(null);
    }

    public Task createERezeptTask(RuntimeConfig runtimeConfig) {
        return createERezeptTask(true, runtimeConfig);
    }

    /**
     * This function creates an empty task based on workflow 160 (Muster 16) on the
     * prescription server.
     *
     * @return
     */
    public Task createERezeptTask(boolean firstTry, RuntimeConfig runtimeConfig) {
        return createERezeptTask(firstTry, runtimeConfig, "160");
    }

    /**
     * This function creates an empty task based on workflow 160 (Muster 16) on the
     * prescription server.
     *
     * @return
     */
    public Task createERezeptTask(boolean firstTry, RuntimeConfig runtimeConfig, String flowtype) {
        requestNewAccessTokenIfNecessary(runtimeConfig, null, null);
        // https://github.com/gematik/api-erp/blob/master/docs/erp_bereitstellen.adoc#e-rezept-erstellen
        // POST to https://prescriptionserver.telematik/Task/$create

        Parameters parameters = new Parameters();
        ParametersParameterComponent workflowTypeParameter = new ParametersParameterComponent();
        workflowTypeParameter.setName("workflowType");
        Coding valueCoding = (Coding) workflowTypeParameter.addChild("valueCoding");
        valueCoding.setSystem("https://gematik.de/fhir/erp/CodeSystem/GEM_ERP_CS_FlowType");
        valueCoding.setCode(flowtype);
        parameters.addParameter(workflowTypeParameter);

        String parameterString = fhirContext.newXmlParser().encodeResourceToString(parameters);
        log.fine("Parameter String: " + parameterString);

        try (Response response = client.target(appConfig.getPrescriptionServiceURL()).path("/Task/$create").request()
            .header("User-Agent", appConfig.getUserAgent())
            .header("Authorization", "Bearer " + bearerToken.get(runtimeConfig))
            .post(Entity.entity(parameterString, "application/fhir+xml; charset=utf-8"))) {

            String taskString = response.readEntity(String.class);

            // if this was the first try, try again, this will request a new bearer token
            if (firstTry && response.getStatus() == 401) {
                log.warning("401 when trying to create e prescription. Retrying.");
                return createERezeptTask(false, runtimeConfig, flowtype);
            }

            if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                Response.ResponseBuilder responseBuilder = Response.status(response.getStatus());
                responseBuilder.entity(taskString);
                throw new WebApplicationException(responseBuilder.build());
            }
            log.info("Task Response: " + taskString);
            return fhirContext.newXmlParser().parseResource(Task.class, new StringReader(taskString));
        }
    }

    public void abortERezeptTask(String taskId, String accessCode) {
        abortERezeptTask(null, taskId, accessCode);
    }

    /**
     * This function creates an empty task based on workflow 160 (Muster 16) on the
     * prescription server.
     *
     * @return
     */
    public void abortERezeptTask(RuntimeConfig runtimeConfig, String taskId, String accessCode) {
        requestNewAccessTokenIfNecessary(runtimeConfig, null, null);
        try (Response response = client.target(appConfig.getPrescriptionServiceURL()).path("/Task").path("/" + taskId).path("/$abort")
            .request().header("User-Agent", appConfig.getUserAgent()).header("Authorization", "Bearer " + bearerToken.get(runtimeConfig)).header("X-AccessCode", accessCode)
            .post(Entity.entity("", "application/fhir+xml; charset=utf-8"))) {
            String taskString = response.readEntity(String.class);
            // if it is not successful
            if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                Response.ResponseBuilder responseBuilder = Response.status(response.getStatus());
                responseBuilder.entity(taskString);
                throw new WebApplicationException(responseBuilder.build());
            }

            log.info("Task $abort Response: " + taskString);
        }
    }

    /**
     * Is executed when an abortTasksEvent is received
     *
     * @param abortTasksEvent event that contains the task to abort
     */
    public void onAbortTasksEvent(@ObservesAsync AbortTasksEvent abortTasksEvent) {
        requestNewAccessTokenIfNecessary(abortTasksEvent.getRuntimeConfig(), abortTasksEvent.getReplyTo(), abortTasksEvent.getId());
        List<AbortTaskStatus> abortTaskStatusList = new ArrayList<>();
        for (AbortTaskEntry abortTaskEntry : abortTasksEvent.getTasks()) {
            AbortTaskStatus abortTaskStatus = new AbortTaskStatus(abortTaskEntry);

            try {
                abortERezeptTask(abortTasksEvent.getRuntimeConfig(), abortTaskEntry.getId(), abortTaskEntry.getAccessCode());
                abortTaskStatus.setStatus(AbortTaskStatus.Status.OK);
            } catch (Throwable t) {
                abortTaskStatus.setThrowable(t);
                abortTaskStatus.setStatus(AbortTaskStatus.Status.ERROR);
            }

            abortTaskStatusList.add(abortTaskStatus);
        }
        abortTasksStatusEvent.fireAsync(new AbortTasksStatusEvent(abortTaskStatusList, abortTasksEvent.getReplyTo(), abortTasksEvent.getId()));
    }

    /**
     * Reacts to the event the ActivateComfortSignatureEvent
     */
    public void onActivateComfortSignatureEvent(@ObservesAsync ActivateComfortSignatureEvent activateComfortSignatureEvent) {
        String userId = activateComfortSignature(activateComfortSignatureEvent.getRuntimeConfig(), activateComfortSignatureEvent.getReplyTo(), activateComfortSignatureEvent.getId());
        onGetSignatureModeEvent(new GetSignatureModeEvent(activateComfortSignatureEvent.getReplyTo(), activateComfortSignatureEvent.getId()), userId, true);
    }


    public String activateComfortSignature() {
        return activateComfortSignature(null);
    }

    /**
     * Activate comfort signature
     */
    public String activateComfortSignature(RuntimeConfig runtimeConfig) {
        return activateComfortSignature(runtimeConfig, null, null);
    }

    /**
     * Activate comfort signature
     */
    public String activateComfortSignature(RuntimeConfig runtimeConfig, Session replyTo, String replyToMessageId) {
        final Holder<Status> status = new Holder<>();
        final Holder<SignatureModeEnum> signatureMode = new Holder<>();
        String signatureServiceCardHandle = null;

        try {
            userIdForComfortSignature = UUID.randomUUID().toString();
            ContextType contextType = connectorServicesProvider.getContextType(runtimeConfig);
            contextType.setUserId(userIdForComfortSignature);
            signatureServiceCardHandle = getSignatureServiceCardHandle(runtimeConfig);
            connectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig).activateComfortSignature(signatureServiceCardHandle, contextType,
                status, signatureMode);
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not enable comfort signature", e);
            exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, replyTo, replyToMessageId));
        }
        return userIdForComfortSignature;
    }

    private String getSignatureServiceCardHandle(RuntimeConfig runtimeConfig) throws ConnectorCardsException {
        String signatureServiceCardHandle;
        signatureServiceCardHandle = (runtimeConfig != null && runtimeConfig.getEHBAHandle() != null) ? runtimeConfig.getEHBAHandle() : connectorCardsService.getConnectorCardHandle(
            ConnectorCardsService.CardHandleType.HBA, runtimeConfig);
        return signatureServiceCardHandle;
    }

    public void onGetSignatureModeEvent(@ObservesAsync GetSignatureModeEvent getSignatureModeEvent) {
        onGetSignatureModeEvent(getSignatureModeEvent, null);
    }

    public void onGetSignatureModeEvent(GetSignatureModeEvent getSignatureModeEvent, String userId) {
        onGetSignatureModeEvent(getSignatureModeEvent, userId, false);
    }

    /**
     * Reacts to the event the GetSignatureMode Event
     */
    public void onGetSignatureModeEvent(GetSignatureModeEvent getSignatureModeEvent, String userId, boolean answertToActivateComfortSignature) {
        try {
            GetSignatureModeResponseEvent getSignatureModeResponseEvent = getSignatureMode(getSignatureModeEvent.getRuntimeConfig(), getSignatureModeEvent.getReplyTo(), getSignatureModeEvent.getId());
            if (getSignatureModeResponseEvent != null) {
                if (getSignatureModeEvent != null) {
                    getSignatureModeResponseEvent.setReplyTo(getSignatureModeEvent.getReplyTo());
                    getSignatureModeResponseEvent.setReplyToMessageId(getSignatureModeEvent.getId());
                }
                getSignatureModeResponseEvent.setUserId(userId != null ? userId : (getSignatureModeEvent.getRuntimeConfig() != null ? getSignatureModeEvent.getRuntimeConfig().getUserId() : null));
                getSignatureModeResponseEvent.setAnswertToActivateComfortSignature(answertToActivateComfortSignature);
                this.getSignatureModeResponseEvent.fireAsync(getSignatureModeResponseEvent);
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not GetSignatureModeEvent", e);
            exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, getSignatureModeEvent.getReplyTo(), getSignatureModeEvent.getReplyToMessageId()));
        }
    }

    public GetSignatureModeResponseEvent getSignatureMode() {
        return getSignatureMode(null, null, null);
    }

    /**
     *
     */
    public GetSignatureModeResponseEvent getSignatureMode(RuntimeConfig runtimeConfig, Session replyTo, String replyToMessageId) {
        if (userIdForComfortSignature == null && (runtimeConfig == null || (runtimeConfig.getUserId() == null || runtimeConfig.getUserId().isEmpty()))) {
            Status status = new Status();
            status.setResult("OK");
            ComfortSignatureStatusEnum comfortSignatureStatus = ComfortSignatureStatusEnum.DISABLED;
            // comfort signature not activated
            try {
                return new GetSignatureModeResponseEvent(status, comfortSignatureStatus, 0, DatatypeFactory.newInstance().newDuration(0l), null);
            } catch (DatatypeConfigurationException e) {
                log.log(Level.WARNING, "Could not generate Duration", e);
                exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, replyTo, replyToMessageId));
                return null;
            }
        }
        Holder<Status> status = new Holder<>();
        Holder<ComfortSignatureStatusEnum> comfortSignatureStatus = new Holder<>();
        Holder<Integer> comfortSignatureMax = new Holder<>();
        Holder<javax.xml.datatype.Duration> comfortSignatureTimer = new Holder<>();
        Holder<SessionInfo> sessionInfo = new Holder<>();

        String signatureServiceCardHandle;
        try {
            signatureServiceCardHandle = getSignatureServiceCardHandle(runtimeConfig);
            ;
            ContextType contextType = connectorServicesProvider.getContextType(runtimeConfig);
            if (runtimeConfig != null && runtimeConfig.getUserId() != null && !"".equals(runtimeConfig.getUserId())) {
                contextType.setUserId(runtimeConfig.getUserId());
            } else {
                contextType.setUserId(userIdForComfortSignature);
            }
            connectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig).getSignatureMode(signatureServiceCardHandle, contextType, status, comfortSignatureStatus,
                comfortSignatureMax, comfortSignatureTimer, sessionInfo);
            return new GetSignatureModeResponseEvent(status.value, comfortSignatureStatus.value, comfortSignatureMax.value, comfortSignatureTimer.value, sessionInfo.value);
        } catch (ConnectorCardsException | FaultMessage e) {
            log.log(Level.WARNING, "Could not get signature signature", e);
            exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, replyTo, replyToMessageId));
            return null;
        }
    }

    /**
     * Reacts to the event the DeactivateComfortSignatureEvent
     */
    public void onDeactivateComfortSignatureEvent(@ObservesAsync DeactivateComfortSignatureEvent deactivateComfortSignatureEvent) {
        deactivateComfortSignature(deactivateComfortSignatureEvent.getRuntimeConfig(), deactivateComfortSignatureEvent.getReplyTo(), deactivateComfortSignatureEvent.getId());
        onGetSignatureModeEvent(new GetSignatureModeEvent(deactivateComfortSignatureEvent.getReplyTo(), deactivateComfortSignatureEvent.getId()));
    }

    public void deactivateComfortSignature() {
        deactivateComfortSignature(null, null, null);
    }

    public void deactivateComfortSignature(RuntimeConfig runtimeConfig) {
        deactivateComfortSignature(runtimeConfig, null, null);
    }

    /**
     *
     */
    public void deactivateComfortSignature(RuntimeConfig runtimeConfig, Session replyTo, String replyToMessageId) {
        String signatureServiceCardHandle = null;
        try {
            signatureServiceCardHandle = getSignatureServiceCardHandle(runtimeConfig);
            ContextType contextType = connectorServicesProvider.getContextType(runtimeConfig);
            if (runtimeConfig != null && runtimeConfig.getUserId() != null && !"".equals(runtimeConfig.getUserId())) {
                contextType.setUserId(runtimeConfig.getUserId());
            } else {
                contextType.setUserId(userIdForComfortSignature);
            }

            connectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig).deactivateComfortSignature(Arrays.asList(signatureServiceCardHandle));
            userIdForComfortSignature = null;
        } catch (ConnectorCardsException | FaultMessage e) {
            log.log(Level.WARNING, "Could not deactivate comfort signature", e);
            exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, replyTo, replyToMessageId));
        }
    }

    public void onGetCardsEvent(@ObservesAsync GetCardsEvent getCardsEvent) {
        try {
            GetCardsResponse getCardsResponse = getCards(getCardsEvent.getRuntimeConfig());
            getCardsResponseEvent.fireAsync(new GetCardsResponseEvent(getCardsResponse, getCardsEvent.getReplyTo(), getCardsEvent.getId()));
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not get cards", e);
            exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, getCardsEvent.getReplyTo(), getCardsEvent.getId()));
        }
    }

    public GetCardsResponse getCards() throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        return getCards(null);
    }

    /**
     * @throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage
     */
    public GetCardsResponse getCards(RuntimeConfig runtimeConfig) throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        GetCards parameter = new GetCards();
        parameter.setContext(connectorServicesProvider.getContextType(runtimeConfig));
        EventServicePortType eventServicePortType = connectorServicesProvider.getEventServicePortType(runtimeConfig); // todo: if runtimeconfig without userid/hba/smcb: connectorBaseURL ignored ! request to http://ti-konnektor/eventservice
        if (eventServicePortType == null) {
            throw new RuntimeException("EventServicePortType is null. This normally means that the connector configuration is not correct.");
        } else {
            return eventServicePortType.getCards(parameter);
        }
    }

    /**
     * Checks if ERezeptService is reachable
     */
    public boolean isERezeptServiceReachable(RuntimeConfig runtimeConfig, String parameterBearerToken) {
        String prescriptionServiceURL = (runtimeConfig != null && runtimeConfig.getPrescriptionServerURL() != null)
            ? runtimeConfig.getPrescriptionServerURL()
            : appConfig.getPrescriptionServiceURL();
        if (prescriptionServiceURL == null) {
            return false;
        }
        // use a http client that does not use a VAU implementation
        Client testClient = ClientBuilder.newBuilder().readTimeout(3, TimeUnit.SECONDS).connectTimeout(3, TimeUnit.SECONDS).build();
        try (Response response = testClient
            .target(prescriptionServiceURL)
            .request()
            .header("User-Agent", appConfig.getUserAgent())
            .header("Authorization", "Bearer " + parameterBearerToken)
            .get()
        ) {
            return response.getStatus() == Response.Status.OK.getStatusCode();
        } catch (Exception ex) {
            return false;
        }
    }
}
