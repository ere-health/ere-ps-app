package health.ere.ps.service.gematik;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.ValidationResult;
import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.gematik.ERezeptWorkflowException;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.profile.RUTestProfile;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.connector.endpoint.SSLUtilities;
import health.ere.ps.service.idp.client.IdpClient;
import health.ere.ps.service.idp.client.IdpHttpClientService;
import health.ere.ps.service.pdf.DocumentService;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@Disabled
@TestProfile(RUTestProfile.class)
public class MassGeneratorTest {

    private static Logger log = Logger.getLogger(MassGeneratorTest.class.getName());

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
    @Inject
    PrescriptionBundleValidator prescriptionBundleValidator;

    String discoveryDocumentUrl;

    @BeforeEach
    void init() {
        try {
            // https://community.oracle.com/thread/1307033?start=0&tstart=0
            LogManager.getLogManager().readConfiguration(
                MassGeneratorTest.class
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
    void testCreateERezeptMassCreate() throws Exception {

        discoveryDocumentUrl = appConfig.getIdpBaseURL() + IdpHttpClientService.DISCOVERY_DOCUMENT_URI;

        idpClient.init(appConfig.getIdpClientId(), appConfig.getIdpAuthRequestRedirectURL(), discoveryDocumentUrl, true);
        idpClient.initializeClient();

        String cardHandle = connectorCardsService.getConnectorCardHandle(
                ConnectorCardsService.CardHandleType.SMC_B);

        X509Certificate x509Certificate = cardCertificateReaderService.retrieveSmcbCardCertificate(cardHandle);

        IdpTokenResult idpTokenResult = idpClient.login(x509Certificate);

        log.info("Access Token: " + idpTokenResult.getAccessToken().getRawString());

        String testBearerToken = idpTokenResult.getAccessToken().getRawString();

        int i = 0;
        DocumentService documentService = new DocumentService();
                documentService.init();
        
        List<String> cards = Files.readAllLines(Paths.get("../secret-test-print-samples/Noventi/egk/cards.txt"));

        eRezeptWorkflowService.activateComfortSignature();
        String thisMomentString = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ssX")
                                .withZone(ZoneOffset.UTC)
                                .format(Instant.now());
        FileWriter fw = new FileWriter("target/e-rezept-report-"+thisMomentString+".csv");
        fw.write("Id,Filename,AccessCode,Patient\n");
        for(String card : cards) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("../secret-test-print-samples/Noventi/templates/"), "*.{xml}")) {

                for (Path entry : stream) {
                    InputStream fileStream = new FileInputStream(entry.toFile());
                    String template = new String(fileStream.readAllBytes());
                    fileStream.close();
                    template = replaceTemplates(template);
                    Bundle bundle = iParser.parseResource(Bundle.class, template);
                    bundle.setId(UUID.randomUUID().toString());
                    try {
                        ((MedicationRequest)bundle.getEntry().stream().filter(e -> e.getResource() instanceof MedicationRequest).findAny().get().getResource()).setAuthoredOnElement(new DateTimeType(new Date(), TemporalPrecisionEnum.DAY));
                    } catch(NoSuchElementException ex) {
                        ex.printStackTrace();
                    }
                    try {
                        Patient patient = ((Patient)bundle.getEntry().stream().filter(e -> e.getResource() instanceof Patient).findAny().get().getResource());
                        patient.getIdentifier().get(0).setValue(card);
                    } catch(NoSuchElementException ex) {
                        ex.printStackTrace();
                    }
                    ValidationResult validationResult = prescriptionBundleValidator.validateResource(bundle, true);
                    if(validationResult.isSuccessful()) {
                        BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable = eRezeptWorkflowService.createERezeptOnPrescriptionServer(testBearerToken, bundle);
                        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ssX")
                                .withZone(ZoneOffset.UTC)
                                .format(Instant.now());
                                
                        if(bundleWithAccessCodeOrThrowable.getThrowable() != null) {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            bundleWithAccessCodeOrThrowable.getThrowable().printStackTrace(pw);
                            Files.write(Paths.get("target/"+entry.toFile().getName().replace(".xml", "")+" "+ thisMoment + ".txt"), sw.toString().getBytes());
                            Files.write(Paths.get("target/"+entry.toFile().getName().replace(".xml", "")+" "+ thisMoment + ".xml"), iParser.encodeResourceToString(bundleWithAccessCodeOrThrowable.getBundle()).getBytes());
                        } else {
                            ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
                            String fileName = entry.toFile().getName().replace(".xml", "")+"-" + thisMoment + ".pdf";
                            Files.write(Paths.get("target/"+fileName), a.toByteArray());
                            fw.write(bundleWithAccessCodeOrThrowable.getBundle().getIdentifier().getId()+","+fileName+","+bundleWithAccessCodeOrThrowable.getAccessCode()+","+card+"\n");
                        }

                        log.info("Time: "+thisMoment);
                    } else {
                        log.info(entry.toFile().getName()+" is not valid");
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
            // break;
        }
        eRezeptWorkflowService.deactivateComfortSignature();
        fw.close();
    }

    public String replaceTemplates(String xml) {
        String today = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                              .withZone(ZoneOffset.UTC)
                              .format(Instant.now());
        String in30Days = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now().plus(30, ChronoUnit.DAYS));
        // grep -P '{{[a-zA-Z-]*}}' -h -o  * | sort | uniq
        // date and authoredOn
        xml = xml.replaceAll("\\{\\{epres-date\\}\\}", today);
        xml = xml.replaceAll("\\{\\{epres-date-accident\\}\\}", today);
        xml = xml.replaceAll("\\{\\{epres-date-start-valid\\}\\}", today);
        xml = xml.replaceAll("\\{\\{epres-date-end-valid\\}\\}", in30Days);
        // will be set by Fachdienst
        xml = xml.replaceAll("\\{\\{epres-id\\}\\}", "160.000.000.000.00");
        xml = xml.replaceAll("\\{\\{technical-id\\}\\}", UUID.randomUUID().toString());
        return xml;
    }

}
