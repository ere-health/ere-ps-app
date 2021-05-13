package health.ere.ps.service.extractor;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import health.ere.ps.service.muster16.Muster16FormDataExtractorService;
import health.ere.ps.service.muster16.parser.Muster16SvgExtractorParser;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Map;

import io.quarkus.test.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SVGExtractorTest {

    private static final String MUSTER_16_TEMPLATE_SVG = "/svg-extract-templates/Muster-16-Template.svg";
    private static final String TESTRECIPE_PDF = "/muster-16-print-samples/cgm-z1-manuel-blechschmidt.pdf";

    private SVGExtractor svgExtractor;

    private Muster16FormDataExtractorService muster16FormDataExtractorService;

    @Mock
    Muster16SvgExtractorParser parser;

    private Logger logger;

    @BeforeEach
    public void before() throws Exception {
        muster16FormDataExtractorService = new Muster16FormDataExtractorService(parser);
        this.svgExtractor = new SVGExtractor(muster16FormDataExtractorService);
        this.svgExtractor.init(getClass().getResource(MUSTER_16_TEMPLATE_SVG).toURI(), true);
        logger = Logger.getLogger(this.getClass());
    }

    @Disabled
    @Test
    void testExtract() throws URISyntaxException {
        
        Map<String, String> map = svgExtractor.extract(getClass().getResourceAsStream(TESTRECIPE_PDF));
        String lineSep = System.getProperty("line.separator");

        assertEquals(lineSep, map.get("factor1"));
        assertEquals(
                "Amoxicillin 1000mg N2" + lineSep + "3x täglich alle 8 Std" + lineSep + "-  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -" + lineSep,
                map.get("medication"));
        assertEquals("Blechschmidt" + lineSep + "Manuel               " + lineSep + "Droysenstr. 7" + lineSep + "D 10629 Berlin   " + lineSep,
                map.get("nameAndAddress"));
        assertEquals(lineSep, map.get("sprBedarf"));
        assertEquals(lineSep, map.get("factor3"));
        assertEquals(lineSep, map.get("accidentOrganization"));
        assertEquals(lineSep, map.get("begrPflicht"));
        assertEquals("30001234  " + lineSep, map.get("practitionerNumber"));
        assertEquals(" 30001234  " + lineSep, map.get("locationNumber"));
        assertEquals(lineSep, map.get("aid"));
        assertEquals("1000000" + lineSep, map.get("status"));
        assertEquals(lineSep, map.get("vaccination"));

    }

  
    @Test
    void testExtract2() throws URISyntaxException, FileNotFoundException {
        svgExtractor.init(getClass().getResource("/svg-extract-templates/Muster-16-Template.svg").toURI(), true);
        Map<String, String> map = svgExtractor.extract(getClass().getResourceAsStream("/muster-16-print-samples/test1.pdf"));

        map.entrySet().stream().forEach(entry -> logger.info(entry.getKey() + " = " + entry.getValue()));
    }

}
