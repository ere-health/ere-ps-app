package health.ere.ps.service.fhir.bundle;

import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.extractor.SVGExtractor;
import health.ere.ps.service.extractor.SVGExtractorConfiguration;
import health.ere.ps.service.muster16.Muster16FormDataExtractorService;
import health.ere.ps.service.muster16.parser.Muster16SvgExtractorParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.pdfbox.pdmodel.PDDocument;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class ExtractionToBundleWorkflowTest {

    private static final Logger log = Logger.getLogger(ExtractionToBundleWorkflowTest.class.getName());


    @Test
    @Disabled("Github doesn't have access to the secret repo, run this test manually")
    public void extractionFromPdf_producesCorrectBundle() throws IOException, ParseException, XMLStreamException {
        // GIVEN
        PDDocument testDocument = PDDocument.load(
                new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept1.pdf"));
        SVGExtractor svgExtractor = new SVGExtractor(SVGExtractorConfiguration.DENS, false);

        // WHEN (simulates the extraction workflow from the start to finish without the events )
        Map<String, String> map = svgExtractor.extract(testDocument);

        Muster16SvgExtractorParser muster16Parser = new Muster16SvgExtractorParser(map);
        Muster16PrescriptionForm muster16PrescriptionForm = Muster16FormDataExtractorService.fillForm(muster16Parser);

        PrescriptionBundlesBuilder bundleBuilder = new PrescriptionBundlesBuilder(muster16PrescriptionForm);
        List<Bundle> bundles = bundleBuilder.createBundles();

        // THEN
        bundles.forEach(bundle -> {
            assertEquals("Berliner Str. 12", extractAddress(bundle));
            assertEquals("14513", extractPostCode(bundle));
            assertEquals("Teltow", extractCity(bundle));
            assertEquals("1976-02-14", extractBirthDate(bundle));
            assertEquals("Dr.", extractPatientPrefix(bundle));
            assertEquals("Markus", extractPatientFirstName(bundle));
            assertEquals("Heckner", extractPatientLastName(bundle));
            assertEquals("0", extractGebPfl(bundle));
        });
    }


    private String extractAddress(Bundle bundle) {
        return getEntry(bundle, "Patient").getResource().getChildByName("address").getValues().get(0)
                .getChildByName("line").getValues().get(0).primitiveValue();
    }

    private String extractPostCode(Bundle bundle) {
        return getEntry(bundle, "Patient").getResource().getChildByName("address").getValues().get(0)
                .getChildByName("postalCode").getValues().get(0).primitiveValue();
    }

    private String extractCity(Bundle bundle) {
        return getEntry(bundle, "Patient").getResource().getChildByName("address").getValues().get(0)
                .getChildByName("city").getValues().get(0).primitiveValue();
    }

    private String extractBirthDate(Bundle bundle) {
        return getEntry(bundle, "Patient").getResource().getChildByName("birthDate").getValues().get(0)
                .primitiveValue();
    }

    private String extractPatientPrefix(Bundle bundle) {
        return getEntry(bundle, "Patient").getResource().getChildByName("name").getValues().get(0)
                .getChildByName("prefix").getValues().get(0).primitiveValue();
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

    private Bundle.BundleEntryComponent getEntry(Bundle bundle, String name) {
        return bundle.getEntry().stream()
                .filter(entry -> entry.getResource().fhirType().equals(name))
                .collect(Collectors.toList()).get(0);
    }
}
