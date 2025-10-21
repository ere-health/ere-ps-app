package health.ere.ps.service.gematik;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;
import health.ere.ps.exception.gematik.ERezeptWorkflowException;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.profile.TitusTestProfile;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.connector.endpoint.SSLUtilities;
import health.ere.ps.service.fhir.prescription.PrescriptionService;
import health.ere.ps.service.idp.BearerTokenService;
import health.ere.ps.service.idp.client.IdpClient;
import health.ere.ps.service.pdf.DocumentService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.apache.fop.apps.FOPException;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
public class ERezeptWorkflowServiceTest {

    private static final Logger log = Logger.getLogger(ERezeptWorkflowServiceTest.class.getName());
    private final IParser iParser = FhirContext.forR4().newXmlParser();

    @Inject
    ERezeptWorkflowService eRezeptWorkflowService;

    @Inject
    IdpClient idpClient;
    @Inject
    BearerTokenService bearerTokenService;
    @Inject
    PrescriptionService prescriptionService;
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

    public BundleWithAccessCodeOrThrowable createERezeptOnPrescriptionServer(Bundle bundle) throws ERezeptWorkflowException {
        return eRezeptWorkflowService.createERezeptOnPrescriptionServer(bundle, null, null, null);
    }

    @Test
    @Tag("titus")
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
        String cardHandle = connectorCardsService.getConnectorCardHandle(ConnectorCardsService.CardHandleType.SMC_B);
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
                BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = createERezeptOnPrescriptionServer(bundle);
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
        String cardHandle = connectorCardsService.getConnectorCardHandle(ConnectorCardsService.CardHandleType.SMC_B);
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
                if(medicationProfile.equals("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.2")) {
                    type = "PZN";
                } else if(medicationProfile.equals("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText|1.0.2")) {
                    type = "FreeText";
                } else if(medicationProfile.equals("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient|1.0.2")) {
                    type = "Ingredient";
                } else if(medicationProfile.equals("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Compounding|1.0.2")) {
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
                BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = createERezeptOnPrescriptionServer(bundle);
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
        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/bundle-samples/bundle_July_2.xml"));
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = createERezeptOnPrescriptionServer(bundle);
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
    void testCreateERezeptOnPrescriptionServerFromXMLBundle() throws IOException, FOPException, TransformerException {
        Bundle[] bundles = prescriptionService.parseFromString(Files.readString(Paths.get("/home/manuel/git/secret-test-print-samples/CGM-Turbomed/XML/Bundle1.xml")));

        List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createMultipleERezeptsOnPrescriptionServer(
            Arrays.asList(bundles), null, null, null
        );
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
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = createERezeptOnPrescriptionServer(bundle);
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
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = createERezeptOnPrescriptionServer(bundle);
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
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = createERezeptOnPrescriptionServer(bundle);
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
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = createERezeptOnPrescriptionServer(bundle);
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
        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = createERezeptOnPrescriptionServer(bundle);
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
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testCreateERezeptTask() throws DataFormatException, IOException {
        Task task = eRezeptWorkflowService.createERezeptTask(true, null, "160");
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
        SignResponse signResponse = eRezeptWorkflowService.signBundleWithIdentifiers(bundleWithAccessCode.getBundle(), true, null, null, null);
        Files.write(Paths.get("target/titus-eRezeptWorkflowService-signBundleWithIdentifiers.dat"), signResponse.getSignatureObject().getBase64Signature().getValue());
    }

    @Test
    @Disabled
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testSignDocument() throws IOException, ERezeptWorkflowException {
        Bundle bundle = iParser.parseResource(Bundle.class, getClass().getResourceAsStream("/examples_erezept/Erezept_template_3.xml"));

        Task task = new Task();
        BundleWithAccessCodeOrThrowable bundleWithAccessCode = eRezeptWorkflowService.updateBundleWithTask(task, bundle);
        eRezeptWorkflowService.signBundleWithIdentifiers(bundleWithAccessCode.getBundle());
    }

    @Test
    @Disabled
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testSignBatchDocument() throws IOException, ERezeptWorkflowException {
        Bundle bundle1 = new Bundle();
        Bundle bundle2 = new Bundle();

        eRezeptWorkflowService.signBundleWithIdentifiers(Arrays.asList(bundle1, bundle2), false, null, null, null, true);
    }

    @Test
    @Disabled
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testUpdateERezeptTask() throws DataFormatException, IOException {
        Task task = iParser.parseResource(Task.class, new FileInputStream("target/titus-eRezeptWorkflowService-createERezeptTask.xml"));
        byte[] signedBytes = Files.readAllBytes(Paths.get("target/titus-eRezeptWorkflowService-signBundleWithIdentifiers.dat"));
        String accessCode = new String(Files.readAllBytes(Paths.get("target/titus-eRezeptWorkflowService-accessToken.txt")));
        String taskId = task.getIdElement().getIdPart();
        eRezeptWorkflowService.updateERezeptTask(taskId, accessCode, signedBytes, true, null, null, null);
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
    @Tag("titus")
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testToggleComfortSignature() throws ERezeptWorkflowException {
        eRezeptWorkflowService.activateComfortSignature();
        eRezeptWorkflowService.getSignatureMode();
        eRezeptWorkflowService.deactivateComfortSignature();
    }

    @Test
    @Tag("titus")
        // This is an integration test case that requires the manual usage of titus https://frontend.titus.ti-dienste.de/#/
    void testIsERezeptServiceReachable() throws ERezeptWorkflowException {
        String parameterBearerToken = bearerTokenService.requestBearerToken();
        assertTrue(eRezeptWorkflowService.isERezeptServiceReachable(null, parameterBearerToken));
    }
}