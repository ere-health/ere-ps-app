package health.ere.ps.service.gematik;

import static health.ere.ps.service.extractor.TemplateProfile.CGM_TURBO_MED;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.apache.fop.apps.FOPException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.exception.gematik.ERezeptWorkflowException;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.profile.RUTestProfile;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.connector.endpoint.SSLUtilities;
import health.ere.ps.service.extractor.SVGExtractor;
import health.ere.ps.service.fhir.XmlPrescriptionProcessor;
import health.ere.ps.service.fhir.bundle.PrescriptionBundlesBuilder;
import health.ere.ps.service.fhir.bundle.PrescriptionBundlesBuilderTest;
import health.ere.ps.service.idp.client.IdpClient;
import health.ere.ps.service.idp.client.IdpHttpClientService;
import health.ere.ps.service.muster16.Muster16FormDataExtractorService;
import health.ere.ps.service.muster16.parser.Muster16SvgExtractorParser;
import health.ere.ps.service.pdf.DocumentService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(RUTestProfile.class)
public class ERezeptWorkflowServiceTest {

    private static final Logger log = Logger.getLogger(ERezeptWorkflowServiceTest.class.getName());
    private final IParser iParser = FhirContext.forR4().newXmlParser();

    @Inject
    ERezeptWorkflowService eRezeptWorkflowService;

    @Inject
    AppConfig appConfig;
    @Inject
    IdpClient idpClient;
    @Inject
    CardCertificateReaderService cardCertificateReaderService;
    @Inject
    ConnectorCardsService connectorCardsService;

    String discoveryDocumentUrl;

    @BeforeEach
    void init() {
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
    void testCreateERezeptMassCreateWithComfortSignature() throws Exception {
        eRezeptWorkflowService.activateComfortSignature();
        testCreateERezeptMassCreate();
        eRezeptWorkflowService.deactivateComfortSignature();
    }

    @Test
    @Disabled
    void testCreateERezeptMassCreate() throws Exception {

        discoveryDocumentUrl = appConfig.getIdpBaseURL() + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;

        idpClient.init(appConfig.getIdpClientId(), appConfig.getIdpAuthRequestRedirectURL(), discoveryDocumentUrl, true, true);
        idpClient.initializeClient(true, false);

        String cardHandle = connectorCardsService.getConnectorCardHandle(
                ConnectorCardsService.CardHandleType.SMC_B);

        X509Certificate x509Certificate = cardCertificateReaderService.retrieveSmcbCardCertificate(cardHandle, null);

        IdpTokenResult idpTokenResult = idpClient.login(x509Certificate);

        log.info("Access Token: " + idpTokenResult.getAccessToken().getRawString());

        String testBearerToken = idpTokenResult.getAccessToken().getRawString();

        eRezeptWorkflowService.setBearerToken(testBearerToken);

        int i = 0;
        DocumentService documentService = new DocumentService();
                documentService.init();
                
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("src/test/resources/pilotregion/"), "*.{xml}")) {
            for (Path entry : stream) {
                Bundle bundle = iParser.parseResource(Bundle.class, new FileInputStream(entry.toFile()));
                try {
                    ((MedicationRequest)bundle.getEntry().stream().filter(e -> e.getResource() instanceof MedicationRequest).findAny().get().getResource()).setAuthoredOnElement(new DateTimeType(new Date(), TemporalPrecisionEnum.DAY));
                } catch(NoSuchElementException ex) {
                    ex.printStackTrace();
                }
                //try {
                //    Patient patient = ((Patient)bundle.getEntry().stream().filter(e -> e.getResource() instanceof Patient).findAny().get().getResource());
                //    patient.getIdentifier().get(0).setValue("X110490897");
                //} catch(NoSuchElementException ex) {
                //    ex.printStackTrace();
                //} 
                BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(bundle);
                String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ssX")
                        .withZone(ZoneOffset.UTC)
                        .format(Instant.now());
                if(bundleWithAccessCodeOrThrowable.getThrowable() != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    bundleWithAccessCodeOrThrowable.getThrowable().printStackTrace(pw);
                    Files.write(Paths.get("target/E-Rezept-" + thisMoment + ".txt"), sw.toString().getBytes());
                } else {
                    ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
                    Files.write(Paths.get("target/E-Rezept-" + thisMoment + ".pdf"), a.toByteArray());
                    log.info("Time: "+thisMoment);
                }
                i++;
                //if (i == 1) {
                //    break;
                //}
            }
        } catch (DirectoryIteratorException | ERezeptWorkflowException ex) {
            // I/O error encounted during the iteration, the cause is an IOException
            log.info("Exception: "+ex);
                
            ex.printStackTrace();
        }
    }

    @Test
    @Disabled
    void testCreateERezeptMassCreate2() throws Exception {

        discoveryDocumentUrl = appConfig.getIdpBaseURL() + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;

        idpClient.init(appConfig.getIdpClientId(), appConfig.getIdpAuthRequestRedirectURL(), discoveryDocumentUrl, true, true);
        idpClient.initializeClient(true, false);

        String cardHandle = connectorCardsService.getConnectorCardHandle(
                ConnectorCardsService.CardHandleType.SMC_B);

        X509Certificate x509Certificate = cardCertificateReaderService.retrieveSmcbCardCertificate(cardHandle, null);

        IdpTokenResult idpTokenResult = idpClient.login(x509Certificate);

        log.info("Access Token: " + idpTokenResult.getAccessToken().getRawString());

        int i = 0;
        DocumentService documentService = new DocumentService();
                documentService.init();
        eRezeptWorkflowService.activateComfortSignature();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("src/test/resources/simplifier_erezept/"), "*.{xml}")) {
            for (Path entry : stream) {
                Bundle bundle = iParser.parseResource(Bundle.class, new FileInputStream(entry.toFile()));
                
                Medication medication = ((Medication)bundle.getEntry().stream().filter(e -> e.getResource() instanceof Medication).findAny().get().getResource());
                // PZN, FreeText, Ingredient, Compounding
                String medicationProfile = medication.getMeta().getProfile().get(0).getValue();
                String type;
                if(medicationProfile.equals("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.1")) {
                    type = "PZN";
                } else if(medicationProfile.equals("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText|1.0.1")) {
                    type = "FreeText";
                } else if(medicationProfile.equals("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient|1.0.1")) {
                    type = "Ingredient";
                } else if(medicationProfile.equals("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Compounding|1.0.1")) {
                    type = "Compounding";
                } else {
                    type = "Unknown";
                }

                try {
                    MedicationRequest medicationRequest = ((MedicationRequest)bundle.getEntry().stream().filter(e -> e.getResource() instanceof MedicationRequest).findAny().get().getResource());
                    medicationRequest.setAuthoredOnElement(new DateTimeType(new Date(), TemporalPrecisionEnum.DAY));
                    Extension multiplePrescription = medicationRequest.getExtensionByUrl("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription");
                    BooleanType multiplePrescriptionBoolean = (BooleanType)multiplePrescription.getExtensionByUrl("Kennzeichen").getValue();
                    if(multiplePrescriptionBoolean.booleanValue()) {
                        // do not generate multiplePrescription
                        continue;
                    }
                } catch(NoSuchElementException ex) {
                    ex.printStackTrace();
                }
                
                //try {
                //    Patient patient = ((Patient)bundle.getEntry().stream().filter(e -> e.getResource() instanceof Patient).findAny().get().getResource());
                //    patient.getIdentifier().get(0).setValue("X110490897");
                //} catch(NoSuchElementException ex) {
                //    ex.printStackTrace();
                //} 
                BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(bundle);
                String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ssX")
                        .withZone(ZoneOffset.UTC)
                        .format(Instant.now());
                if(bundleWithAccessCodeOrThrowable.getThrowable() != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    bundleWithAccessCodeOrThrowable.getThrowable().printStackTrace(pw);
                    Files.write(Paths.get("target/E-Rezept-"+type+"-" + thisMoment + ".txt"), sw.toString().getBytes());
                } else {
                    ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
                    Files.write(Paths.get("target/E-Rezept-"+type+"-"  + thisMoment + ".pdf"), a.toByteArray());
                    log.info("Time: "+thisMoment);
                }
                i++;
                //if (i == 1) {
                //    break;
                //}
            }
        } catch (DirectoryIteratorException | ERezeptWorkflowException ex) {
            // I/O error encounted during the iteration, the cause is an IOException
            log.info("Exception: "+ex);
                
            ex.printStackTrace();
        }

        eRezeptWorkflowService.deactivateComfortSignature();
    }

    @Test
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testIsExired() {
        assertTrue(eRezeptWorkflowService.isExpired("eyJhbGciOiJCUDI1NlIxIiwidHlwIjoiYXQrSldUIiwia2lkIjoicHVrX2lkcF9zaWcifQ.eyJzdWIiOiJNU1lXUGYxVlJfaXdlNzFGQVBMVzJJY0YwemNlQTVqa0x2V1piWFlmSms0IiwicHJvZmVzc2lvbk9JRCI6IjEuMi4yNzYuMC43Ni40LjUwIiwib3JnYW5pemF0aW9uTmFtZSI6IjIwMjExMDEyMiBOT1QtVkFMSUQiLCJpZE51bW1lciI6IjEtMi1BUlpULVdhbHRyYXV0RHJvbWJ1c2NoMDEiLCJhbXIiOlsibWZhIiwic2MiLCJwaW4iXSwiaXNzIjoiaHR0cHM6Ly9pZHAuZXJlemVwdC1pbnN0YW56MS50aXR1cy50aS1kaWVuc3RlLmRlIiwiZ2l2ZW5fbmFtZSI6IldhbHRyYXV0IiwiY2xpZW50X2lkIjoiZ2VtYXRpa1Rlc3RQcyIsImFjciI6ImdlbWF0aWstZWhlYWx0aC1sb2EtaGlnaCIsImF1ZCI6Imh0dHBzOi8vZXJwLXRlc3QuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS8iLCJhenAiOiJnZW1hdGlrVGVzdFBzIiwic2NvcGUiOiJvcGVuaWQgZS1yZXplcHQiLCJhdXRoX3RpbWUiOjE2MjU1MjA2ODMsImV4cCI6MTYyNTUyMDk4MywiZmFtaWx5X25hbWUiOiJEcm9tYnVzY2giLCJpYXQiOjE2MjU1MjA2ODMsImp0aSI6ImI4MmMyMzgxYjQ1MTFjZGEifQ.K4qiZS6oSEe5izDiaIN-rBjcXzJM_y6HYUOpIEUKK-9evxEXco8BB4RJhfkagQJKwCgi11pctShMOs5seN1mOw"));
    }

    @Test
    @Disabled
    void testCreateERezeptOnPrescriptionServer() throws IOException, ERezeptWorkflowException, FOPException, TransformerException {
        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/examples_erezept/bundle_July_2.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(bundle);
        DocumentService documentService = new DocumentService();
        documentService.init();
        ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mmX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        Files.write(Paths.get("target/E-Rezept-" + thisMoment + ".pdf"), a.toByteArray());
    }

    @Test
    @Disabled
    void testCreateERezeptOnPrescriptionServerFromXMLBundle() throws IOException, ERezeptWorkflowException, FOPException, TransformerException {
        Bundle[] bundles = XmlPrescriptionProcessor.parseFromString(Files.readString(Paths.get("/home/manuel/git/secret-test-print-samples/CGM-Turbomed/XML/Bundle1.xml")));

        List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createMultipleERezeptsOnPrescriptionServer(Arrays.asList(bundles));
        DocumentService documentService = new DocumentService();
        documentService.init();
        ByteArrayOutputStream a = documentService.generateERezeptPdf(bundleWithAccessCodeOrThrowable);
        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mmX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        Files.write(Paths.get("target/E-Rezept-" + thisMoment + ".pdf"), a.toByteArray());
    }

    @Test
    @Disabled
    void testCreateERezeptOnPrescriptionServer2() throws IOException, ERezeptWorkflowException, FOPException, TransformerException {
        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/281a985c-f25b-4aae-91a6-41ad744080b0.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(bundle);
        DocumentService documentService = new DocumentService();
        documentService.init();
        ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        Files.write(Paths.get("target/E-Rezept-" + thisMoment + ".pdf"), a.toByteArray());
    }

    @Test
    @Disabled
    void testCreateERezeptOnPrescriptionServerX110479894() throws IOException, ERezeptWorkflowException, FOPException, TransformerException {
        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/X110479894.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(bundle);
        DocumentService documentService = new DocumentService();
        documentService.init();
        ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        Files.write(Paths.get("target/E-Rezept-" + thisMoment + ".pdf"), a.toByteArray());
    }


    @Test
    @Disabled
    void testCreateERezeptOnPrescriptionServerX110493020() throws IOException, ERezeptWorkflowException, FOPException, TransformerException {

        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/X110493020.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(bundle);
        DocumentService documentService = new DocumentService();
        documentService.init();
        ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        Files.write(Paths.get("target/E-Rezept-" + thisMoment + ".pdf"), a.toByteArray());
    }


    @Test
    @Disabled
    void testCreateERezeptOnPrescriptionServerX110433911() throws IOException, ERezeptWorkflowException, FOPException, TransformerException {

        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/X110433911.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(bundle);
        DocumentService documentService = new DocumentService();
        documentService.init();
        ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        Files.write(Paths.get("target/E-Rezept-" + thisMoment + ".pdf"), a.toByteArray());
    }

    @Test
    @Disabled
    void testCreateERezeptOnPrescriptionServerX110452075() throws IOException, ERezeptWorkflowException, FOPException, TransformerException {

        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/X110452075.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(bundle);
        DocumentService documentService = new DocumentService();
        documentService.init();
        ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        Files.write(Paths.get("target/E-Rezept-" + thisMoment + ".pdf"), a.toByteArray());
    }

    @Test
    @Disabled
    void testCreateERezeptWithPrescriptionBuilderOnPrescriptionServer() throws ParseException {
        List<Bundle> bundles = new PrescriptionBundlesBuilder(
                PrescriptionBundlesBuilderTest.getMuster16PrescriptionFormForTests()).createBundles();

        bundles.forEach(bundle -> {
            log.info(iParser.encodeResourceToString(bundle));
            try {
                eRezeptWorkflowService.createERezeptOnPrescriptionServer(bundle);
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
                    eRezeptWorkflowService.createERezeptOnPrescriptionServer(bundle);
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
        Task task = eRezeptWorkflowService.createERezeptTask();
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
    void testSignBatchDocument() throws IOException, ERezeptWorkflowException {
        Bundle bundle1 = new Bundle();
        Bundle bundle2 = new Bundle();

        eRezeptWorkflowService.signBundleWithIdentifiers(Arrays.asList(bundle1, bundle2), false);
    }

    @Test
    @Disabled
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testUpdateERezeptTask() throws DataFormatException, IOException {
        Task task = iParser.parseResource(Task.class, new FileInputStream("target/titus-eRezeptWorkflowService-createERezeptTask.xml"));
        byte[] signedBytes = Files.readAllBytes(Paths.get("target/titus-eRezeptWorkflowService-signBundleWithIdentifiers.dat"));
        String accessCode = new String(Files.readAllBytes(Paths.get("target/titus-eRezeptWorkflowService-accessToken.txt")));
        eRezeptWorkflowService.updateERezeptTask(task, accessCode, signedBytes);
    }

    @Test
    @Disabled
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testAbortERezeptTask() throws DataFormatException, IOException {
        String accessCode = new String(Files.readAllBytes(Paths.get("target/titus-eRezeptWorkflowService-accessToken.txt")));
        String taskId = new String(Files.readAllBytes(Paths.get("target/titus-eRezeptWorkflowService-taskId.txt")));
        eRezeptWorkflowService.abortERezeptTask(taskId, accessCode);
    }

    @Test
    @Disabled
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testDeactivateComfortSignature() throws ERezeptWorkflowException {
        eRezeptWorkflowService.deactivateComfortSignature();
    }

    @Test
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testToggleComfortSignature() throws ERezeptWorkflowException {
        eRezeptWorkflowService.activateComfortSignature();
        eRezeptWorkflowService.getSignatureMode();
        eRezeptWorkflowService.deactivateComfortSignature();
    }
    
    @Test
    // @Disabled
    void testCreateERezeptWithPrescriptionBuilderOnPrescriptionServerRuntimeConfigTitus() throws ParseException {
    	// Titus: ssh -R 1501:kon-instanz2.titus.ti-dienste.de:443 -R 1502:idp.erezept-instanz1.titus.ti-dienste.de:443 -R 1503:fd.erezept-instanz1.titus.ti-dienste.de:443 manuel@localhost
    	
    	RuntimeConfig runtimeConfig = new RuntimeConfig();
    	runtimeConfig.getConfigurations().setConnectorBaseURL("https://localhost:1501");
    	runtimeConfig.getConfigurations().setClientCertificate("data:application/x-pkcs12;base64,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");
    	runtimeConfig.getConfigurations().setClientCertificatePassword("00");
    	
    	runtimeConfig.setIdpBaseURL("https://localhost:1502");
    	runtimeConfig.setPrescriptionServerURL("https://localhost:1503");
    	
        List<Bundle> bundles = new PrescriptionBundlesBuilder(
                PrescriptionBundlesBuilderTest.getMuster16PrescriptionFormForTests()).createBundles();

        bundles.forEach(bundle -> {
            log.info(iParser.encodeResourceToString(bundle));
            try {
                eRezeptWorkflowService.createERezeptOnPrescriptionServer(bundle, runtimeConfig);
            } catch (ERezeptWorkflowException e) {
                e.printStackTrace();
            }
        });
    }
    
    @Test
    // @Disabled
    void testCreateERezeptWithPrescriptionBuilderOnPrescriptionServerRuntimeConfigRU() throws ParseException {
    	// RU: ssh -R 1501:10.0.0.98:443 -R 1502:idp-ref.zentral.idp.splitdns.ti-dienste.de:443 -R 1503:erp-ref.zentral.erp.splitdns.ti-dienste.de:443 manuel@localhost
    	
    	RuntimeConfig runtimeConfig = new RuntimeConfig();
    	runtimeConfig.getConfigurations().setConnectorBaseURL("https://localhost:1501");
    	runtimeConfig.getConfigurations().setMandantId("Incentergy");
    	runtimeConfig.getConfigurations().setWorkplaceId("1786_A1");
    	runtimeConfig.getConfigurations().setClientSystemId("Incentergy");
    	
    	runtimeConfig.getConfigurations().setClientCertificate("file:///home/manuel/Desktop/RU-Connector-Cert/incentergy_U9pRlw8SBfMExkycgNDs.p12");
    	runtimeConfig.getConfigurations().setClientCertificatePassword("U9pRlw8SBfMExkycgNDs");
    	
    	runtimeConfig.setIdpBaseURL("https://localhost:1502");
    	runtimeConfig.setPrescriptionServerURL("https://localhost:1503");
    	
        List<Bundle> bundles = new PrescriptionBundlesBuilder(
                PrescriptionBundlesBuilderTest.getMuster16PrescriptionFormForTests()).createBundles();

        bundles.forEach(bundle -> {
            log.info(iParser.encodeResourceToString(bundle));
            try {
                eRezeptWorkflowService.createERezeptOnPrescriptionServer(bundle, runtimeConfig);
            } catch (ERezeptWorkflowException e) {
                e.printStackTrace();
            }
        });
    }
}
