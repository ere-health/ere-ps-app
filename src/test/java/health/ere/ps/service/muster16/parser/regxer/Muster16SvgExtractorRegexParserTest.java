package health.ere.ps.service.muster16.parser.regxer;

import health.ere.ps.service.extractor.SVGExtractor;
import health.ere.ps.service.extractor.TemplateProfile;
import health.ere.ps.service.muster16.parser.rgxer.Muster16SvgRegexParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class Muster16SvgExtractorRegexParserTest {

//    private static final Logger log = Logger.getLogger(Muster16SvgExtractorRegexParserTest.class.getName());


    @Test
    void testParseData_CGM_Z1() throws IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(TemplateProfile.CGM_Z1.configuration);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(getClass()
                .getResourceAsStream("/muster-16-print-samples/cgm-z1-manuel-blechschmidt.pdf")));

        Muster16SvgRegexParser parser = new Muster16SvgRegexParser(map);

        assertEquals("TK > Brandenburg 83", parser.parseInsuranceCompany());
        assertEquals("100696012", parser.parseInsuranceCompanyId());
        assertTrue(parser.parsePatientNamePrefix().isEmpty());
        assertEquals("Manuel", parser.parsePatientFirstName());
        assertEquals("Blechschmidt", parser.parsePatientLastName());
        assertEquals("Droysenstr.", parser.parsePatientStreetName());
        assertEquals("7", parser.parsePatientStreetNumber());
        assertEquals("10629", parser.parsePatientZipCode());
        assertEquals("Berlin", parser.parsePatientCity());
        assertEquals("1986-07-16", parser.parsePatientDateOfBirth());
        assertEquals("1000000", parser.parsePatientStatus());
        assertEquals("30001234", parser.parseClinicId());
        assertEquals("30001234", parser.parseDoctorId());
        assertEquals("2021-04-13", parser.parsePrescriptionDate());
        assertEquals("V062074590", parser.parsePatientInsuranceId());
        assertEquals("0301234567", parser.parsePractitionerPhoneNumber());
        assertEquals("12345", parser.parsePractitionerZipCode());
        assertEquals("Berlin", parser.parsePractitionerCity());
        assertEquals("In der tollen Str.", parser.parsePractitionerStreetName());
        assertEquals("115", parser.parsePractitionerStreetNumber());
    }

    @Test
    @Disabled("Github doesn't have access to the secret repo, run this test manually")
    void testParse_CGMTurboMed() throws IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(TemplateProfile.CGM_TURBO_MED.configuration);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(
                new FileInputStream("../secret-test-print-samples/CGM-Turbomed/test1_no_number_in_practitioner_name.pdf")));

        Muster16SvgRegexParser parser = new Muster16SvgRegexParser(map);

        assertEquals("Bahn - BKK", parser.parseInsuranceCompany());
        assertEquals("109938331", parser.parseInsuranceCompanyId());
        assertTrue(parser.parsePatientNamePrefix().isEmpty());
        assertEquals("Banholzer", parser.parsePatientFirstName());
        assertEquals("Dominik", parser.parsePatientLastName());
        assertEquals("Maria Trost", parser.parsePatientStreetName());
        assertEquals("21", parser.parsePatientStreetNumber());
        assertEquals("56070", parser.parsePatientZipCode());
        assertEquals("Koblenz", parser.parsePatientCity());
        assertEquals("1987-07-19", parser.parsePatientDateOfBirth());
        assertEquals("5000000", parser.parsePatientStatus());
        assertEquals("999123456", parser.parseClinicId());
        assertEquals("471100815", parser.parseDoctorId());
        assertEquals("2021-04-30", parser.parsePrescriptionDate());
        assertEquals("", parser.parsePatientInsuranceId());
        assertEquals("0261110110", parser.parsePractitionerPhoneNumber());
        assertEquals("56068", parser.parsePractitionerZipCode());
        assertEquals("Koblenz", parser.parsePractitionerCity());
        assertEquals("E-Reze pt", parser.parsePractitionerFirstName());
        assertEquals("Testarzt", parser.parsePractitionerLastName());
        assertEquals("Neustraße", parser.parsePractitionerStreetName());
        assertEquals("10", parser.parsePractitionerStreetNumber());
        assertEquals("Dr.", parser.parsePractitionerNamePrefix());
    }

    @Test
    @Disabled("Github doesn't have access to the secret repo, run this test manually")
    void testParse_Dens1() throws IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(TemplateProfile.DENS.configuration);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(
                new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept1.pdf")));

        Muster16SvgRegexParser parser = new Muster16SvgRegexParser(map);

        assertEquals("DENS GmbH", parser.parseInsuranceCompany());
        assertEquals("", parser.parseInsuranceCompanyId());
        List<String> expectedPrefix = List.of("Dr.");
        assertEquals(expectedPrefix, parser.parsePatientNamePrefix());
        assertEquals("Markus", parser.parsePatientFirstName());
        assertEquals("Heckner", parser.parsePatientLastName());
        assertEquals("Berliner Str.", parser.parsePatientStreetName());
        assertEquals("12", parser.parsePatientStreetNumber());
        assertEquals("14513", parser.parsePatientZipCode());
        assertEquals("Teltow", parser.parsePatientCity());
        assertEquals("1976-02-14", parser.parsePatientDateOfBirth());
        assertEquals("3000000", parser.parsePatientStatus());
        assertEquals("30000000", parser.parseClinicId());
        assertEquals("30000000", parser.parseDoctorId());
        assertEquals("2021-04-29", parser.parsePrescriptionDate());
        assertEquals("", parser.parsePatientInsuranceId());
        assertEquals("03328334540", parser.parsePractitionerPhoneNumber());
        assertEquals("03328334547", parser.parsePractitionerFaxNumber());
        assertEquals("14513", parser.parsePractitionerZipCode());
        assertEquals("Teltow", parser.parsePractitionerCity());
        assertEquals("DENS", parser.parsePractitionerFirstName());
        assertEquals("GmbH", parser.parsePractitionerLastName());
        assertEquals("Berliner Str.", parser.parsePractitionerStreetName());
        assertEquals("13", parser.parsePractitionerStreetNumber());
        assertEquals("", parser.parsePractitionerNamePrefix());
    }

    @Test
    @Disabled("Github doesn't have access to the secret repo, run this test manually")
    void testExtractDensErezept() throws IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(TemplateProfile.DENS.configuration);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(
                new FileInputStream("../secret-test-print-samples/DENS-GmbH/eRezept.pdf")));

        Muster16SvgRegexParser parser = new Muster16SvgRegexParser(map);

        assertEquals("DENS GmbH", parser.parseInsuranceCompany());
        assertEquals("", parser.parseInsuranceCompanyId());
        List<String> expectedPrefix = List.of("Dr.");
        assertEquals(expectedPrefix, parser.parsePatientNamePrefix());
        assertEquals("Markus", parser.parsePatientFirstName());
        assertEquals("Heckner", parser.parsePatientLastName());
        assertEquals("Testweg", parser.parsePatientStreetName());
        assertEquals("1", parser.parsePatientStreetNumber());
        assertEquals("13403", parser.parsePatientZipCode());
        assertEquals("Berlin", parser.parsePatientCity());
        assertEquals("1976-02-14", parser.parsePatientDateOfBirth());
        assertEquals("3000000", parser.parsePatientStatus());
        assertEquals("30000000", parser.parseClinicId());
        assertEquals("30000000", parser.parseDoctorId());
        assertEquals("2021-04-26", parser.parsePrescriptionDate());
        assertEquals("", parser.parsePatientInsuranceId());
        assertEquals("03328334540", parser.parsePractitionerPhoneNumber());
        assertEquals("03328334547", parser.parsePractitionerFaxNumber());
        assertEquals("14513", parser.parsePractitionerZipCode());
        assertEquals("Teltow", parser.parsePractitionerCity());
        assertEquals("DENS", parser.parsePractitionerFirstName());
        assertEquals("GmbH", parser.parsePractitionerLastName());
        assertEquals("Berliner Str.", parser.parsePractitionerStreetName());
        assertEquals("13", parser.parsePractitionerStreetNumber());
        assertEquals("", parser.parsePractitionerNamePrefix());
    }
}
