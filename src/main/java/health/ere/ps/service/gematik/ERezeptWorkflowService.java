package health.ere.ps.service.gematik;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.xml.ws.Holder;

import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.parser.XMLParserException;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Task;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.eventservice.v7.GetCards;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.signatureservice.v7.DocumentType;
import de.gematik.ws.conn.signatureservice.v7.SignRequest;
import de.gematik.ws.conn.signatureservice.v7.SignRequest.OptionalInputs;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;
import de.gematik.ws.conn.signatureservice.v7_5_5.ComfortSignatureStatusEnum;
import de.gematik.ws.conn.signatureservice.v7_5_5.SessionInfo;
import de.gematik.ws.conn.signatureservice.v7_5_5.SignatureModeEnum;
import de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.event.AbortTaskEntry;
import health.ere.ps.event.AbortTaskStatus;
import health.ere.ps.event.AbortTasksEvent;
import health.ere.ps.event.AbortTasksStatusEvent;
import health.ere.ps.event.ActivateComfortSignatureEvent;
import health.ere.ps.event.BundlesWithAccessCodeEvent;
import health.ere.ps.event.DeactivateComfortSignatureEvent;
import health.ere.ps.event.GetSignatureModeEvent;
import health.ere.ps.event.GetSignatureModeResponseEvent;
import health.ere.ps.event.ReadyToSignBundlesEvent;
import health.ere.ps.event.SignAndUploadBundlesEvent;
import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.exception.gematik.ERezeptWorkflowException;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.provider.ConnectorServicesProvider;
import health.ere.ps.service.idp.BearerTokenService;
import health.ere.ps.vau.VAUEngine;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;

@ApplicationScoped
public class ERezeptWorkflowService {

    private static final String EREZEPT_IDENTIFIER_SYSTEM = "https://gematik.de/fhir/NamingSystem/PrescriptionID";
    private static final Logger log = Logger.getLogger(ERezeptWorkflowService.class.getName());
    private static final FhirContext fhirContext = FhirContext.forR4();

    static {
        org.apache.xml.security.Init.init();
    }

    @Inject
    AppConfig appConfig;
    @Inject
    UserConfig userConfig;

    @Inject
    ConnectorServicesProvider connectorServicesProvider;
    @Inject
    ConnectorCardsService connectorCardsService;
    @Inject
    Event<BundlesWithAccessCodeEvent> bundlesWithAccessCodeEvent;
    @Inject
    Event<ReadyToSignBundlesEvent> readyToSignBundlesEvent;
    @Inject
    Event<Exception> exceptionEvent;
    @Inject
    BearerTokenService bearerTokenService;
    @Inject
    Event<AbortTasksStatusEvent> abortTasksStatusEvent;
    @Inject
    Event<GetSignatureModeResponseEvent> getSignatureModeResponseEvent;

    private Client client;
    //In the future it should be managed automatically by the webclient, including its renewal
    private String bearerToken;

    /**
     * Extracts the access code from a task
     */
    static String getAccessCode(Task task) {
        return task.getIdentifier().stream()
                .filter(id -> id.getSystem().equals("https://gematik.de/fhir/NamingSystem/AccessCode")).findFirst()
                .orElse(new Identifier()).getValue();
    }

    @PostConstruct
    public void init() throws SecretsManagerException {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        if (appConfig.vauEnabled()) {
            try {
                ((ResteasyClientBuilderImpl) clientBuilder).httpEngine(new VAUEngine(appConfig.getPrescriptionServiceURL()));
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Could not enable VAU", ex);
                exceptionEvent.fireAsync(ex);
            }
        }
        client = clientBuilder.build();
    }

    /**
     * This function catches the sign and upload bundle events and does the
     * necessary processing
     */
    public void onSignAndUploadBundlesEvent(@ObservesAsync SignAndUploadBundlesEvent signAndUploadBundlesEvent) {
        requestNewAccessTokenIfNecessary();

        log.info(String.format("Received %d bundles to sign ", signAndUploadBundlesEvent.listOfListOfBundles.size()));
        log.info("Contents of list of bundles to sign are as follows:");
        signAndUploadBundlesEvent.listOfListOfBundles.forEach(bundlesList -> {
            log.info("Bundles list contents is:");
            bundlesList.forEach(bundle -> log.info("Bundle content: " + bundle.toString()));
        });

        List<List<BundleWithAccessCodeOrThrowable>> bundleWithAccessCodeOrThrowable = new ArrayList<>();
        for (List<Bundle> bundles : signAndUploadBundlesEvent.listOfListOfBundles) {
            log.info(String.format("Getting access codes for %d bundles.",
                    bundles.size()));
            bundleWithAccessCodeOrThrowable
                    .add(createMultipleERezeptsOnPrescriptionServer(bearerToken, bundles));
        }

        log.info(String.format("Firing event to create prescription receipts for %d bundles.",
                bundleWithAccessCodeOrThrowable.size()));
        bundlesWithAccessCodeEvent.fireAsync(new BundlesWithAccessCodeEvent(bundleWithAccessCodeOrThrowable));
    }

    public List<BundleWithAccessCodeOrThrowable> createMultipleERezeptsOnPrescriptionServer(String bearerToken, List<Bundle> bundles) {
        return createMultipleERezeptsOnPrescriptionServer(bearerToken, bundles, false);
    }

    /**
     * This function tries to create BundleWithAccessCodes for all given bundles.
     * <p>
     * When an error is thrown it create an object that contains this error.
     */
    public List<BundleWithAccessCodeOrThrowable> createMultipleERezeptsOnPrescriptionServer(String bearerToken, List<Bundle> bundles, boolean comfortSignature) {
        List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodes = new ArrayList<>();
        List<Task> tasks = new ArrayList<>();
        for (Bundle bundle : bundles) {
            // Example: src/test/resources/gematik/Task-4711.xml
            try {
                Task task = createERezeptTask(bearerToken);
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
            if(task != null) {
                try {
                    BundleWithAccessCodeOrThrowable bundleWithAccessCode = updateBundleWithTask(task, bundles.get(i));
                    bundleWithAccessCodes.get(i).setBundle(bundleWithAccessCode.getBundle());
                    bundleWithAccessCodes.get(i).setAccessCode(bundleWithAccessCode.getAccessCode());
                } catch(Throwable t) {
                    bundleWithAccessCodes.get(i).setThrowable(t);
                }
            } else {
                bundleWithAccessCodes.get(i).setThrowable(new ERezeptWorkflowException("Task is null please check log for errors."));
            }
            i++;
        }
        try {
            List<SignResponse> signedDocuments = signBundleWithIdentifiers(bundles, false);
            i = 0;
            for(SignResponse signedDocument : signedDocuments) {
                BundleWithAccessCodeOrThrowable bundleWithAccessCode = bundleWithAccessCodes.get(i);
                try {
                    Task task = tasks.get(i);
                    if(task != null) {
                        updateERezeptTask(bearerToken, task, bundleWithAccessCode.getAccessCode(),
                            signedDocument.getSignatureObject().getBase64Signature().getValue());
                    }
                } catch(Throwable t) {
                    bundleWithAccessCode.setThrowable(t);
                }
                i++;
            }
        } catch(Throwable t) {
            bundleWithAccessCodes.stream().forEach(bundleWithAccessCode -> bundleWithAccessCode.setThrowable(t));
        }

        return bundleWithAccessCodes;
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
    public BundleWithAccessCodeOrThrowable createERezeptOnPrescriptionServer(String bearerToken, Bundle bundle)
            throws ERezeptWorkflowException {
        log.fine("Bearer Token: " + bearerToken);

        // Example: src/test/resources/gematik/Task-4711.xml
        Task task = createERezeptTask(bearerToken);

        // Example:
        // src/test/resources/gematik/Bundle-4fe2013d-ae94-441a-a1b1-78236ae65680.xml
        BundleWithAccessCodeOrThrowable bundleWithAccessCode = updateBundleWithTask(task, bundle);
        SignResponse signedDocument = signBundleWithIdentifiers(bundleWithAccessCode.getBundle());

        updateERezeptTask(bearerToken, task, bundleWithAccessCode.getAccessCode(),
                signedDocument.getSignatureObject().getBase64Signature().getValue());

        return bundleWithAccessCode;
    }

    /**
     * This function adds the E-Rezept to the previously created task.
     */
    public void updateERezeptTask(String bearerToken, Task task, String accessCode, byte[] signedBytes) {
        Parameters parameters = new Parameters();
        ParametersParameterComponent ePrescriptionParameter = new ParametersParameterComponent();
        ePrescriptionParameter.setName("ePrescription");
        Binary binary = new Binary();
        binary.setContentType("application/pkcs7-mime");
        binary.setContent(signedBytes);
        ePrescriptionParameter.setResource(binary);
        parameters.addParameter(ePrescriptionParameter);

        Response response = client.target(appConfig.getPrescriptionServiceURL()).path("/Task")
                .path("/" + task.getIdElement().getIdPart()).path("/$activate").request()
                .header("User-Agent", appConfig.getUserAgent())
                .header("Authorization", "Bearer " + bearerToken).header("X-AccessCode", accessCode)
                .post(Entity.entity(fhirContext.newXmlParser().encodeResourceToString(parameters),
                        "application/fhir+xml; charset=utf-8"));

        String taskString = response.readEntity(String.class);
        log.info("Response when trying to activate the task:" + taskString);

        if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
            // OperationOutcome operationOutcome =
            // fhirContext.newXmlParser().parseResource(OperationOutcome.class, new
            // StringReader(taskString));
            response.close();
            throw new RuntimeException(taskString);
        }
        response.close();
        log.info("Task $activate Response: " + taskString);
    }

    /**
     * Adds the identifiers to the bundle.
     *
     * @param task
     * @param bundle
     */
    public BundleWithAccessCodeOrThrowable updateBundleWithTask(Task task, Bundle bundle) {
        String prescriptionID = task.getIdentifier().stream()
                .filter(id -> id.getSystem().equals(EREZEPT_IDENTIFIER_SYSTEM)).findFirst().orElse(new Identifier()).getValue();
        Identifier identifier = new Identifier();
        identifier.setSystem(EREZEPT_IDENTIFIER_SYSTEM);
        identifier.setValue(prescriptionID);
        bundle.setIdentifier(identifier);

        String accessCode = ERezeptWorkflowService.getAccessCode(task);
        return new BundleWithAccessCodeOrThrowable(bundle, accessCode);
    }

    public SignResponse signBundleWithIdentifiers(Bundle bundle) throws ERezeptWorkflowException {
        return signBundleWithIdentifiers(bundle, false);
    }


    public SignResponse signBundleWithIdentifiers(Bundle bundle, boolean wait10secondsAfterJobNumber)
            throws ERezeptWorkflowException {
        return signBundleWithIdentifiers(Arrays.asList(bundle), wait10secondsAfterJobNumber).get(0);
    }
    /**
     * This function signs the bundle with the signatureService.signDocument from
     * the connector.
     *
     * @return
     * @throws ERezeptWorkflowException
     */
    public List<SignResponse> signBundleWithIdentifiers(List<Bundle> bundles, boolean wait10secondsAfterJobNumber)
            throws ERezeptWorkflowException {

        List<SignResponse> signResponses = null;

        readyToSignBundlesEvent.fireAsync(new ReadyToSignBundlesEvent(bundles));

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
                    exceptionEvent.fireAsync(e);
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
                signRequest.setIncludeRevocationInfo(true);
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
            String signatureServiceCardHandle = connectorCardsService.getConnectorCardHandle(
                    ConnectorCardsService.CardHandleType.HBA);
            if ("PTV4+".equals(userConfig.getConnectorVersion())) {
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
                if(appConfig.enableBatchSign()) {
                    String jobNumber = connectorServicesProvider.getSignatureServicePortTypeV755().getJobNumber(connectorServicesProvider.getContextType());

                    signResponsesV755 = connectorServicesProvider.getSignatureServicePortTypeV755().signDocument(signatureServiceCardHandle,
                            appConfig.getConnectorCrypt(), connectorServicesProvider.getContextType(), userConfig.getTvMode(),
                            jobNumber, signRequestsV755);
                } else {
                    signResponsesV755 = signRequestsV755.stream().map(signRequestV755 -> {
                        String jobNumber;
                        try {
                            jobNumber = connectorServicesProvider.getSignatureServicePortTypeV755().getJobNumber(connectorServicesProvider.getContextType());
                            
                            List<de.gematik.ws.conn.signatureservice.v7_5_5.SignResponse> list = connectorServicesProvider.getSignatureServicePortTypeV755().signDocument(signatureServiceCardHandle,
                            appConfig.getConnectorCrypt(), connectorServicesProvider.getContextType(), userConfig.getTvMode(),
                            jobNumber, Arrays.asList(signRequestV755));
                            return list.get(0);
                        } catch (FaultMessage e) {
                            exceptionEvent.fireAsync(e);
                            return null;
                        }
                    }).collect(Collectors.toList());
                }

                List<SignResponse> signResponses744 = signResponsesV755.stream().map(signResponseV755 -> {
                    SignResponse signResponse744 = new SignResponse();
                    signResponse744.setSignatureObject(signResponseV755.getSignatureObject());
                    signResponse744.setStatus(signResponseV755.getStatus());
                    return signResponse744;
                }).collect(Collectors.toList());

                return signResponses744;
                // PTV4, could be PTV3 as well, to be refactored in a future task
            } else {
                if(appConfig.enableBatchSign()) {
                    signResponses = connectorServicesProvider.getSignatureServicePortType().signDocument(signatureServiceCardHandle,
                            connectorServicesProvider.getContextType(), userConfig.getTvMode(),
                            connectorServicesProvider.getSignatureServicePortType().getJobNumber(connectorServicesProvider.getContextType()), signRequests);
                 } else {
                    signResponses = signRequests.stream().map(signRequest-> {
                        List<SignResponse> list;
                        try {
                            list = connectorServicesProvider.getSignatureServicePortType().signDocument(signatureServiceCardHandle,
                            connectorServicesProvider.getContextType(), userConfig.getTvMode(),
                            connectorServicesProvider.getSignatureServicePortType().getJobNumber(connectorServicesProvider.getContextType()), Arrays.asList(signRequest));
                        } catch (FaultMessage e) {
                            exceptionEvent.fireAsync(e);
                            return null;
                        }
                        return list.get(0);
                    }).collect(Collectors.toList());
                 } 
            }
        } catch (ConnectorCardsException | FaultMessage e) {
            throw new ERezeptWorkflowException("Exception signing bundles with identifiers.", e);
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

        String parameterString = fhirContext.newXmlParser().encodeResourceToString(parameters);
        log.fine("Parameter String: " + parameterString);

        Response response = client.target(appConfig.getPrescriptionServiceURL()).path("/Task/$create").request()
                .header("User-Agent", appConfig.getUserAgent())
                .header("Authorization", "Bearer " + bearerToken)
                .post(Entity.entity(parameterString, "application/fhir+xml; charset=utf-8"));

        String taskString = response.readEntity(String.class);
        if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
            // OperationOutcome operationOutcome =
            // fhirContext.newXmlParser().parseResource(OperationOutcome.class, new
            // StringReader(taskString));
            response.close();
            throw new RuntimeException(taskString);
        }
        response.close();
        log.info("Task Response: " + taskString);
        return fhirContext.newXmlParser().parseResource(Task.class, new StringReader(taskString));
    }

    /**
     * This function creates an empty task based on workflow 160 (Muster 16) on the
     * prescription server.
     *
     * @return
     */
    public void abortERezeptTask(String bearerToken, String taskId, String accessCode) {
        Response response = client.target(appConfig.getPrescriptionServiceURL()).path("/Task").path("/" + taskId).path("/$abort")
        .request().header("User-Agent", appConfig.getUserAgent()).header("Authorization", "Bearer " + bearerToken).header("X-AccessCode", accessCode)
                .post(Entity.entity("", "application/fhir+xml; charset=utf-8"));
        String taskString = response.readEntity(String.class);
        // if it is not successful and it was found
        if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL
        && response.getStatus() != Response.Status.NOT_FOUND.getStatusCode()) {
            response.close();
            throw new RuntimeException(taskString);
        }
        response.close();
        
        log.info("Task $abort Response: " + taskString);
    }
    
    /**
     * Requests a new bearerToken if the current one is expired
     */
    public void requestNewAccessTokenIfNecessary() {
        if (StringUtils.isEmpty(bearerToken) || isExpired(bearerToken)) {
            bearerToken = bearerTokenService.requestBearerToken();
        }
    }

    public String getBearerToken() {
        return bearerToken;
    }
    
    /**
     * Checks if the given bearer token is expired.
     * @param bearerToken2 the bearer token to check
     */
    boolean isExpired(String bearerToken2) {
        JwtConsumer consumer = new JwtConsumerBuilder()
            .setDisableRequireSignature()
            .setSkipSignatureVerification()
            .setSkipDefaultAudienceValidation()
            .setRequireExpirationTime()
            .build();
        try {
            consumer.process(bearerToken2);
            return false;
        } catch (InvalidJwtException e) {
            return true;
        }
    }

    /**
     * Is executed when an abortTasksEvent is received
     * @param abortTasksEvent event that contains the task to abort
     */
    public void onAbortTasksEvent(@ObservesAsync AbortTasksEvent abortTasksEvent) {
        requestNewAccessTokenIfNecessary();
        List<AbortTaskStatus> abortTaskStatusList = new ArrayList<>();
        for (AbortTaskEntry abortTaskEntry : abortTasksEvent.getTasks()) {
            AbortTaskStatus abortTaskStatus = new AbortTaskStatus(abortTaskEntry);

            try {
                abortERezeptTask(bearerToken, abortTaskEntry.getId(), abortTaskEntry.getAccessCode());
                abortTaskStatus.setStatus(AbortTaskStatus.Status.OK);
            } catch (Throwable t) {
                abortTaskStatus.setThrowable(t);
                abortTaskStatus.setStatus(AbortTaskStatus.Status.ERROR);
            }

            abortTaskStatusList.add(abortTaskStatus);
        }
        abortTasksStatusEvent.fireAsync(new AbortTasksStatusEvent(abortTaskStatusList));
    }

    /**
     * Reacts to the event the ActivateComfortSignatureEvent
     */
    public void onActivateComfortSignatureEvent(@ObservesAsync ActivateComfortSignatureEvent activateComfortSignatureEvent) {
        activateComfortSignature();
        onGetSignatureModeEvent(null);
    }

    /**
     * Activate comfort signature
     */
    public void activateComfortSignature() {
        final Holder<Status> status = new Holder<>();
        final Holder<SignatureModeEnum> signatureMode = new Holder<>();
        String signatureServiceCardHandle = null;

        try {
            signatureServiceCardHandle = connectorCardsService.getConnectorCardHandle(
                    ConnectorCardsService.CardHandleType.HBA);
            connectorServicesProvider.getSignatureServicePortTypeV755().activateComfortSignature(signatureServiceCardHandle, connectorServicesProvider.getContextType(),
                    status, signatureMode);
        } catch (ConnectorCardsException | FaultMessage e) {
            log.log(Level.WARNING, "Could not enable comfort signature", e);
            exceptionEvent.fire(e);
        }
    }

    /**
     * Reacts to the event the GetSignatureMode Event
     */
    public void onGetSignatureModeEvent(@ObservesAsync GetSignatureModeEvent getSignatureModeEvent) {
        GetSignatureModeResponseEvent getSignatureModeResponseEvent = getSignatureMode();
        if(getSignatureModeResponseEvent != null) {
            this.getSignatureModeResponseEvent.fireAsync(getSignatureModeResponseEvent);
        }
    }

    /**
     *
     */
    public GetSignatureModeResponseEvent getSignatureMode() {
        Holder<Status> status = new Holder<>();
        Holder<ComfortSignatureStatusEnum> comfortSignatureStatus = new Holder<>();
        Holder<Integer> comfortSignatureMax = new Holder<>();
        Holder<javax.xml.datatype.Duration> comfortSignatureTimer = new Holder<>();
        Holder<SessionInfo> sessionInfo = new Holder<>();

        String signatureServiceCardHandle;
        try {
            signatureServiceCardHandle = connectorCardsService.getConnectorCardHandle(
                    ConnectorCardsService.CardHandleType.HBA);
            connectorServicesProvider.getSignatureServicePortTypeV755().getSignatureMode(signatureServiceCardHandle, connectorServicesProvider.getContextType(), status, comfortSignatureStatus,
                    comfortSignatureMax, comfortSignatureTimer, sessionInfo);
            return new GetSignatureModeResponseEvent(status.value, comfortSignatureStatus.value, comfortSignatureMax.value, comfortSignatureTimer.value, sessionInfo.value);
        } catch (ConnectorCardsException | FaultMessage e) {
            log.log(Level.WARNING, "Could not get signature signature", e);
            exceptionEvent.fire(e);
            return null;
        }
    }

    /**
     * Reacts to the event the DeactivateComfortSignatureEvent
     */
    public void onDeactivateComfortSignatureEvent(@ObservesAsync DeactivateComfortSignatureEvent deactivateComfortSignatureEvent) {
        deactivateComfortSignature();
        onGetSignatureModeEvent(null);
    }

    /**
     *
     */
    public void deactivateComfortSignature() {
        String signatureServiceCardHandle = null;
        try {
            signatureServiceCardHandle = connectorCardsService.getConnectorCardHandle(
                    ConnectorCardsService.CardHandleType.HBA);
            connectorServicesProvider.getSignatureServicePortTypeV755().deactivateComfortSignature(Arrays.asList(signatureServiceCardHandle));
        } catch (ConnectorCardsException | FaultMessage e) {
            log.log(Level.WARNING, "Could not deactivate comfort signature", e);
            exceptionEvent.fire(e);
        }
    }

    /**
     * @throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage
     */
    public GetCardsResponse getCards() throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        GetCards parameter = new GetCards();
        parameter.setContext(connectorServicesProvider.getContextType());
        return connectorServicesProvider.getEventServicePortType().getCards(parameter);
    }
}
