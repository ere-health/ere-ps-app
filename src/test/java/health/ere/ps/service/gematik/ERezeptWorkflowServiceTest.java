package health.ere.ps.service.gematik;

import static health.ere.ps.service.extractor.TemplateProfile.CGM_TURBO_MED;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DateTimeType;
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
@TestProfile(TitusTestProfile.class)
public class ERezeptWorkflowServiceTest {

    private static final Logger log = Logger.getLogger(ERezeptWorkflowServiceTest.class.getName());
    private final IParser iParser = FhirContext.forR4().newXmlParser();
    private String testBearerToken = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIiwiY3R5IjoiTkpXVCIsImV4cCI6MTYyNzMwNzYwNX0..s1njSJ1mM3HHzHnf.PhPXwMFHLxmWWaNYNIOcTrORCHqTuVy80DsWBzNJ5sTvY5kLdF5X4453102xVf3LLCtaQXwTCV2BbBn2RYGgjXyHs7lYpiR8rGbcNhWdtMQjlnMLhcwLyLjBiPMcHZwDvLcwQp1IbeelPMSYt1agKlMJpCfJVwRCSLyEalZUlPTYFkQ7M-F2sOEyGR2YelumXws64NIl8fDdv5wGUCZObBVBqFdI4p3BNJ_66bRCQV8SntCO2PbOyNhpmTthf_aSLewmhxJylE3IEbxTwspHIuHjk5XlJrJOfIIGEaHcXYsW91xtEoKu8vai_cKMDXFOHF7KCNrsuKwFddmXLy7AUyWHpaGtIGeh9dfj2kKhz5QB4yS1dSH3zQzPt2Zttleejvw2Cc0XMarVgQ5KzRSlW6Rjx38DCZzHD6EOtOAwsMj_wfw-v0b7ikCYapn4FXAC6Vh3G0KD8371niQiFzXZ4JrXv-4fRS6_Io1IsW9glaeoXfeQl5s-WQw4O5lcitJ_VExlbFkyG2cqpZXduO0oEoO_PRAlD7K9NxHiWrqiboa0G8L-eBWajRaz2oJA3qMBZUhvZay_MJpvIH7MGTydy1MU7RsoUHL8x_GIHSH1jVIE-GD-KtBkJ8PZAC_DCmn2Rh9tUXuoCPoDhrrSaoinoaZzsVZrVVHArJ4K5ALSDandypyyDIMlJB-pg0_PeY1k1BW8DVSOGhC3EhOMOgU21wwuaNajU1sRSndl77kg6DHWnZZmvb3Ofwf0MXi0nlI4JnNGX-S6Df1s3Afgwl5Uu9BYWiTZYmwRXqSUbS9gVa37xbJzaUqM1YdGkbVrqnIl6ahY9W4akdH9iHNwtDKejmhLIb9SsuXlNsawlJU-HnLu73wsRAfmtlqsWY7zLdfjBAFwMHzGG8FSo5gpYs-9XvIFDPm6mw1fF0_MlIB8J87706jBtNTDy-3icvjlJO_3pwcf4HIAKNOqpD-K6bEOKvXzMpNWpSvDnz4LQhDA009iFgGz_Vk421eMXpf3p6LDGXJKlpwtzB0031q6ePAl9ueqEWYSM5xxsldTebizdi2RHfakfuuUD9v30Xau7F92ol_n3SnghCkIVWQdPS80t4kSBwWDK6pOxvckiPtpA_T6Yq7OROvbkZ3C81muIuvbEOn2VFVq64p82xR335lEyCpVa8kZHHRy4__AfxBQ_f1EIqpZG8JfaoXsPjsraxTM8FioGEMRWMfnJ4qmeXtwEBVyyc1xWMvAs-p6a_mLem4n54I5xeF4Uw.lc4l3XNF3xtZUVNerDW1qA";

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
    void testCreateERezeptMassCreate() throws Exception {

        discoveryDocumentUrl = appConfig.getIdpBaseURL() + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;

        idpClient.init(appConfig.getIdpClientId(), appConfig.getIdpAuthRequestRedirectURL(), discoveryDocumentUrl, true);
        idpClient.initializeClient();

        String cardHandle = connectorCardsService.getConnectorCardHandle(
                ConnectorCardsService.CardHandleType.SMC_B);

        X509Certificate x509Certificate = cardCertificateReaderService.retrieveSmcbCardCertificate(cardHandle);

        IdpTokenResult idpTokenResult = idpClient.login(x509Certificate);

        log.info("Access Token: " + idpTokenResult.getAccessToken().getRawString());

        testBearerToken = idpTokenResult.getAccessToken().getRawString();

        int i = 0;
        DocumentService documentService = new DocumentService();
                documentService.init();
                
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("src/test/resources/simplifier_erezept/"), "*.{xml}")) {
            for (Path entry : stream) {
                Bundle bundle = iParser.parseResource(Bundle.class, new FileInputStream(entry.toFile()));
                try {
                    ((MedicationRequest)bundle.getEntry().stream().filter(e -> e.getResource() instanceof MedicationRequest).findAny().get().getResource()).setAuthoredOnElement(new DateTimeType(new Date(), TemporalPrecisionEnum.DAY));
                } catch(NoSuchElementException ex) {
                    ex.printStackTrace();
                } 
                BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
                ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
                String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ssX")
                        .withZone(ZoneOffset.UTC)
                        .format(Instant.now());
                Files.write(Paths.get("target/E-Rezept-" + thisMoment + ".pdf"), a.toByteArray());
                log.info("Time: "+thisMoment);
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
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testIsExired() {
        assertTrue(eRezeptWorkflowService.isExpired("eyJhbGciOiJCUDI1NlIxIiwidHlwIjoiYXQrSldUIiwia2lkIjoicHVrX2lkcF9zaWcifQ.eyJzdWIiOiJNU1lXUGYxVlJfaXdlNzFGQVBMVzJJY0YwemNlQTVqa0x2V1piWFlmSms0IiwicHJvZmVzc2lvbk9JRCI6IjEuMi4yNzYuMC43Ni40LjUwIiwib3JnYW5pemF0aW9uTmFtZSI6IjIwMjExMDEyMiBOT1QtVkFMSUQiLCJpZE51bW1lciI6IjEtMi1BUlpULVdhbHRyYXV0RHJvbWJ1c2NoMDEiLCJhbXIiOlsibWZhIiwic2MiLCJwaW4iXSwiaXNzIjoiaHR0cHM6Ly9pZHAuZXJlemVwdC1pbnN0YW56MS50aXR1cy50aS1kaWVuc3RlLmRlIiwiZ2l2ZW5fbmFtZSI6IldhbHRyYXV0IiwiY2xpZW50X2lkIjoiZ2VtYXRpa1Rlc3RQcyIsImFjciI6ImdlbWF0aWstZWhlYWx0aC1sb2EtaGlnaCIsImF1ZCI6Imh0dHBzOi8vZXJwLXRlc3QuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS8iLCJhenAiOiJnZW1hdGlrVGVzdFBzIiwic2NvcGUiOiJvcGVuaWQgZS1yZXplcHQiLCJhdXRoX3RpbWUiOjE2MjU1MjA2ODMsImV4cCI6MTYyNTUyMDk4MywiZmFtaWx5X25hbWUiOiJEcm9tYnVzY2giLCJpYXQiOjE2MjU1MjA2ODMsImp0aSI6ImI4MmMyMzgxYjQ1MTFjZGEifQ.K4qiZS6oSEe5izDiaIN-rBjcXzJM_y6HYUOpIEUKK-9evxEXco8BB4RJhfkagQJKwCgi11pctShMOs5seN1mOw"));
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
        Files.write(Paths.get("target/E-Rezept-" + thisMoment + ".pdf"), a.toByteArray());
    }

    @Test
    @Disabled
    void testCreateERezeptOnPrescriptionServerFromXMLBundle() throws IOException, ERezeptWorkflowException {
        Bundle[] bundles = XmlPrescriptionProcessor.parseFromString(Files.readString(Paths.get("/home/manuel/git/secret-test-print-samples/CGM-Turbomed/XML/Bundle1.xml")));

        List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createMultipleERezeptsOnPrescriptionServer(testBearerToken, Arrays.asList(bundles));
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
    void testCreateERezeptOnPrescriptionServer2() throws IOException, ERezeptWorkflowException {
        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/281a985c-f25b-4aae-91a6-41ad744080b0.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
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
    void testCreateERezeptOnPrescriptionServerX110479894() throws IOException, ERezeptWorkflowException {
        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/X110479894.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
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
    void testCreateERezeptOnPrescriptionServerX110493020() throws IOException, ERezeptWorkflowException {

        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/X110493020.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
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
    void testCreateERezeptOnPrescriptionServerX110433911() throws IOException, ERezeptWorkflowException {

        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/X110433911.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
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
    void testCreateERezeptOnPrescriptionServerX110452075() throws IOException, ERezeptWorkflowException {

        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/simplifier_erezept/X110452075.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
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
