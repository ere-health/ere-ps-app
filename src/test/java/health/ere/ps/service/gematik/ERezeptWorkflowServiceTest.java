package health.ere.ps.service.gematik;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
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

public class ERezeptWorkflowServiceTest {

    private static Logger log = Logger.getLogger(ERezeptWorkflowServiceTest.class.getName());

    FhirContext fhirContext = FhirContext.forR4();
    IParser iParser = fhirContext.newXmlParser();

    String testBearerToken = "eyJhbGciOiJCUDI1NlIxIiwidHlwIjoiYXQrSldUIiwia2lkIjoicHVrX2lkcF9zaWcifQ.eyJzdWIiOiJWV3dvVWhROHpRTDh0U1BjVW9VcEJXVUs5UVgtOUpvRURaTmttc0dFSDVrIiwicHJvZmVzc2lvbk9JRCI6IjEuMi4yNzYuMC43Ni40LjUwIiwib3JnYW5pemF0aW9uTmFtZSI6IjIwMjExMDEyMiBOT1QtVkFMSUQiLCJpZE51bW1lciI6IjEtMi1BUlpULVdhbHRyYXV0RHJvbWJ1c2NoMDEiLCJhbXIiOlsibWZhIiwic2MiLCJwaW4iXSwiaXNzIjoiaHR0cHM6Ly9pZHAuemVudHJhbC5pZHAuc3BsaXRkbnMudGktZGllbnN0ZS5kZSIsImdpdmVuX25hbWUiOiJXYWx0cmF1dCIsImNsaWVudF9pZCI6ImVSZXplcHRBcHAiLCJhdWQiOiJodHRwczovL2VycC50ZWxlbWF0aWsuZGUvbG9naW4iLCJhY3IiOiJnZW1hdGlrLWVoZWFsdGgtbG9hLWhpZ2giLCJhenAiOiJlUmV6ZXB0QXBwIiwic2NvcGUiOiJlLXJlemVwdCBvcGVuaWQiLCJhdXRoX3RpbWUiOjE2MjEzNDgwMDcsImV4cCI6MTYyMTM0ODMwNywiZmFtaWx5X25hbWUiOiJEcm9tYnVzY2giLCJpYXQiOjE2MjEzNDgwMDcsImp0aSI6IjU5MDI5ZjdmZTYwOGVlNjQifQ.qTgKvnWSXSCMDBGj_VLpqUikBjYzlJT4UTnZoVehr3E2EWLKFir8YmS2zg3Rsk58RojytPzMAVGpYJMoxY2sfQ";

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

        
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            KeyManagerFactory kmf =
                KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
    
            KeyStore ks = KeyStore.getInstance( "PKCS12" );
            // Download this file from the titus backend
            // https://frontend.titus.ti-dienste.de/#/platform/mandant
            ks.load(ERezeptWorkflowServiceTest.class.getResourceAsStream("/ps_erp_incentergy_01.p12"), "00".toCharArray() );
            kmf.init( ks, "00".toCharArray() );
            sc.init( kmf.getKeyManagers(), null, null );
            eRezeptWorkflowService.customSSLContext = sc;
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException | UnrecoverableKeyException | KeyManagementException e) {
            e.printStackTrace();
        }


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
    // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testCreateERezeptTask() throws DataFormatException, IOException {
        Task task = eRezeptWorkflowService.createERezeptTask(testBearerToken);
        Files.write(Paths.get("target/titus-eRezeptWorkflowService-createERezeptTask.xml"), iParser.encodeResourceToString(task).getBytes());
        Files.write(Paths.get("target/titus-eRezeptWorkflowService-accessToken.txt"), ERezeptWorkflowService.getAccessCode(task).getBytes());
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
        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml"));
        Task task = iParser.parseResource(Task.class, new FileInputStream("target/titus-eRezeptWorkflowService-createERezeptTask.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCode = eRezeptWorkflowService.updateBundleWithTask(task, bundle);
        SignResponse signResponse = eRezeptWorkflowService.signBundleWithIdentifiers(bundleWithAccessCode.bundle, false);
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
    void testDeactivateComfortSignature() throws FaultMessage {
        eRezeptWorkflowService.deactivateComfortSignature();
    }


}
