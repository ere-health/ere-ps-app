package health.ere.ps.service.gematik;

import java.io.ByteArrayOutputStream;
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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
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
public class MassGeneratorTest {

    private static Logger log = Logger.getLogger(MassGeneratorTest.class.getName());

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
        int i = 0;
        DocumentService documentService = new DocumentService();
                documentService.init();
                
        RuntimeConfig runtimeConfig = getRuntimeConfig();

        Map<Path, Bundle> bundles = new LinkedHashMap<>();
        
        List<String> cards = Files.readAllLines(Paths.get("../secret-test-print-samples/Noventi/egk/Versicherte_20220214.csv"));

        List<Map<String,String>> versicherte = cards.stream().map(s -> {
            String[] a = s.split("\\|");
            Map<String,String> h = new HashMap<>();
            h.put("nummer", a[0]);
            h.put("prefix", a[1]);
            h.put("given", a[2]);
            h.put("family", a[3]);
            h.put("streetName", a[4]);
            h.put("houseNumber", a[5]);
            h.put("postalCode", a[6]);
            h.put("city", a[7]);
            h.put("birthdate", a[8]);
            return h;
        }).collect(Collectors.toList());


        // eRezeptWorkflowService.activateComfortSignature(runtimeConfig);
        String thisMomentString = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ssX")
                                .withZone(ZoneOffset.UTC)
                                .format(Instant.now());
        FileWriter fw = new FileWriter("target/e-rezept-report-"+thisMomentString+".csv");
        fw.write("Id,Filename,AccessCode,Patient\n");
        for(Map<String,String> card : versicherte) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("../secret-test-print-samples/Noventi/templates/"), "*.{xml}")) {

                for (Path entry : stream) {
                    InputStream fileStream = new FileInputStream(entry.toFile());
                    String template = new String(fileStream.readAllBytes(), "UTF-8");
                    fileStream.close();
                    template = replaceTemplates(template);
                    Bundle bundle = iParser.parseResource(Bundle.class, template);
                    bundle.setId(UUID.randomUUID().toString());
                    try {
                        ((MedicationRequest)bundle.getEntry().stream().filter(e -> e.getResource() instanceof MedicationRequest).findAny().get().getResource()).setAuthoredOnElement(new DateTimeType(new Date(), TemporalPrecisionEnum.DAY));
                    } catch(NoSuchElementException ex) {
                        ex.printStackTrace();
                    }
                    if(entry.toFile().getName().equals("eRezept_REZ_3.xml")) {
                    	Patient patient = ((Patient)bundle.getEntry().stream().filter(e -> e.getResource() instanceof Patient).findAny().get().getResource());
                        patient.getIdentifier().get(0).setValue(card.get("nummer"));
                        
                        StringType family = patient.getName().get(0).getFamilyElement();
                        if(family.getValueAsString().length() > 40) {                        	
                        	family.setValue(family.getValueAsString().substring(0, 40));
                        	family.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/humanname-own-name").setValue(new StringType(family.getValueAsString().substring(0, 40)));
                        }
                        
                    } else {
	                    try {
	                        Patient patient = ((Patient)bundle.getEntry().stream().filter(e -> e.getResource() instanceof Patient).findAny().get().getResource());
	                        patient.getIdentifier().get(0).setValue(card.get("nummer"));
	                        patient.getName().get(0).getGiven().get(0).setValue(card.get("given"));
	                        StringType family = patient.getName().get(0).getFamilyElement();
	                        family.setValue(card.get("family"));
	                        family.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/humanname-own-name").setValue(new StringType(card.get("family")));
	
	                        if(!"".equals(card.get("prefix"))) {
	                            List<StringType> prefixList = new ArrayList<StringType>();
	                            StringType prefix = new StringType(card.get("prefix"));
	                            Extension extension = new Extension("http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier", new CodeType("AC"));
	                            prefix.addExtension(extension);
	                            prefixList.add(prefix);
	                            patient.getName().get(0).setPrefix(prefixList);
	                        }
	
	                        Address address = patient.getAddress().get(0);
	                        address.setCity(card.get("city"));
	                        address.setPostalCode(card.get("postalCode"));
                            if(address.getLine().get(0).getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-postBox") == null) {
                                StringType line = address.getLine().get(0);
                                line.setValue(card.get("streetName")+" "+card.get("houseNumber"));
                                Extension streetName = line.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName");
                                if(streetName == null) {
                                    streetName = new Extension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName");
                                    line.addExtension(streetName);
                                }
                                streetName.setValue(new StringType(card.get("streetName")));
                                Extension houseNumber = line.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber");
                                if(houseNumber == null) {
                                    houseNumber = new Extension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber");
                                    line.addExtension(houseNumber);
                                }
                                
                                houseNumber.setValue(new StringType(card.get("houseNumber")));
                            }
	
	                        patient.setBirthDate(new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
	                                .parse(card.get("birthdate")));
	                    
	                    } catch(Exception ex) {
	                        ex.printStackTrace();
	                    }
                    }
                    // System.out.println(iParser.encodeResourceToString(bundle));
                    //if(2 == 1+1) {
                    //    break;
                    //}
                    ValidationResult validationResult = prescriptionBundleValidator.validateResource(bundle, true);
                    if(validationResult.isSuccessful()) {
                        List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowables;
                        bundles.put(entry, bundle);
                        if(bundles.size() == 30) {

                            try {
                                bundleWithAccessCodeOrThrowables = eRezeptWorkflowService.createMultipleERezeptsOnPrescriptionServer(new ArrayList<Bundle>(bundles.values()), runtimeConfig);
                                
                            } catch(Exception ex) {
                                bundleWithAccessCodeOrThrowables = Arrays.asList(new BundleWithAccessCodeOrThrowable(ex));
                                ex.printStackTrace();
                            }
                            int z = 0;
                            for(BundleWithAccessCodeOrThrowable bundleWithAccessCodeOrThrowable : bundleWithAccessCodeOrThrowables) {
                                entry = new ArrayList<Path>(bundles.keySet()).get(z);
                                z++;
                                String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ssX")
                                        .withZone(ZoneOffset.UTC)
                                        .format(Instant.now());
                                        
                                if(bundleWithAccessCodeOrThrowable.getThrowable() != null) {
                                    StringWriter sw = new StringWriter();
                                    PrintWriter pw = new PrintWriter(sw);
                                    bundleWithAccessCodeOrThrowable.getThrowable().printStackTrace(pw);
                                    Files.write(Paths.get("target/"+entry.toFile().getName().replace(".xml", "")+"-"+card.get("nummer")+"-"+ thisMoment + ".txt"), sw.toString().getBytes());
                                    Files.write(Paths.get("target/"+entry.toFile().getName().replace(".xml", "")+"-"+card.get("nummer")+"-"+ thisMoment + ".xml"), iParser.encodeResourceToString(bundleWithAccessCodeOrThrowable.getBundle()).getBytes());
                                } else {
                                    ByteArrayOutputStream a = documentService.generateERezeptPdf(Arrays.asList(bundleWithAccessCodeOrThrowable));
                                    String fileName = entry.toFile().getName().replace(".xml", "")+"-"+card.get("nummer")+"-" + thisMoment + ".pdf";
                                    Files.write(Paths.get("target/"+fileName), a.toByteArray());
                                    fw.write(bundleWithAccessCodeOrThrowable.getBundle().getIdentifier().getValue()+","+fileName+","+bundleWithAccessCodeOrThrowable.getAccessCode()+","+card.get("nummer")+"\n");
                                    fw.flush();
                                }
        
                                log.info("Time: "+thisMoment);
                            }
                            bundles.clear();
                        }
                    } else {
                        log.info(entry.toFile().getName()+" is not valid");
                    }
                    i++;
                    if(i % 200 == 0) {
                        // RezeptWorkflowService.deactivateComfortSignature(runtimeConfig);
                        // eRezeptWorkflowService.activateComfortSignature(runtimeConfig);
                    }
                }
            } catch (Exception ex) {
                // I/O error encounted during the iteration, the cause is an IOException
                log.info("Exception: "+ex);
                    
                ex.printStackTrace();
            }
            // break;
        }
        // eRezeptWorkflowService.deactivateComfortSignature(runtimeConfig);
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
    
    private RuntimeConfig getRuntimeConfig() throws FaultMessage {
        String cardHandleVincenzkrankenhaus = eRezeptWorkflowService.getCards().getCards()
            .getCard().stream().filter(ch -> "VincenzkrankenhausTEST-ONLY".equals(ch.getCardHolderName())).findFirst().get().getCardHandle();
        
        RuntimeConfig runtimeConfig = new RuntimeConfig();
        runtimeConfig.copyValuesFromUserConfig(userConfig);
        runtimeConfig.setSMCBHandle(cardHandleVincenzkrankenhaus);
        return runtimeConfig;
    }

}
