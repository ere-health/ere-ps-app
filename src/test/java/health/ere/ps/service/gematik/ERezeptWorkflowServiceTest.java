package health.ere.ps.service.gematik;

import static org.mockito.ArgumentMatchers.eq;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.parser.XMLParserException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;
import de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.service.fhir.bundle.PrescriptionBundleBuilder;
import health.ere.ps.service.fhir.bundle.PrescriptionBundleBuilderTest;

public class ERezeptWorkflowServiceTest {

    private static Logger log = Logger.getLogger(ERezeptWorkflowServiceTest.class.getName());

    FhirContext fhirContext = FhirContext.forR4();
    IParser iParser = fhirContext.newXmlParser();

    String testBearerToken = "eyJhbGciOiJCUDI1NlIxIiwidHlwIjoiYXQrSldUIiwia2lkIjoicHVrX2lkcF9zaWcifQ.eyJzdWIiOiJWV3dvVWhROHpRTDh0U1BjVW9VcEJXVUs5UVgtOUpvRURaTmttc0dFSDVrIiwicHJvZmVzc2lvbk9JRCI6IjEuMi4yNzYuMC43Ni40LjUwIiwib3JnYW5pemF0aW9uTmFtZSI6IjIwMjExMDEyMiBOT1QtVkFMSUQiLCJpZE51bW1lciI6IjEtMi1BUlpULVdhbHRyYXV0RHJvbWJ1c2NoMDEiLCJhbXIiOlsibWZhIiwic2MiLCJwaW4iXSwiaXNzIjoiaHR0cHM6Ly9pZHAuemVudHJhbC5pZHAuc3BsaXRkbnMudGktZGllbnN0ZS5kZSIsImdpdmVuX25hbWUiOiJXYWx0cmF1dCIsImNsaWVudF9pZCI6ImVSZXplcHRBcHAiLCJhdWQiOiJodHRwczovL2VycC50ZWxlbWF0aWsuZGUvbG9naW4iLCJhY3IiOiJnZW1hdGlrLWVoZWFsdGgtbG9hLWhpZ2giLCJhenAiOiJlUmV6ZXB0QXBwIiwic2NvcGUiOiJvcGVuaWQgZS1yZXplcHQiLCJhdXRoX3RpbWUiOjE2MjE1MzQ1NTEsImV4cCI6MTYyMTUzNDg1MSwiZmFtaWx5X25hbWUiOiJEcm9tYnVzY2giLCJpYXQiOjE2MjE1MzQ1NTEsImp0aSI6ImUwMDA2ZTA1NGYzMWU3ZDEifQ.YP02JNf7la2pjJbSXVAv-apOSzWnsz1yMi__JXz-wMJciXXZ-Sa4yVFQ0e3YPS8I2PNBonAfS9PYh6Cfr0G0Sg";

    static ERezeptWorkflowService eRezeptWorkflowService;

    @BeforeAll
    static void init() {

        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dumpTreshold", "999999");

        eRezeptWorkflowService = new ERezeptWorkflowService();
        eRezeptWorkflowService.prescriptionserverUrl = "https://fd.erezept-instanz1.titus.ti-dienste.de";
        eRezeptWorkflowService.signatureServiceEndpointAddress = "https://kon-instanz2.titus.ti-dienste.de:443/soap-api/SignatureService/7.5.4";
        eRezeptWorkflowService.eventServiceEndpointAddress = "https://kon-instanz2.titus.ti-dienste.de/soap-api/EventService/7.2.0";
        eRezeptWorkflowService.signatureServiceCardHandle = "1-1-ARZT-WaltrautFinkengrund01";
        eRezeptWorkflowService.signatureServiceContextMandantId = "ps_erp_incentergy_01";
        eRezeptWorkflowService.signatureServiceContextClientSystemId = "ps_erp_incentergy_01_HBA";
        eRezeptWorkflowService.signatureServiceContextWorkplaceId = "CATS";
        eRezeptWorkflowService.signatureServiceContextUserId = "197610";
        eRezeptWorkflowService.signatureServiceTvMode = "NONE";

        
        InputStream p12Certificate = ERezeptWorkflowServiceTest.class.getResourceAsStream("/ps_erp_incentergy_01.p12");
        eRezeptWorkflowService.setUpCustomSSLContext(p12Certificate);
        eRezeptWorkflowService.init();
    }

    @Test @Disabled
    // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testGetCards() throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        eRezeptWorkflowService.getCards();
    }
    
    @Test @Disabled
    void testCreateERezeptOnPrescriptionServer() throws InvalidCanonicalizerException, XMLParserException, CanonicalizationException, FaultMessage, IOException {
        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml"));
        eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
    }

    @Test @Disabled
    void testCreateERezeptWithPrescriptionBuilderOnPrescriptionServer() throws InvalidCanonicalizerException, XMLParserException, CanonicalizationException, FaultMessage, IOException, ParseException {
        Bundle bundle = PrescriptionBundleBuilderTest.getPrescriptionBundleBuilder().createBundle();
        log.info(iParser.encodeResourceToString(bundle));
        eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
    }

    @Test @Disabled
    // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testCreateERezeptTask() throws DataFormatException, IOException {
        Task task = eRezeptWorkflowService.createERezeptTask(testBearerToken);
        Files.write(Paths.get("target/titus-eRezeptWorkflowService-createERezeptTask.xml"), iParser.encodeResourceToString(task).getBytes());
        Files.write(Paths.get("target/titus-eRezeptWorkflowService-accessToken.txt"), ERezeptWorkflowService.getAccessCode(task).getBytes());
        Files.write(Paths.get("target/titus-eRezeptWorkflowService-taskId.txt"), task.getIdElement().getIdPart().getBytes());
    }

    @Test @Disabled
    // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testActivateComfortSignature() throws FaultMessage {
        eRezeptWorkflowService.activateComfortSignature();
    }

    @Test @Disabled
    // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testGetSignatureMode() throws FaultMessage {
        eRezeptWorkflowService.getSignatureMode();
    }
    
    @Test @Disabled
    // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testUpdateBundleWithTaskAndSignBundleWithIdentifiers() throws InvalidCanonicalizerException, XMLParserException, CanonicalizationException, FaultMessage, IOException {
        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/281a985c-f25b-4aae-91a6-41ad744080b0.xml"));
        Task task = iParser.parseResource(Task.class, new FileInputStream("target/titus-eRezeptWorkflowService-createERezeptTask.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCode = eRezeptWorkflowService.updateBundleWithTask(task, bundle);
        SignResponse signResponse = eRezeptWorkflowService.signBundleWithIdentifiers(bundleWithAccessCode.bundle, true);
        Files.write(Paths.get("target/titus-eRezeptWorkflowService-signBundleWithIdentifiers.dat"), signResponse.getSignatureObject().getBase64Signature().getValue());
    }
    @Test @Disabled
    // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testUpdateERezeptTask() throws DataFormatException, IOException {
        Task task = iParser.parseResource(Task.class, new FileInputStream("target/titus-eRezeptWorkflowService-createERezeptTask.xml"));
        byte[] signedBytes = Files.readAllBytes(Paths.get("target/titus-eRezeptWorkflowService-signBundleWithIdentifiers.dat"));
        String accessCode = new String(Files.readAllBytes(Paths.get("target/titus-eRezeptWorkflowService-accessToken.txt")));
        eRezeptWorkflowService.updateERezeptTask(testBearerToken, task, accessCode, signedBytes);
    }

    @Test @Disabled
    // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testAbortERezeptTask() throws DataFormatException, IOException {
        String accessCode = new String(Files.readAllBytes(Paths.get("target/titus-eRezeptWorkflowService-accessToken.txt")));
        String taskId = new String(Files.readAllBytes(Paths.get("target/titus-eRezeptWorkflowService-taskId.txt")));
        eRezeptWorkflowService.abortERezeptTask(testBearerToken, taskId, accessCode);
    }

    
    @Test @Disabled
    // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testDeactivateComfortSignature() throws FaultMessage {
        eRezeptWorkflowService.deactivateComfortSignature();
    }


}
