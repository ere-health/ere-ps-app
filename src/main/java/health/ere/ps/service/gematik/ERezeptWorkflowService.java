package health.ere.ps.service.gematik;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;

import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.parser.XMLParserException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7.DocumentType;
import de.gematik.ws.conn.signatureservice.v7.SignRequest;
import de.gematik.ws.conn.signatureservice.v7.SignRequest.OptionalInputs;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;
import de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureService;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortType;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;

public class ERezeptWorkflowService {

    private static Logger log = Logger.getLogger(ERezeptWorkflowService.class.getName());

    FhirContext fhirContext = FhirContext.forR4();

    @ConfigProperty(name = "prescriptionserver.url", defaultValue = "")
    String prescriptionserverUrl;

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

    static {
        org.apache.xml.security.Init.init();
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
    public BundleWithAccessCode createERezeptOnPresciptionServer(String bearerToken, Bundle bundle)
            throws InvalidCanonicalizerException, XMLParserException, CanonicalizationException, FaultMessage,
            IOException {

        log.fine("Bearer Token: " + bearerToken);

        // Example: src/test/resources/gematik/Task-4711.xml
        Task task = createERezeptTask(bearerToken);

        // Example:
        // src/test/resources/gematik/Bundle-4fe2013d-ae94-441a-a1b1-78236ae65680.xml
        BundleWithAccessCode bundleWithAccessCode = updateBundleWithTask(task, bundle);
        SignResponse signedDocument = signBundleWithIdentifiers(bundleWithAccessCode.bundle);

        updateERezeptTask(bearerToken, task, bundleWithAccessCode, signedDocument);

        return bundleWithAccessCode;
    }

    /**
     * This function adds the E-Rezept to the previously created task.
     * 
     * @param task
     * @param signedDocument
     */
    public void updateERezeptTask(String bearerToken, Task task, BundleWithAccessCode bundleWithAccessCode,
            SignResponse signedDocument) {
        Client client = ClientBuilder.newBuilder().build();

        Parameters parameters = new Parameters();
        ParametersParameterComponent ePrescriptionParameter = new ParametersParameterComponent();
        ePrescriptionParameter.setName("ePrescription");
        Binary binary = new Binary();
        binary.setContentType("application/pkcs7-mime");
        binary.setContent(signedDocument.getSignatureObject().getBase64Signature().getValue());
        ePrescriptionParameter.setResource(binary);
        parameters.addParameter(ePrescriptionParameter);
        String s = client.target(prescriptionserverUrl).path("/Task").path("/" + task.getId()).path("/$activate")
                .request().header("Authorization", "Bearer " + bearerToken)
                .header("X-AccessCode", bundleWithAccessCode.accessCode)
                .post(Entity.entity(parameters, "application/fhir+xml; charset=UTF-8")).readEntity(String.class);
        log.fine(s);
    }

    /**
     * Adds the identifiers to the bundle.
     * 
     * @param task
     * @param bundle
     */
    public BundleWithAccessCode updateBundleWithTask(Task task, Bundle bundle) {
        String identifierSystem = "https://gematik.de/fhir/NamingSystem/PrescriptionID";
        String prescriptionID = task.getIdentifier().stream().filter(id -> id.getSystem().equals(identifierSystem))
                .findFirst().get().getValue();
        Identifier identifier = new Identifier();
        identifier.setSystem(identifierSystem);
        identifier.setValue(prescriptionID);
        bundle.setIdentifier(identifier);

        String accessCode = task.getIdentifier().stream()
                .filter(id -> id.getSystem().equals("https://gematik.de/fhir/NamingSystem/AccessCode")).findFirst()
                .get().getValue();
        return new BundleWithAccessCode(bundle, accessCode);
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
    public SignResponse signBundleWithIdentifiers(Bundle bundle) throws FaultMessage, InvalidCanonicalizerException,
            XMLParserException, CanonicalizationException, IOException {
        SignatureServicePortType signatureService = new SignatureService().getSignatureServicePort();

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
        signRequest.setDocument(document);
        signRequest.setIncludeRevocationInfo(true);
        List<SignRequest> signRequests = Arrays.asList(signRequest);

        ContextType contextType = new ContextType();
        contextType.setMandantId(signatureServiceContextMandantId);
        contextType.setClientSystemId(signatureServiceContextClientSystemId);
        contextType.setWorkplaceId(signatureServiceContextWorkplaceId);
        contextType.setUserId(signatureServiceContextUserId);

        String jobNumber = UUID.randomUUID().toString();

        List<SignResponse> signResponse = signatureService.signDocument(signatureServiceCardHandle,
                signatureServiceCrypt, contextType, signatureServiceTvMode, jobNumber, signRequests);
        return signResponse.get(0);
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

        String taskString = client.target(prescriptionserverUrl).path("/Task/$create").request()
                .header("Authorization", "Bearer " + bearerToken)
                .post(Entity.entity(parameterString, "application/fhir+xml; charset=UTF-8")).readEntity(String.class);

        log.fine("Task Response: " + taskString);
        Task task = fhirContext.newXmlParser().parseResource(Task.class, new StringReader(taskString));
        return task;
    }

}
