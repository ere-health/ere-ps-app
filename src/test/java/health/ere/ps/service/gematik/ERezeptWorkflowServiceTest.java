package health.ere.ps.service.gematik;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;
import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.exception.gematik.ERezeptWorkflowException;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.connector.endpoint.SSLUtilities;
import health.ere.ps.service.extractor.SVGExtractor;
import health.ere.ps.service.fhir.bundle.PrescriptionBundlesBuilder;
import health.ere.ps.service.fhir.bundle.PrescriptionBundlesBuilderTest;
import health.ere.ps.service.muster16.Muster16FormDataExtractorService;
import health.ere.ps.service.muster16.parser.Muster16SvgExtractorParser;
import health.ere.ps.service.pdf.DocumentService;
import health.ere.ps.test.DevelopmentTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static health.ere.ps.service.extractor.TemplateProfile.CGM_TURBO_MED;

@QuarkusTest
@TestProfile(DevelopmentTestProfile.class)
public class ERezeptWorkflowServiceTest {

    private static final Logger log = Logger.getLogger(ERezeptWorkflowServiceTest.class.getName());
    private final FhirContext fhirContext = FhirContext.forR4();
    private final IParser iParser = fhirContext.newXmlParser();
    private final String testBearerToken = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIiwiY3R5IjoiTkpXVCIsImV4cCI6MTYyNTA2NjM0MH0..GSqrRDh8B9_4L4VO.nqMys3GE_dKBDgEfIA2O0Sg9QQc8csNlsl8XVWQRnP3iC5Q7777Ci_SbNTLkqBSeEX_cKUbFIo7ElfDGlW3doVt9lZrqUsYvZySewl4-Rb9d3D1hFugegY46_LXtzuzwEm36W8M38DRWaIrP759riLFUPqcB6vr3oIMKzjeYZ2l3A1-H8H8AKfFhYwneWAGG5RwXwDNWMocnn3--m3pRRjgVE7JdAli8Nw5rJ26aAJ4S9q9WOQElWOzFnYteAqqjdLlWXHgK_7rElgNvMckhOeEN9rg0rouWuhN-bNXyeJMFuRQALXZ1cl5c7LGwwfI5RB-OwpOE7hAHGcWoovs89_e0uL-R8M9zNSvqU0IIDmqnvhVMdP63yHRYOCnjlsy7Z6ViWkJ_O4BnD8HbdK0yBQknKDmriX1ofPaVzZFyuLyKCLdDIhw5Ub5ML_tJ0vZ9m9WO8u1cm_352qO0NWN4O1f6b8dO2UWb1X-copBjebsD-A1Kyw8BoJzRq2dGvfarfHI1bkRl_KhFkVX15dpoqPcyLpRcqYS4W26jK7MlrjNcy508FS7CcfheQ4zbi3hxQGZDfCZ9cEit3h5I6lZEGHRGDLm_QDbEC-2rkDRRAP_97n3zswa97c57B3xG366yn4FCe9GeZHiZCY4qMtRSijAcW71G3CYzArXWwMURxAuYL8cGOen_Fsit491AM6R3AtYK4yQ_eTk_4i6UYv6GqPmxvRYjNjzAfMeyKupkLY8N0wQnL67dkMTz7RH-N8BL1PDF-ETzXmKVFmUdvhojEYV7vfOJx2yV-cXv2cChBI7A--SSffFJhqZOQUYMFRtUphBiXZTLgF2_PFpS32lgdCpn5dkNW7kMK1ARIZx7iVvhIYcYxSByJcdNZ928nHT5Kpdz8Z5ap-O8L_FYjiPrRAJ9O28ZU7ZHTVRDyOb5nOZf7kG3xVIvxJV3FQ0kLmXp4xU8tW8G53sj161rFllBJLVBKDI_7fHTnOg2Aa58wCm4AeC4thH90szLkSAPS3GtFsWkkHaEuBWffBvrHEI2POOwVy2eyz991z3OZBrwF76N1bazJTrMc4CYp3BWSR7S6dYf-E2wYeXeKqJud0qrmfVlGjMfLre8NVn-8hYtrxPEOCFAoBMZRf1S2IPvOgdYRFMoRcJDQ6PyBiSetPWvNRmJJbdWiKEUxGK1ILSKfg5QQxvWpS3yj-evGLApJbL0BBmhougcvRcbm0xQQexcZF9VQdVKye7jEG0T49SQMhskh8KU3BwJaQ.A0rDmBg2FB_lLfswYqw8ww";

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


        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();
    }

    @Test
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testGetCards() throws de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        eRezeptWorkflowService.getCards();
    }


    @Test
    @Disabled
    void testCreateERezeptOnPrescriptionServer() throws IOException, ERezeptWorkflowException {
        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/examples_erezept/bundle_July_2.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
        DocumentService documentService = new DocumentService();
        documentService.init();
        ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mmX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        Files.write(Paths.get("target/E-Rezept-"+thisMoment+".pdf"), a.toByteArray());
    }

    @Test
    @Disabled
    void testCreateERezeptOnPrescriptionServer2() throws IOException, ERezeptWorkflowException {
        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/281a985c-f25b-4aae-91a6-41ad744080b0.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
        DocumentService documentService = new DocumentService();
        documentService.init();
        ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        Files.write(Paths.get("target/E-Rezept-"+thisMoment+".pdf"), a.toByteArray());
    }

    @Test
    @Disabled
    void testCreateERezeptOnPrescriptionServerX110479894() throws IOException, ERezeptWorkflowException {
        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/X110479894.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
        DocumentService documentService = new DocumentService();
        documentService.init();
        ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        Files.write(Paths.get("target/E-Rezept-"+thisMoment+".pdf"), a.toByteArray());
    }


    @Test
    @Disabled
    void testCreateERezeptOnPrescriptionServerX110493020() throws IOException, ERezeptWorkflowException {

        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/X110493020.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
        DocumentService documentService = new DocumentService();
        documentService.init();
        ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        Files.write(Paths.get("target/E-Rezept-"+thisMoment+".pdf"), a.toByteArray());
    }


    @Test
    @Disabled
    void testCreateERezeptOnPrescriptionServerX110433911() throws IOException, ERezeptWorkflowException {

        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/X110433911.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
        DocumentService documentService = new DocumentService();
        documentService.init();
        ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        Files.write(Paths.get("target/E-Rezept-"+thisMoment+".pdf"), a.toByteArray());
    }

    @Test
    @Disabled
    void testCreateERezeptOnPrescriptionServerX110452075() throws IOException, ERezeptWorkflowException {

        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/X110452075.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
        DocumentService documentService = new DocumentService();
        documentService.init();
        ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        Files.write(Paths.get("target/E-Rezept-"+thisMoment+".pdf"), a.toByteArray());
    }

    @Test
    @Disabled
    void testCreateERezeptMassCreate() throws IOException {
        int i = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(ClassLoader.getSystemResource("/simplifier_erezept/demos/").toURI()), "*.{xml}")) {
            for (Path entry: stream) {
                Bundle bundle = iParser.parseResource(Bundle.class, new FileInputStream(entry.toFile()));
                BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
                DocumentService documentService = new DocumentService();
                documentService.init();
                ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
                String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                        .withZone(ZoneOffset.UTC)
                        .format(Instant.now());
                Files.write(Paths.get("target/E-Rezept-"+thisMoment+".pdf"), a.toByteArray());
                i++;
                if(i==2) {
                    break;
                }
            }
        } catch (DirectoryIteratorException | ERezeptWorkflowException | URISyntaxException ex) {
            // I/O error encounted during the iteration, the cause is an IOException
            // throw ex.getCause();
        }
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
    void testSignDocument() throws IOException, ERezeptWorkflowException {
        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/examples_erezept/Erezept_template_3.xml"));

        Task task = new Task();
        BundleWithAccessCodeOrThrowable bundleWithAccessCode = eRezeptWorkflowService.updateBundleWithTask(task, bundle);
        SignResponse signResponse = eRezeptWorkflowService.signBundleWithIdentifiers(bundleWithAccessCode.getBundle());
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
