package health.ere.ps.service.gematik;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import de.gematik.ws.conn.signatureservice.v7_5_5.SignResponse;
import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.exception.gematik.ERezeptWorkflowException;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.extractor.SVGExtractor;
import health.ere.ps.service.extractor.SVGExtractorConfiguration;
import health.ere.ps.service.fhir.bundle.PrescriptionBundlesBuilder;
import health.ere.ps.service.fhir.bundle.PrescriptionBundlesBuilderTest;
import health.ere.ps.service.muster16.Muster16FormDataExtractorService;
import health.ere.ps.service.muster16.parser.Muster16SvgExtractorParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static health.ere.ps.service.extractor.TemplateProfile.CGM_TURBO_MED;

@QuarkusTest
public class ERezeptWorkflowServiceTest {

    private static final Logger log = Logger.getLogger(ERezeptWorkflowServiceTest.class.getName());
    private final FhirContext fhirContext = FhirContext.forR4();
    private final IParser iParser = fhirContext.newXmlParser();
    private final String testBearerToken = "eyJhbGciOiJCUDI1NlIxIiwidHlwIjoiYXQrSldUIiwia2lkIjoicHVrX2lkcF9zaWcifQ.eyJzdWIiOiJzM1pPekJtT01GZkdSbHB6R1E5d3NvQ3hBWFBZbFVQcUYwb0I3SWctMEJRIiwicHJvZmVzc2lvbk9JRCI6IjEuMi4yNzYuMC43Ni40LjUwIiwib3JnYW5pemF0aW9uTmFtZSI6IjIwMjExMDEyMiBOT1QtVkFMSUQiLCJpZE51bW1lciI6IjEtMi1BUlpULVdhbHRyYXV0RHJvbWJ1c2NoMDEiLCJhbXIiOlsibWZhIiwic2MiLCJwaW4iXSwiaXNzIjoiaHR0cHM6Ly9pZHAuemVudHJhbC5pZHAuc3BsaXRkbnMudGktZGllbnN0ZS5kZSIsImdpdmVuX25hbWUiOiJXYWx0cmF1dCIsImNsaWVudF9pZCI6ImVSZXplcHRBcHAiLCJhY3IiOiJnZW1hdGlrLWVoZWFsdGgtbG9hLWhpZ2giLCJhdWQiOiJodHRwczovL2VycC10ZXN0LnplbnRyYWwuZXJwLnNwbGl0ZG5zLnRpLWRpZW5zdGUuZGUvIiwiYXpwIjoiZVJlemVwdEFwcCIsInNjb3BlIjoiZS1yZXplcHQgb3BlbmlkIiwiYXV0aF90aW1lIjoxNjIzNjI4OTgyLCJleHAiOjE2MjM2MjkyODIsImZhbWlseV9uYW1lIjoiRHJvbWJ1c2NoIiwiaWF0IjoxNjIzNjI4OTgyLCJqdGkiOiI3NGRkMGRmNTNlZGUzYjI3In0.oR4PM_G218IFYPKyhCEdBnRgVeF2goE_fZYkVWmiXlF1FWetk-pCigoigBhqyPjGGbZTGnUL7Z2VgkdWQ9GirA";

    @Inject
    ERezeptWorkflowService eRezeptWorkflowService;

    @BeforeEach
    void init() throws SecretsManagerException {
        try {
            // https://community.oracle.com/thread/1307033?start=0&tstart=0
            LogManager.getLogManager().readConfiguration(
                    ERezeptWorkflowServiceTest.class
                            .getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dumpTreshold", "999999");

//        eRezeptWorkflowService = new ERezeptWorkflowService();
        eRezeptWorkflowService.prescriptionserverUrl = "https://fd.erezept-instanz1.titus.ti-dienste.de";
        eRezeptWorkflowService.signatureServiceEndpointAddress = "https://kon-instanz2.titus.ti-dienste.de:443/soap-api/SignatureService/7.5.4";
        eRezeptWorkflowService.eventServiceEndpointAddress = "https://kon-instanz2.titus.ti-dienste.de/soap-api/EventService/7.2.0";
//        eRezeptWorkflowService.signatureServiceCardHandle = "1-1-ARZT-WaltrautFinkengrund01";
        eRezeptWorkflowService.signatureServiceContextMandantId = "ps_erp_incentergy_01";
        eRezeptWorkflowService.signatureServiceContextClientSystemId = "ps_erp_incentergy_01_HBA";
        eRezeptWorkflowService.signatureServiceContextWorkplaceId = "CATS";
        eRezeptWorkflowService.signatureServiceContextUserId = "197610";
        eRezeptWorkflowService.signatureServiceTvMode = "NONE";
        eRezeptWorkflowService.enableVau = true;

        InputStream p12Certificate = ERezeptWorkflowServiceTest.class.getResourceAsStream("/ps_erp_incentergy_01.p12");
        eRezeptWorkflowService.setUpCustomSSLContext(p12Certificate);
        eRezeptWorkflowService.init();
    }

    @Test
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testGetCards() throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        eRezeptWorkflowService.getCards();
    }

    
    @Test
    @Disabled
    void testCreateERezeptOnPrescriptionServer() throws ERezeptWorkflowException {
        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/examples_erezept/Erezept_template_2.xml"));

        eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
    }

    @Test
    @Disabled
    void testCreateERezeptWithPrescriptionBuilderOnPrescriptionServer() throws ParseException {
        List<Bundle> bundles = new PrescriptionBundlesBuilder(
                PrescriptionBundlesBuilderTest.getMuster16PrescriptionFormForTests()).createBundles();

        bundles.forEach(bundle -> {
            log.info(iParser.encodeResourceToString(bundle));
            try {
                eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
            } catch (ERezeptWorkflowException e) {
                e.printStackTrace();
            }
        });
    }

    @Disabled
    @Test
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testCreateERezeptFromPdfOnPrescriptionServer() throws URISyntaxException,
            IOException, ParseException, ERezeptWorkflowException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(CGM_TURBO_MED.configuration, true);

        try (PDDocument pdDocument = PDDocument.load(getClass().getResourceAsStream(
                "/muster-16-print-samples/test1.pdf"))) {
            Map<String, String> map = svgExtractor.extract(pdDocument);
            Muster16SvgExtractorParser muster16Parser = new Muster16SvgExtractorParser(map);

            Muster16PrescriptionForm muster16PrescriptionForm = Muster16FormDataExtractorService.fillForm(muster16Parser);
            PrescriptionBundlesBuilder bundleBuilder =
                    new PrescriptionBundlesBuilder(muster16PrescriptionForm);

            List<Bundle> bundles = bundleBuilder.createBundles();
            bundles.forEach(bundle -> {
                log.info(iParser.encodeResourceToString(bundle));
                try {
                    eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
                } catch (ERezeptWorkflowException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Test
    @Disabled
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testCreateERezeptTask() throws DataFormatException, IOException {
        Task task = eRezeptWorkflowService.createERezeptTask(testBearerToken);
        Files.write(Paths.get("target/titus-eRezeptWorkflowService-createERezeptTask.xml"), iParser.encodeResourceToString(task).getBytes());
        Files.write(Paths.get("target/titus-eRezeptWorkflowService-accessToken.txt"), ERezeptWorkflowService.getAccessCode(task).getBytes());
        Files.write(Paths.get("target/titus-eRezeptWorkflowService-taskId.txt"), task.getIdElement().getIdPart().getBytes());
    }

    @Test
    @Disabled
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testActivateComfortSignature() throws ERezeptWorkflowException {
        eRezeptWorkflowService.activateComfortSignature();
    }

    @Test
    @Disabled
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testGetSignatureMode() throws ERezeptWorkflowException {
        eRezeptWorkflowService.getSignatureMode();
    }

    
    @Test
    @Disabled
    // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testUpdateBundleWithTaskAndSignBundleWithIdentifiers() throws IOException, ERezeptWorkflowException {
        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/examples_erezept/Erezept_template_3.xml"));

        Task task = iParser.parseResource(Task.class, new FileInputStream("target/titus-eRezeptWorkflowService-createERezeptTask.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCode = eRezeptWorkflowService.updateBundleWithTask(task, bundle);
        SignResponse signResponse = eRezeptWorkflowService.signBundleWithIdentifiers(bundleWithAccessCode.getBundle(), true);
        Files.write(Paths.get("target/titus-eRezeptWorkflowService-signBundleWithIdentifiers.dat"), signResponse.getSignatureObject().getBase64Signature().getValue());
    }

    @Test
    @Disabled
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testUpdateERezeptTask() throws DataFormatException, IOException {
        Task task = iParser.parseResource(Task.class, new FileInputStream("target/titus-eRezeptWorkflowService-createERezeptTask.xml"));
        byte[] signedBytes = Files.readAllBytes(Paths.get("target/titus-eRezeptWorkflowService-signBundleWithIdentifiers.dat"));
        String accessCode = new String(Files.readAllBytes(Paths.get("target/titus-eRezeptWorkflowService-accessToken.txt")));
        eRezeptWorkflowService.updateERezeptTask(testBearerToken, task, accessCode, signedBytes);
    }

    @Test
    @Disabled
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testAbortERezeptTask() throws DataFormatException, IOException {
        String accessCode = new String(Files.readAllBytes(Paths.get("target/titus-eRezeptWorkflowService-accessToken.txt")));
        String taskId = new String(Files.readAllBytes(Paths.get("target/titus-eRezeptWorkflowService-taskId.txt")));
        eRezeptWorkflowService.abortERezeptTask(testBearerToken, taskId, accessCode);
    }

    @Test
    @Disabled
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testDeactivateComfortSignature() throws ERezeptWorkflowException {
        eRezeptWorkflowService.deactivateComfortSignature();
    }
}
