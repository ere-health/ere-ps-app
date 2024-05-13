package health.ere.ps.service.pdf;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

import javax.xml.transform.TransformerException;

import org.apache.fop.apps.FOPException;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import health.ere.ps.event.BundlesWithAccessCodeEvent;
import health.ere.ps.event.ERezeptWithDocumentsEvent;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.profile.TitusTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
public class DocumentServiceTest {

    private final static List<Bundle> testBundles = new ArrayList<>();
    private final static String TARGET_PATH = "./target/test_Erezepten/";
    private final static FhirContext ctx = FhirContext.forR4();

    @Inject
    DocumentService documentService;


    @BeforeAll
    public static void prepareTestDirectoryAndBundles() throws IOException {
        if (!Path.of(TARGET_PATH).toFile().exists()) {
            Files.createDirectory(Path.of(TARGET_PATH));
        }

        // GIVEN
        testBundles.add((Bundle) ctx.newXmlParser().parseResource(
                DocumentServiceTest.class.getResourceAsStream("/examples_erezept/Erezept_template_1.xml")));
        testBundles.add((Bundle) ctx.newXmlParser().parseResource(
                DocumentServiceTest.class.getResourceAsStream("/examples_erezept/Erezept_template_2.xml")));
        testBundles.add((Bundle) ctx.newXmlParser().parseResource(
                DocumentServiceTest.class.getResourceAsStream("/examples_erezept/Erezept_template_3.xml")));
        testBundles.add((Bundle) ctx.newXmlParser().parseResource(
                DocumentServiceTest.class.getResourceAsStream("/examples_erezept/Erezept_template_4.xml")));
        testBundles.add((Bundle) ctx.newXmlParser().parseResource(
                DocumentServiceTest.class.getResourceAsStream("/examples_erezept/Erezept_template_5.xml")));
        testBundles.add((Bundle) ctx.newXmlParser().parseResource(
                DocumentServiceTest.class.getResourceAsStream("/examples_erezept/Beispiel_16.xml")));
        testBundles.add((Bundle) ctx.newXmlParser().parseResource(
                DocumentServiceTest.class.getResourceAsStream("/examples_erezept/Beispiel_52.xml")));


        try {
            // https://community.oracle.com/thread/1307033?start=0&tstart=0
            LogManager.getLogManager().readConfiguration(
                DocumentServiceTest.class
                            .getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void onBundlesWithAccessCodes_respectsLimitOfMaxNumberOfMedicationsPerPrescription() {
        // GIVEN1
        int maxNumberOfMedicationsPerPrescription = 9;
        Event<ERezeptWithDocumentsEvent> mockedEvent = Mockito.mock(Event.class);
        documentService.seteRezeptDocumentsEvent(mockedEvent);

        List<BundleWithAccessCodeOrThrowable> bundles = new ArrayList<>();

        for (int i = 0; i < maxNumberOfMedicationsPerPrescription; i++) {
            bundles.add(new BundleWithAccessCodeOrThrowable(
                    (Bundle) ctx.newXmlParser().parseResource(
                            DocumentServiceTest.class.getResourceAsStream("/examples_erezept/Erezept_template_1.xml")),
                    "MOCK_CODE"));
        }

        // WHEN1
        documentService.onBundlesWithAccessCodes(new BundlesWithAccessCodeEvent(List.of(bundles)));

        // THEN1
        Mockito.verify(mockedEvent, Mockito.times(1)).fireAsync(Mockito.any());

        // GIVEN2
        Mockito.reset(mockedEvent);
        bundles.add(new BundleWithAccessCodeOrThrowable(
                (Bundle) ctx.newXmlParser().parseResource(
                        DocumentServiceTest.class.getResourceAsStream("/examples_erezept/Erezept_template_1.xml")),
                "MOCK_CODE"));

        // WHEN2
        documentService.onBundlesWithAccessCodes(new BundlesWithAccessCodeEvent(List.of(bundles)));

        // THEN2
        Mockito.verify(mockedEvent, Mockito.times(2)).fireAsync(Mockito.any());
    }

    @Test
    // @Disabled("Running the pdf generation tests takes a lot of time, run them manually")
    public void generateERezeptPdf_generatesCorrectPdf_givenOneMedicationToDisplay() throws IOException {
        // WHEN + THEN
        // DefaultFontConfigurator
        ByteArrayOutputStream baos = createStreamForANumberOfPdfs(1);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_one_medications.pdf"), baos.toByteArray());
    }

    @Test
    // @Disabled("Running the pdf generation tests takes a lot of time, run them manually")
    public void generateERezeptPdf_generatesCorrectPdf_givenOneCompoundingToDisplay() throws IOException, FOPException, TransformerException {
        // WHEN + THEN
        // DefaultFontConfigurator
        List<BundleWithAccessCodeOrThrowable> bundles = new ArrayList<>();
        bundles.add(new BundleWithAccessCodeOrThrowable(testBundles.get(6), "777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea"));
        ByteArrayOutputStream baos = documentService.generateERezeptPdf(bundles);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_one_compounding.pdf"), baos.toByteArray());
    }

    @Test
    @Disabled("Running the pdf generation tests takes a lot of time, run them manually")
    public void generateERezeptPdf_generatesCorrectPdf_givenOneMedicationToDisplayIngredient() throws IOException, FOPException, TransformerException {
        // WHEN + THEN
        // DefaultFontConfigurator
        List<BundleWithAccessCodeOrThrowable> bundles = new ArrayList<>();
        bundles.add(new BundleWithAccessCodeOrThrowable(testBundles.get(5), "MOCK_CODE"));
        ByteArrayOutputStream baos = documentService.generateERezeptPdf(bundles);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_one_medications_ingredient.pdf"), baos.toByteArray());
    }

    @Test
    @Disabled("Running the pdf generation tests takes a lot of time, run them manually")
    public void generateERezeptPdf_generatesCorrectPdf_givenTwoMedicationsToDisplay() throws IOException {
        // WHEN + THEN
        ByteArrayOutputStream baos = createStreamForANumberOfPdfs(2);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_two_medications.pdf"), baos.toByteArray());
    }

    @Test
    @Disabled("Running the pdf generation tests takes a lot of time, run them manually")
    public void generateERezeptPdf_generatesCorrectPdf_givenThreeMedicationsToDisplay() throws IOException {
        // WHEN + THEN
        ByteArrayOutputStream baos = createStreamForANumberOfPdfs(3);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_three_medications.pdf"), baos.toByteArray());
    }

    @Test
    @Disabled("Running the pdf generation tests takes a lot of time, run them manually")
    public void generateERezeptPdf_generatesCorrectPdfWithTwoPages_givenFourMedicationsToDisplay() throws IOException {
        // WHEN + THEN
        ByteArrayOutputStream baos = createStreamForANumberOfPdfs(4);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_four_medications.pdf"), baos.toByteArray());
    }

    @Test
    @Disabled("Running the pdf generation tests takes a lot of time, run them manually")
    public void generateERezeptPdf_generatesCorrectPdfWithTwoPages_givenFiveMedicationsToDisplay() throws IOException {
        // WHEN + THEN
        ByteArrayOutputStream baos = createStreamForANumberOfPdfs(5);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_five_medications.pdf"), baos.toByteArray());
    }

    @Test
    @Disabled("Running the pdf generation tests takes a lot of time, run them manually")
    public void generateERezeptPdf_generatesCorrectPdfWithTwoPages_givenSixMedicationsToDisplay() throws IOException {
        // WHEN + THEN
        ByteArrayOutputStream baos = createStreamForANumberOfPdfs(6);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_six_medications.pdf"), baos.toByteArray());
    }


    @Test
    @Disabled("Running the pdf generation tests takes a lot of time, run them manually")
    public void generateERezeptPdf_generatesCorrectPdfWithThreePages_givenEightMedicationsToDisplay() throws IOException {
        // WHEN + THEN
        ByteArrayOutputStream baos = createStreamForANumberOfPdfs(8);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_eight_medications.pdf"), baos.toByteArray());
    }

    @Test
    @Disabled("Running the pdf generation tests takes a lot of time, run them manually")
    public void generateERezeptPdf_generatesCorrectPdfWithThreePages_givenNineMedicationsToDisplay() throws IOException {
        // WHEN + THEN
        ByteArrayOutputStream generatedPdfsStream = createStreamForANumberOfPdfs(9);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_nine_medications.pdf"), generatedPdfsStream.toByteArray());
    }

    @Test
    @Disabled("Running the pdf generation tests takes a lot of time, run them manually")
    public void generateAllKBVPdf() throws IOException {
        String dir = "./src/test/resources/examples-kbv-fhir-erp-v1-0-2"; //todo: write to src? in a test? shouldn't this be a .gitignore test-result like folder?
        String prefix = "1_0_2";
        generatePdfsForAllFilesInFolder(dir, prefix);

        dir = "./src/test/resources/examples-kbv-fhir-erp-v1-1-0"; //todo: write to src? in a test? shouldn't this be a .gitignore test-result like folder?
        prefix = "1_1_0";
        generatePdfsForAllFilesInFolder(dir, prefix);
    }

    @Test
    @Disabled("Running the pdf generation tests takes a lot of time, run them manually")
    public void generatePdfsForTestingBundleFoldersViaHelperFunction() throws IOException {
        String dir = "./src/test/resources/secret/bundles-v1-1-0"; //todo: write to src? in a test? shouldn't this be a .gitignore test-result like folder?
        String prefix = "test_v1_1_0";
        generatePdfsForAllFilesInFolder(dir, prefix);
    }

    private void generatePdfsForAllFilesInFolder(String dir, String prefix) throws IOException {
        List<ByteArrayOutputStream> pdfStreams = Files.list(Paths.get(dir))
                .filter((p) -> p.toFile().isFile())
                .map((p) -> {
                    try {
                        if (p.getFileName().toString().endsWith(".xml")) {
                            return new BundleWithAccessCodeOrThrowable((Bundle) ctx.newXmlParser().parseResource(new FileReader(p.toFile())), "ACCESS_CODE");
                        } else if (p.getFileName().toString().endsWith(".json"))  {
                            return new BundleWithAccessCodeOrThrowable((Bundle) ctx.newJsonParser().parseResource(new FileReader(p.toFile())), "ACCESS_CODE");
                        } else {
                            return null;
                        }
                    } catch (ConfigurationException | DataFormatException | FileNotFoundException e) {
                        e.printStackTrace();
                        fail("Couldn't parse: " + p.getFileName().toString());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map((b) -> {
                try {
                    return documentService.generateERezeptPdf(Arrays.asList(b));
                } catch (FOPException | IOException | TransformerException e) {
                    e.printStackTrace();
                    fail("Couldn't generate pdf for bundle with medicationRequestId: " + b.getMedicationRequestId());
                    return null;
                }
            }).collect(Collectors.toList());
        for(int i = 0;i<pdfStreams.size();i++) {
            Files.write(Paths.get(TARGET_PATH + prefix+ "_" +i + ".pdf"), pdfStreams.get(i).toByteArray());
        }
    }


    private ByteArrayOutputStream createStreamForANumberOfPdfs(int number) {
        List<BundleWithAccessCodeOrThrowable> bundles = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            bundles.add(new BundleWithAccessCodeOrThrowable(testBundles.get(i % 6), "777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea" + i));
        }
        try {
            return documentService.generateERezeptPdf(bundles);
        } catch (FOPException | IOException | TransformerException e) {
            e.printStackTrace();
            fail();
            return null;
        }
    }
}