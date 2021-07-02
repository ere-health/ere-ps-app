package health.ere.ps.service.fhir.bundle;

import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.extractor.SVGExtractor;
import health.ere.ps.service.muster16.Muster16FormDataExtractorService;
import health.ere.ps.service.muster16.parser.rgxer.Muster16SvgRegexParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static health.ere.ps.service.extractor.TemplateProfile.CGM_TURBO_MED;
import static health.ere.ps.service.extractor.TemplateProfile.DENS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class ExtractionToBundleWorkflowTest {

    private static final Logger log = Logger.getLogger(ExtractionToBundleWorkflowTest.class.getName());

    @Test
    @Disabled("Github doesn't have access to the secret repo, run this test manually")
    public void extractionFromPdf_producesCorrectBundle_givenDensPdf() throws IOException, XMLStreamException {
        // GIVEN
        PDDocument testDocument = PDDocument.load(
                new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept1.pdf"));
        SVGExtractor svgExtractor = new SVGExtractor(DENS.configuration, false);

        // WHEN (simulates the extraction workflow from the extraction from the pdf to the creation of the bundle
        // before we send it to the frontend through websocket without the events being fired)
        Map<String, String> extractionResultsMap = svgExtractor.extract(testDocument);

        Muster16SvgRegexParser muster16Parser = new Muster16SvgRegexParser(extractionResultsMap);
        Muster16PrescriptionForm muster16PrescriptionForm = Muster16FormDataExtractorService.fillForm(muster16Parser);

//        logExtractionResultsAndMuster16Form(extractionResultsMap, muster16PrescriptionForm);

        PrescriptionBundlesBuilder bundleBuilder = new PrescriptionBundlesBuilder(muster16PrescriptionForm);
        List<Bundle> bundles = bundleBuilder.createBundles();

        // THEN
        bundles.forEach(bundle -> {
            assertEquals("Berliner Str. 12", extractPatientAddress(bundle));
            assertEquals("14513", extractPatientPostCode(bundle));
            assertEquals("Teltow", extractPatientCity(bundle));
            assertEquals("1976-02-14", extractBirthDate(bundle));
            assertEquals("Dr.", extractPatientPrefix(bundle));
            assertEquals("Markus", extractPatientFirstName(bundle));
            assertEquals("Heckner", extractPatientLastName(bundle));
            assertEquals("3", extractPatientStatus(bundle));
            assertEquals("0", extractGebPfl(bundle));
            assertEquals("DENS", extractPractitionerFirstName(bundle));
            assertEquals("GmbH", extractPractitionerLastName(bundle));
            assertEquals("Berliner Str. 13", extractPractitionerAddress(bundle));
            assertEquals("Teltow", extractPractitionerCity(bundle));
            assertEquals("14513", extractPractitionerPostCode(bundle));
            assertEquals("03328334540", extractPractitionerPhoneNumber(bundle));
            assertEquals("03328334547", extractPractitionerFaxNumber(bundle));
            assertEquals("2021-04-29T00:00:00+07:00", extractAuthoredOn(bundle));
        });

        assertEquals("Ibuprofen 600mg", extractMedicationName(bundles.get(0)));
        assertEquals("Omeprazol 40 mg", extractMedicationName(bundles.get(1)));
        assertEquals("Amoxicillin 1.000 mg", extractMedicationName(bundles.get(2)));
    }


    @Test
    @Disabled("Github doesn't have access to the secret repo, run this test manually")
    public void extractionFromPdf_producesCorrectBundle_givenCGMPdf() throws IOException, XMLStreamException {
        // GIVEN
        PDDocument testDocument = PDDocument.load(
                new FileInputStream("../secret-test-print-samples/CGM-Turbomed/test1_no_number_in_practitioner_name.pdf"));
        SVGExtractor svgExtractor = new SVGExtractor(CGM_TURBO_MED.configuration, false);

        // WHEN (simulates the extraction workflow from the extraction from the pdf to the creation of the bundle
        // before we send it to the frontend through websocket without the events being fired)
        Map<String, String> extractionResultsMap = svgExtractor.extract(testDocument);

        Muster16SvgRegexParser muster16Parser = new Muster16SvgRegexParser(extractionResultsMap);
        Muster16PrescriptionForm muster16PrescriptionForm = Muster16FormDataExtractorService.fillForm(muster16Parser);

        PrescriptionBundlesBuilder bundleBuilder = new PrescriptionBundlesBuilder(muster16PrescriptionForm);
        Bundle bundle = bundleBuilder.createBundles().get(0);

        // THEN
        assertEquals("Maria Trost 21", extractPatientAddress(bundle));
        assertEquals("56070", extractPatientPostCode(bundle));
        assertEquals("Koblenz", extractPatientCity(bundle));
        assertEquals("1987-07-19", extractBirthDate(bundle));
        assertEquals("", extractPatientPrefix(bundle));
        assertEquals("Banholzer", extractPatientFirstName(bundle));
        assertEquals("Dominik", extractPatientLastName(bundle));
        assertEquals("5", extractPatientStatus(bundle));
        assertEquals("0", extractGebPfl(bundle));
        assertEquals("E-Reze pt", extractPractitionerFirstName(bundle));
        assertEquals("Testarzt", extractPractitionerLastName(bundle));
        assertEquals("Dr.", extractPractitionerPrefix(bundle));
        assertEquals("Neustraße 10", extractPractitionerAddress(bundle));
        assertEquals("Koblenz", extractPractitionerCity(bundle));
        assertEquals("56068", extractPractitionerPostCode(bundle));
        assertEquals("0261110110", extractPractitionerPhoneNumber(bundle));
        assertEquals("Novalgin AMP N1 5X2 ml", extractMedicationName(bundle));
        assertEquals("2021-04-30T00:00:00+07:00", extractAuthoredOn(bundle));
    }

    private String extractMedicationName(Bundle bundle) {
        return getEntry(bundle, "Medication").getResource().getChildByName("code").getValues().get(0)
                .getChildByName("text").getValues().get(0).primitiveValue();
    }

    private String extractPractitionerPrefix(Bundle bundle) {
        List<Base> prefix = getEntry(bundle, "Practitioner").getResource().getChildByName("name").getValues().get(0)
                .getChildByName("prefix").getValues();
        return (prefix.isEmpty()) ? "" : prefix.get(0).primitiveValue();
    }

    private String extractPractitionerFirstName(Bundle bundle) {
        return getEntry(bundle, "Practitioner").getResource().getChildByName("name").getValues().get(0)
                .getChildByName("given").getValues().get(0).primitiveValue();
    }

    private String extractPractitionerLastName(Bundle bundle) {
        return getEntry(bundle, "Practitioner").getResource().getChildByName("name").getValues().get(0)
                .getChildByName("family").getValues().get(0).primitiveValue();
    }

    private String extractPractitionerAddress(Bundle bundle) {
        return getEntry(bundle, "Practitioner").getResource().getChildByName("address").getValues().get(0)
                .getChildByName("line").getValues().get(0).primitiveValue();
    }

    private String extractPractitionerPostCode(Bundle bundle) {
        return getEntry(bundle, "Practitioner").getResource().getChildByName("address").getValues().get(0)
                .getChildByName("postalCode").getValues().get(0).primitiveValue();
    }

    private String extractPractitionerCity(Bundle bundle) {
        return getEntry(bundle, "Practitioner").getResource().getChildByName("address").getValues().get(0)
                .getChildByName("city").getValues().get(0).primitiveValue();
    }

    private String extractPractitionerPhoneNumber(Bundle bundle) {
        return getEntry(bundle, "Practitioner").getResource().getChildByName("telecom").getValues().get(0)
                .getChildByName("value").getValues().get(0).primitiveValue();
    }

    private String extractPractitionerFaxNumber(Bundle bundle) {
        return getEntry(bundle, "Practitioner").getResource().getChildByName("telecom").getValues().get(1)
                .getChildByName("value").getValues().get(0).primitiveValue();
    }

    private String extractPatientStatus(Bundle bundle) {
        return getEntry(bundle, "Coverage").getResource().getChildByName("extension").getValues().get(3)
                .getChildByName("value[x]").getValues().get(0).getChildByName("code").getValues().get(0)
                .primitiveValue();
    }

    private String extractPatientAddress(Bundle bundle) {
        return getEntry(bundle, "Patient").getResource().getChildByName("address").getValues().get(0)
                .getChildByName("line").getValues().get(0).primitiveValue();
    }

    private String extractPatientPostCode(Bundle bundle) {
        return getEntry(bundle, "Patient").getResource().getChildByName("address").getValues().get(0)
                .getChildByName("postalCode").getValues().get(0).primitiveValue();
    }

    private String extractPatientCity(Bundle bundle) {
        return getEntry(bundle, "Patient").getResource().getChildByName("address").getValues().get(0)
                .getChildByName("city").getValues().get(0).primitiveValue();
    }

    private String extractBirthDate(Bundle bundle) {
        return getEntry(bundle, "Patient").getResource().getChildByName("birthDate").getValues().get(0)
                .primitiveValue();
    }

    private String extractPatientPrefix(Bundle bundle) {
        List<Base> prefix = getEntry(bundle, "Patient").getResource().getChildByName("name").getValues().get(0)
                .getChildByName("prefix").getValues();
        return (prefix.isEmpty()) ? "" : prefix.get(0).primitiveValue();
    }

    private String extractPatientFirstName(Bundle bundle) {
        return getEntry(bundle, "Patient").getResource().getChildByName("name").getValues().get(0)
                .getChildByName("given").getValues().get(0).primitiveValue();
    }

    private String extractPatientLastName(Bundle bundle) {
        return getEntry(bundle, "Patient").getResource().getChildByName("name").getValues().get(0)
                .getChildByName("family").getValues().get(0).primitiveValue();
    }

    private String extractGebPfl(Bundle bundle) {
        return getEntry(bundle, "MedicationRequest").getResource().getChildByName("extension").getValues().get(0)
                .getChildByName("value[x]").getValues().get(0).getChildByName("code").getValues().get(0)
                .primitiveValue();
    }

    private String extractAuthoredOn(Bundle bundle) {
        return getEntry(bundle, "MedicationRequest").getResource().getChildByName("authoredOn")
                .getValues().get(0).primitiveValue();
    }

    private Bundle.BundleEntryComponent getEntry(Bundle bundle, String name) {
        return bundle.getEntry().stream()
                .filter(entry -> entry.getResource().fhirType().equals(name))
                .collect(Collectors.toList()).get(0);
    }

    private void logExtractionResultsAndMuster16Form(Map<String, String> extractionResultsMap,
                                                     Muster16PrescriptionForm muster16PrescriptionForm) {
        extractionResultsMap.entrySet().forEach(entry -> {
            log.info("key:" + entry.getKey() + ", value:" + entry.getValue().trim());
        });

        log.info("Form:" + muster16PrescriptionForm);
    }
}
