package health.ere.ps.service.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ca.uhn.fhir.context.FhirContext;
import health.ere.ps.event.BundlesWithAccessCodeEvent;
import health.ere.ps.event.ERezeptDocumentsEvent;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.profile.DevelopmentTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevelopmentTestProfile.class)
public class DocumentServiceTest {

    private final static List<Bundle> testBundles = new ArrayList<>();
    private final static String TARGET_PATH = "target/test_Erezepten/";
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
        Event<ERezeptDocumentsEvent> mockedEvent = Mockito.mock(Event.class);
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


    private ByteArrayOutputStream createStreamForANumberOfPdfs(int number) {
        List<BundleWithAccessCodeOrThrowable> bundles = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            bundles.add(new BundleWithAccessCodeOrThrowable(testBundles.get(i % 5), "MOCK_CODE" + i));
        }
        return documentService.generateERezeptPdf(bundles);
    }
}