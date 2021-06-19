package health.ere.ps.service.muster16.parser.regxer;

import health.ere.ps.service.extractor.SVGExtractor;
import health.ere.ps.service.extractor.SVGExtractorConfiguration;
import health.ere.ps.service.muster16.parser.rgxer.Muster16SvgRegexParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class Muster16SvgExtractorRegexParserTest {

    @Test
    void testParseData_CGM_Z1() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(SVGExtractorConfiguration.CGM_Z1, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(getClass().getResourceAsStream("/muster-16-print-samples/cgm-z1-manuel-blechschmidt.pdf")));

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
        assertEquals("30001234", parser.parseClinicId());
        assertEquals("30001234", parser.parseDoctorId());
        assertEquals("2021-04-13", parser.parsePrescriptionDate());
        assertEquals("V062074590", parser.parsePatientInsuranceId());
    }

    @Test
    @Disabled
    void testParse_CGMTurboMed() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(SVGExtractorConfiguration.CGM_TURBO_MED, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/CGM-Turbomed/test1.pdf")));

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
        assertEquals("999123456", parser.parseClinicId());
        assertEquals("471100815", parser.parseDoctorId());
        assertEquals("2021-04-30", parser.parsePrescriptionDate());
        assertEquals("", parser.parsePatientInsuranceId());
    }

    @Test
    @Disabled
    void testParse_Dens1() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(SVGExtractorConfiguration.DENS, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept1.pdf")));

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
        assertEquals("30000000", parser.parseClinicId());
        assertEquals("30000000", parser.parseDoctorId());
        assertEquals("2021-04-29", parser.parsePrescriptionDate());
        assertEquals("", parser.parsePatientInsuranceId());
    }

    @Test
    @Disabled
    void testExtractDensErezept() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(SVGExtractorConfiguration.DENS, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/DENS-GmbH/eRezept.pdf")));

        Muster16SvgRegexParser parser = new Muster16SvgRegexParser(map);

        assertEquals("DENS GmbH", parser.parseInsuranceCompany());
        assertEquals("", parser.parseInsuranceCompanyId());
        List<String> expectedPrefix = List.of("Dr.");
        assertEquals(expectedPrefix, parser.parsePatientNamePrefix());        assertEquals("Markus", parser.parsePatientFirstName());
        assertEquals("Heckner", parser.parsePatientLastName());
        assertEquals("Testweg", parser.parsePatientStreetName());
        assertEquals("1", parser.parsePatientStreetNumber());
        assertEquals("13403", parser.parsePatientZipCode());
        assertEquals("Berlin", parser.parsePatientCity());
        assertEquals("1976-02-14", parser.parsePatientDateOfBirth());
        assertEquals("30000000", parser.parseClinicId());
        assertEquals("30000000", parser.parseDoctorId());
        assertEquals("2021-04-26", parser.parsePrescriptionDate());
        assertEquals("", parser.parsePatientInsuranceId());
    }
}
