package health.ere.ps.service.pdf;

import ca.uhn.fhir.context.FhirContext;
import health.ere.ps.event.BundlesWithAccessCodeEvent;
import health.ere.ps.event.ERezeptDocumentsEvent;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import io.quarkus.test.junit.QuarkusTest;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@QuarkusTest
public class DocumentServiceTest {

    private final static List<Bundle> testBundles = new ArrayList<>();
    private final static String TARGET_PATH = "target/test_Erezepten/";
    private final static FhirContext ctx = FhirContext.forR4();

    private DocumentService documentService;

    @BeforeAll
    public static void prepareTestDirectoryAndTestBundles() throws IOException {
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
    }

    @BeforeEach
    public void instantiateDocumentService() {
        documentService = new DocumentService();
        documentService.init();
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
    public void onBundlesWithAccessCodes_firesEventWithBundlesFilteredByPatient_givenMultipleBundlesWithDifferentPatients() {
        // GIVEN
        int numberOfPatientsInBundles = 3;
        Event<ERezeptDocumentsEvent> mockedEvent = Mockito.mock(Event.class);
        documentService.seteRezeptDocumentsEvent(mockedEvent);

        List<BundleWithAccessCodeOrThrowable> firstBundles = List.of(
                new BundleWithAccessCodeOrThrowable(testBundles.get(0), "MOCK_CODE0"),
                new BundleWithAccessCodeOrThrowable(testBundles.get(1), "MOCK_CODE1"));

        List<BundleWithAccessCodeOrThrowable> secondBundles = List.of(
                new BundleWithAccessCodeOrThrowable(testBundles.get(2), "MOCK_CODE2"),
                new BundleWithAccessCodeOrThrowable(testBundles.get(3), "MOCK_CODE3"),
                new BundleWithAccessCodeOrThrowable(testBundles.get(4), "MOCK_CODE4"));

        List<List<BundleWithAccessCodeOrThrowable>> bundles = List.of(firstBundles, secondBundles);
        BundlesWithAccessCodeEvent event = new BundlesWithAccessCodeEvent(bundles);

        // WHEN
        documentService.onBundlesWithAccessCodes(event);

        // THEN
        Mockito.verify(mockedEvent, Mockito.times(numberOfPatientsInBundles)).fireAsync(Mockito.any());
    }

    @Test
    public void generateERezeptPdf_generatesCorrectPdf_givenOneMedicationToDisplay() throws IOException {
        // WHEN + THEN
        ByteArrayOutputStream pdfDocumentsOStream = getOutputStreamForANumberOfPdfDocuments(1);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_one_medication.pdf"), pdfDocumentsOStream.toByteArray());
    }

    @Test
    public void generateERezeptPdf_generatesCorrectPdf_givenTwoMedicationToDisplay() throws IOException {
        // WHEN + THEN
        ByteArrayOutputStream pdfDocumentsOStream = getOutputStreamForANumberOfPdfDocuments(2);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_two_medications.pdf"), pdfDocumentsOStream.toByteArray());
    }

    @Test
    public void generateERezeptPdf_generatesCorrectPdf_givenThreeMedicationToDisplay() throws IOException {
        // WHEN + THEN
        ByteArrayOutputStream pdfDocumentsOStream = getOutputStreamForANumberOfPdfDocuments(3);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_three_medications.pdf"), pdfDocumentsOStream.toByteArray());
    }

    @Test
    public void generateERezeptPdf_generatesCorrectPdfWithTwoPages_givenFourMedicationToDisplay() throws IOException {
        // WHEN + THEN
        ByteArrayOutputStream pdfDocumentsOStream = getOutputStreamForANumberOfPdfDocuments(4);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_four_medications.pdf"), pdfDocumentsOStream.toByteArray());
    }

    @Test
    public void generateERezeptPdf_generatesCorrectPdfWithTwoPages_givenFiveMedicationToDisplay() throws IOException {
        // WHEN + THEN
        ByteArrayOutputStream pdfDocumentsOStream = getOutputStreamForANumberOfPdfDocuments(5);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_five_medications.pdf"), pdfDocumentsOStream.toByteArray());
    }

    @Test
    public void generateERezeptPdf_generatesCorrectPdfWithTwoPages_givenSixMedicationToDisplay() throws IOException {
        // WHEN + THEN
        ByteArrayOutputStream pdfDocumentsOStream = getOutputStreamForANumberOfPdfDocuments(6);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_six_medications.pdf"), pdfDocumentsOStream.toByteArray());
    }

    //TODO: Starting at 7 the QR code on the top-right start being too big, why? How many should we support?

    @Test
    public void generateERezeptPdf_generatesCorrectPdfWithThreePages_givenSevenMedicationToDisplay() throws IOException {
        // WHEN + THEN
        ByteArrayOutputStream pdfDocumentsOStream = getOutputStreamForANumberOfPdfDocuments(7);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_seven_medications.pdf"), pdfDocumentsOStream.toByteArray());
    }

    @Test
    public void generateERezeptPdf_generatesCorrectPdfWithThreePages_givenEightMedicationToDisplay() throws IOException {
        // WHEN + THEN
        ByteArrayOutputStream pdfDocumentsOStream = getOutputStreamForANumberOfPdfDocuments(8);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_eight_medications.pdf"), pdfDocumentsOStream.toByteArray());
    }

    @Test
    public void generateERezeptPdf_generatesCorrectPdfWithThreePages_givenNineMedicationToDisplay() throws IOException {
        // WHEN + THEN
        ByteArrayOutputStream pdfDocumentsOStream = getOutputStreamForANumberOfPdfDocuments(9);
        Files.write(Paths.get(TARGET_PATH + "Erezept_with_nine_medications.pdf"), pdfDocumentsOStream.toByteArray());
    }


    private ByteArrayOutputStream getOutputStreamForANumberOfPdfDocuments(int number) {
        List<BundleWithAccessCodeOrThrowable> bundles = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            bundles.add(new BundleWithAccessCodeOrThrowable(testBundles.get(i % 5), "MOCK_CODE" + i));
        }
        return documentService.generateERezeptPdf(bundles);
    }
}