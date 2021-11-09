package health.ere.ps.service.gematik;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.ValidationResult;
import health.ere.ps.config.AppConfig;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.profile.RUTestProfile;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.connector.endpoint.SSLUtilities;
import health.ere.ps.service.idp.client.IdpClient;
import health.ere.ps.service.pdf.DocumentService;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@Disabled
@TestProfile(RUTestProfile.class)
public class MassGenerator2Test {

    private static Logger log = Logger.getLogger(MassGenerator2Test.class.getName());

    private final static IParser iParser = FhirContext.forR4().newXmlParser();

    static {
        iParser.setPrettyPrint(true);
    }
    
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
                MassGenerator2Test.class
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
        createERezeptMassCreate("../secret-test-print-samples/CIDA/egk/cards.txt");
    }

    @Test
    void testCreateERezeptMassCreateManuel() throws Exception {
        createERezeptMassCreate("src/test/resources/manuels-egk/egk.txt", "src/test/resources/manuels-egk/insurance.txt");
    }

    @Test
    void testCreateERezeptMassCGMLauer() throws Exception {
        createERezeptMassCreate("src/test/resources/manuels-egk/egk.txt", "../secret-test-print-samples/CGM-Lauer/insurance.txt", "../secret-test-print-samples/CGM-Lauer/templates/");
    }

    void createERezeptMassCreate(String cardsString) throws Exception {
        createERezeptMassCreate(cardsString, null);
    }

    void createERezeptMassCreate(String cardsString, String insuranceString) throws Exception {
        createERezeptMassCreate(cardsString, insuranceString, "../secret-test-print-samples/CIDA/templates/");
    }
    void createERezeptMassCreate(String cardsString, String insuranceString, String templateFolder) throws Exception {
        int i = 0;
        DocumentService documentService = new DocumentService();
                documentService.init();
        
        List<String> cards = Files.readAllLines(Paths.get(cardsString));


        eRezeptWorkflowService.activateComfortSignature();
        String thisMomentString = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ssX")
                                .withZone(ZoneOffset.UTC)
                                .format(Instant.now());
        FileWriter fw = new FileWriter("target/e-rezept-report-"+thisMomentString+".csv");
        fw.write("Id,Filename,AccessCode,Patient,Insurance\n");

        List<String> insuranceList = Arrays.asList("Default");
        if(insuranceList != null) {
            insuranceList = Files.readAllLines(Paths.get(insuranceString));
        }
        for(String singleInsurance : insuranceList) {
            for(String card : cards) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(templateFolder), "*")) {
                    for (Path entry : stream) {
                        File file = entry.toFile();
                        List<File> bundleFile;
                        if(!file.isDirectory()) {
                            bundleFile = Arrays.asList(file);
                        } else {
                            bundleFile = Arrays.asList(file.listFiles());
                        }
                        List<Bundle> bundles = new ArrayList<>();
                        for(File myFile : bundleFile) {
                            InputStream fileStream = new FileInputStream(myFile);
                            String template = new String(fileStream.readAllBytes());
                            fileStream.close();
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
                            } catch(Exception ex) {
                                ex.printStackTrace();
                            }
                            if(!singleInsurance.equals("Default")) {
                                Coverage coverage = ((Coverage)bundle.getEntry().stream().filter(e -> e.getResource() instanceof Coverage).findAny().get().getResource());
                                ((Reference)coverage.getPayor().get(0)).getIdentifier().setValue(singleInsurance);
                            }
                            ValidationResult validationResult = prescriptionBundleValidator.validateResource(bundle, true);
                            if(validationResult.isSuccessful()) {
                                bundles.add(bundle);
                            } else {
                                log.info(entry.toFile().getName()+" is not valid");
                            }
                        }
                        // System.out.println(iParser.encodeResourceToString(bundle));
                        //if(2 == 1+1) {
                        //    if(bundles.size() == 0) {
                        //        continue;
                        //    }
                        //    Files.write(Paths.get("target/"+entry.toFile().getName().replace(".xml", "")+"-Single-Test.xml"), iParser.encodeResourceToString(bundles.get(0)).getBytes());
                        //    if(true) {
                        //        break;
                        //    }
                        //
                        //}
                    
                        List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowables = eRezeptWorkflowService.createMultipleERezeptsOnPrescriptionServer(bundles);
                        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ssX")
                                .withZone(ZoneOffset.UTC)
                                .format(Instant.now());
                                
                        if(bundleWithAccessCodeOrThrowables.size() > 0 && bundleWithAccessCodeOrThrowables.get(0).getThrowable() != null) {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            bundleWithAccessCodeOrThrowables.get(0).getThrowable().printStackTrace(pw);
                            Files.write(Paths.get("target/"+entry.toFile().getName().replace(".xml", "")+"-"+card+"-"+ thisMoment + ".txt"), sw.toString().getBytes());
                            if(bundleWithAccessCodeOrThrowables.get(0).getBundle() != null) {
                                Files.write(Paths.get("target/"+entry.toFile().getName().replace(".xml", "")+"-"+card+"-"+ thisMoment + ".xml"), iParser.encodeResourceToString(bundleWithAccessCodeOrThrowables.get(0).getBundle()).getBytes());
                            }
                            fw.flush();
                        } else if(bundleWithAccessCodeOrThrowables.size() > 0) {
                            ByteArrayOutputStream a = documentService.generateERezeptPdf(bundleWithAccessCodeOrThrowables);
                            String fileName = entry.toFile().getName().replace(".xml", "")+"-"+card+"-" + thisMoment + ".pdf";
                            Files.write(Paths.get("target/"+fileName), a.toByteArray());
                            for(BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable : bundleWithAccessCodeOrThrowables) {
                                fw.write(bundleWithAccessCodeOrThrowable.getBundle().getIdentifier().getValue()+","+fileName+","+bundleWithAccessCodeOrThrowable.getAccessCode()+","+card+","+singleInsurance+"\n");
                            }
                            fw.flush();
                        } else {
                            log.warning(file.getName()+" produced no bundleWithAccessCodeOrThrowables");
                        }

                        log.info("Time: "+thisMoment);
                        
                        i++;
                    }
                    if(i == 100) {
                        eRezeptWorkflowService.deactivateComfortSignature();
                        eRezeptWorkflowService.activateComfortSignature();
                    }
                } catch (Exception ex) {
                    // I/O error encounted during the iteration, the cause is an IOException
                    log.info("Exception: "+ex);
                        
                    ex.printStackTrace();
                }
                // break;
            }
        }
        eRezeptWorkflowService.deactivateComfortSignature();
        fw.close();
    }

}
