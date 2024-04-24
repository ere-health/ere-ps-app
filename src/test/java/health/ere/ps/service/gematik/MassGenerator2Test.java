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
import java.nio.file.StandardCopyOption;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jakarta.inject.Inject;

import org.gradle.internal.impldep.com.google.common.collect.Lists;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
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
    UserConfig userConfig;
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
    void testCreateERezeptMassCreateManuel2() throws Exception {
        createERezeptMassCreate("src/test/resources/manuels-egk/egk.txt");
    }
    @Test
    void testCreateERezeptMassCreateManuel3() throws Exception {
        createERezeptMassCreateBatch("src/test/resources/manuels-egk/egk.txt", null, "../secret-test-print-samples/CIDA/templates/", true);
    }
    @Test
    void testCreateERezeptMassCreateCIDA() throws Exception {
        createERezeptMassCreate("../secret-test-print-samples/CIDA/egk/cards.txt", null, "../secret-test-print-samples/CIDA/CIDA-11/", true);
    }
    @Test
    void testCreateERezeptMassCGMLauer() throws Exception {
        createERezeptMassCreate("src/test/resources/manuels-egk/egk.txt", "../secret-test-print-samples/CGM-Lauer/insurance.txt", "../secret-test-print-samples/CGM-Lauer/templates/");
    }

    @Test
    void testCreateERezeptMassGematik() throws Exception {
        createERezeptMassCreateBatch(null, null, "../secret-test-print-samples/gematik/", true);
    }

    @Test
    void testCreateERezeptMassGematik2() throws Exception {
        createERezeptMassCreateBatch(null, null, "../secret-test-print-samples/gematik2/", true);
    }

    @Test
    void testParsing() throws Exception {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("../secret-test-print-samples/gematik/"), "*")) {
            for (Path entry : stream) {
                InputStream fileStream = new FileInputStream(entry.toFile());
                String template = new String(fileStream.readAllBytes());
                fileStream.close();
                try {
                    Bundle bundle = iParser.parseResource(Bundle.class, template);
                } catch(Exception ex) {
                    log.info("File: "+entry.toFile().getAbsolutePath()+" could not be parsed.");
                    ex.printStackTrace();
                }
                
            }
        }
    }

    @Test
    void testCreateERezeptMassKonnektathonAOK_NO() throws Exception {
        createERezeptMassCreate(null, null, "../secret-test-print-samples/Konnektathon/");
    }

    void createERezeptMassCreate(String cardsString) throws Exception {
        createERezeptMassCreate(cardsString, null);
    }

    void createERezeptMassCreate(String cardsString, String insuranceString) throws Exception {
        createERezeptMassCreate(cardsString, insuranceString, "../secret-test-print-samples/CIDA/templates/");
    }

    void createERezeptMassCreate(String cardsString, String insuranceString, String templateFolder) throws Exception  {
        createERezeptMassCreate(cardsString, insuranceString, templateFolder, false);
    }

    void createERezeptMassCreate(String cardsString, String insuranceString, String templateFolder, boolean move) throws Exception {
        if(move) {
            File processed = new File(templateFolder+"/processed");
            if(!processed.exists()) {
                processed.mkdir();
            }
        }
        int i = 0;
        DocumentService documentService = new DocumentService();
                documentService.init();
        
        List<String> cards = cardsString != null ? Files.readAllLines(Paths.get(cardsString)) : Arrays.asList(new String[] {null});

        RuntimeConfig runtimeConfig = getRuntimeConfig();

        eRezeptWorkflowService.activateComfortSignature(runtimeConfig);
        String thisMomentString = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ssX")
                                .withZone(ZoneOffset.UTC)
                                .format(Instant.now());
        FileWriter fw = new FileWriter("target/e-rezept-report-"+thisMomentString+".csv");
        fw.write("Id,Filename,AccessCode,Patient,Insurance\n");

        List<String> insuranceList = Arrays.asList("Default");
        if(insuranceString != null) {
            insuranceList = Files.readAllLines(Paths.get(insuranceString));
        }
        for(String singleInsurance : insuranceList) {
            for(String card : cards) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(templateFolder), "*")) {
                    for (Path entry : stream) {
                        File file = entry.toFile();
                        List<File> bundleFile;
                        String directoryName = "";
                        if(!file.isDirectory()) {
                            bundleFile = Arrays.asList(file);
                            directoryName = "";
                        } else if(!file.getName().equals("processed")) {
                            bundleFile = Arrays.asList(file.listFiles());
                            directoryName = file.getName()+"-";
                        } else {
                            continue;
                        }
                        List<Bundle> bundles = new ArrayList<>();
                        for(File myFile : bundleFile) {
                            InputStream fileStream = new FileInputStream(myFile);
                            String template = new String(fileStream.readAllBytes());
                            fileStream.close();
                            if(move) {
                                Files.move(myFile.toPath(), Paths.get(templateFolder+"/processed/"+directoryName+myFile.getName()), StandardCopyOption.REPLACE_EXISTING);
                            }
                            
                            Bundle bundle = iParser.parseResource(Bundle.class, template);
                            bundle.setId(UUID.randomUUID().toString());
                            try {
                                ((MedicationRequest)bundle.getEntry().stream().filter(e -> e.getResource() instanceof MedicationRequest).findAny().get().getResource()).setAuthoredOnElement(new DateTimeType(new Date(), TemporalPrecisionEnum.DAY));
                            } catch(NoSuchElementException ex) {
                                ex.printStackTrace();
                            }
                            try {
                                if(card != null) {
                                    Patient patient = ((Patient)bundle.getEntry().stream().filter(e -> e.getResource() instanceof Patient).findAny().get().getResource());
                                    patient.getIdentifier().get(0).setValue(card);
                                }
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
                    
                        List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowables = eRezeptWorkflowService.createMultipleERezeptsOnPrescriptionServer(bundles, runtimeConfig);
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
                        if((i % 90) == 0) {
                            eRezeptWorkflowService.deactivateComfortSignature(runtimeConfig);
                            eRezeptWorkflowService.activateComfortSignature(runtimeConfig);
                        }
                    }
                } catch (Exception ex) {
                    // I/O error encounted during the iteration, the cause is an IOException
                    log.info("Exception: "+ex);
                        
                    ex.printStackTrace();
                }
                // break;
            }
        }
        eRezeptWorkflowService.deactivateComfortSignature(runtimeConfig);
        fw.close();
    }

    private RuntimeConfig getRuntimeConfig() throws FaultMessage {
        String cardHandleVincenzkrankenhaus = eRezeptWorkflowService.getCards().getCards()
            .getCard().stream().filter(ch -> "VincenzkrankenhausTEST-ONLY".equals(ch.getCardHolderName())).findFirst().get().getCardHandle();
        
        RuntimeConfig runtimeConfig = new RuntimeConfig();
        runtimeConfig.copyValuesFromUserConfig(userConfig);
        runtimeConfig.setSMCBHandle(cardHandleVincenzkrankenhaus);
        return runtimeConfig;
    }

    void createERezeptMassCreateBatch(String cardsString, String insuranceString, String templateFolder, boolean move) throws Exception {
        if(move) {
            File processed = new File(templateFolder+"/processed");
            if(!processed.exists()) {
                processed.mkdir();
            }
        }
        int i = 0;
        DocumentService documentService = new DocumentService();
                documentService.init();
        
        List<String> cards = cardsString != null ? Files.readAllLines(Paths.get(cardsString)) : Arrays.asList(new String[] {"X110486750"});

        RuntimeConfig runtimeConfig = getRuntimeConfig();

        eRezeptWorkflowService.activateComfortSignature(runtimeConfig);
        String thisMomentString = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ssX")
                                .withZone(ZoneOffset.UTC)
                                .format(Instant.now());
        FileWriter fw = new FileWriter("target/e-rezept-report-"+thisMomentString+".csv");
        fw.write("Id,Filename,AccessCode,Patient,Insurance,PZN,Medication,IKNR,Krankenkasse,Qualfication\n");

        List<String> insuranceList = Arrays.asList("Default");
        if(insuranceString != null) {
            insuranceList = Files.readAllLines(Paths.get(insuranceString));
        }
        for(String singleInsurance : insuranceList) {
            for(String card : cards) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(templateFolder), "*.xml")) {

                    List<Path> listPath = StreamSupport.stream(stream.spliterator(), false).collect(Collectors.toList());

                    for (List<Path> entry : Lists.partition(listPath, 30)) {
                        List<File> bundleFile = entry.stream().map(s -> s.toFile()).collect(Collectors.toList());
                        List<Bundle> bundles = new ArrayList<>();
                        for(File myFile : bundleFile) {
                            InputStream fileStream = new FileInputStream(myFile);
                            String template = new String(fileStream.readAllBytes());
                            fileStream.close();
                            if(move) {
                                Files.move(myFile.toPath(), Paths.get(templateFolder+"/processed/"+myFile.getName()), StandardCopyOption.REPLACE_EXISTING);
                            }
                            
                            Bundle bundle = iParser.parseResource(Bundle.class, template);
                            bundle.setId(UUID.randomUUID().toString());
                            try {
                                ((MedicationRequest)bundle.getEntry().stream().filter(e -> e.getResource() instanceof MedicationRequest).findAny().get().getResource()).setAuthoredOnElement(new DateTimeType(new Date(), TemporalPrecisionEnum.DAY));
                            } catch(NoSuchElementException ex) {
                                ex.printStackTrace();
                            }
                            try {
                                if(card != null) {
                                    Patient patient = ((Patient)bundle.getEntry().stream().filter(e -> e.getResource() instanceof Patient).findAny().get().getResource());
                                    patient.getIdentifier().get(0).setValue(card);
                                }
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
                                log.info(myFile.getName()+" is not valid");
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
                    
                        List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowables = eRezeptWorkflowService.createMultipleERezeptsOnPrescriptionServer(bundles, runtimeConfig);
                        String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ssX")
                                .withZone(ZoneOffset.UTC)
                                .format(Instant.now());
                        
                        int z = 0;
                        for(BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable : bundleWithAccessCodeOrThrowables) {
                            File fileEntry = listPath.get(z).toFile();
                            if(bundleWithAccessCodeOrThrowable.getThrowable() != null) {
                                StringWriter sw = new StringWriter();
                                PrintWriter pw = new PrintWriter(sw);
                                bundleWithAccessCodeOrThrowable.getThrowable().printStackTrace(pw);
                                Files.write(Paths.get("target/"+fileEntry.getName().replace(".xml", "")+"-"+card+"-"+ thisMoment + ".txt"), sw.toString().getBytes());
                                if(bundleWithAccessCodeOrThrowable.getBundle() != null) {
                                    Files.write(Paths.get("target/"+fileEntry.getName().replace(".xml", "")+"-"+card+"-"+ thisMoment + ".xml"), iParser.encodeResourceToString(bundleWithAccessCodeOrThrowable.getBundle()).getBytes());
                                }
                                fw.flush();
                            } else {
                                ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
                                String fileName = fileEntry.getName().replace(".xml", "")+"-"+card+"-" + thisMoment + ".pdf";
                                Files.write(Paths.get("target/"+fileName), a.toByteArray());
                                Bundle bundle = bundleWithAccessCodeOrThrowable.getBundle();
                                MedicationRequest medicationRequest = null;
                                try {
                                    medicationRequest = ((MedicationRequest)bundle.getEntry().stream().filter(e -> e.getResource() instanceof MedicationRequest).findAny().get().getResource());
                                } catch(NoSuchElementException ex) {
                                    ex.printStackTrace();
                                }
                                Medication medication = null;
                                try {
                                    medication = ((Medication)bundle.getEntry().stream().filter(e -> e.getResource() instanceof Medication).findAny().get().getResource());
                                } catch(NoSuchElementException ex) {
                                    ex.printStackTrace();
                                }
                                Patient patient = null;
                                try {
                                    patient = ((Patient)bundle.getEntry().stream().filter(e -> e.getResource() instanceof Patient).findAny().get().getResource());
                                } catch(Exception ex) {
                                    ex.printStackTrace();
                                }
                                String qualification = "";
                                Practitioner practitioner = null;
                                try {
                                    practitioner = ((Practitioner)bundle.getEntry().stream().filter(e -> e.getResource() instanceof Practitioner).findAny().get().getResource());
                                    qualification = practitioner.getQualification().get(0).getCode().getCoding().get(0).getCode();
                                    if(Math.random() > 0.7) {
                                        qualification = "01";
                                        practitioner.getQualification().get(0).getCode().getCoding().get(0).setCode("01");
                                    }
                                } catch(Exception ex) {
                                    ex.printStackTrace();
                                }

                                Coverage coverage = null;
                                try {
                                    coverage = ((Coverage)bundle.getEntry().stream().filter(e -> e.getResource() instanceof Coverage).findAny().get().getResource());
                                } catch(Exception ex) {
                                    ex.printStackTrace();
                                }
                                
                                fw.write(bundle.getIdentifier().getValue()+","+fileName+","+bundleWithAccessCodeOrThrowable.getAccessCode()+","+card+","
                                +singleInsurance+","+medication.getCode().getCoding().get(0).getCode()+","+
                                medication.getCode().getText()+","+
                                coverage.getPayor().get(0).getIdentifier().getValue()+","+
                                coverage.getPayor().get(0).getDisplay()+","+
                                qualification+"\n");
                                fw.flush();
                            }
                            z++;
                        }

                        log.info("Time: "+thisMoment);
                        
                        i++;
                        if((i % 200) == 0) {
                            eRezeptWorkflowService.deactivateComfortSignature(runtimeConfig);
                            eRezeptWorkflowService.activateComfortSignature(runtimeConfig);
                        }
                    }
                } catch (Exception ex) {
                    // I/O error encounted during the iteration, the cause is an IOException
                    log.info("Exception: "+ex);
                        
                    ex.printStackTrace();
                }
                // break;
            }
        }
        eRezeptWorkflowService.deactivateComfortSignature(runtimeConfig);
        fw.close();
    }

}
