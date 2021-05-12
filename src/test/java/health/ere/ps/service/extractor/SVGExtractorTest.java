package health.ere.ps.service.extractor;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class SVGExtractorTest {

    @Inject
    SVGExtractor svgExtractor;

    @Inject
    Logger logger;

    @Disabled
    @Test
    void testExtract() throws URISyntaxException {

        svgExtractor.init(getClass().getResource("/svg-extract-templates/Muster-16-Template.svg").toURI(), true);
        Map<String, String> map = svgExtractor.extract(getClass().getResourceAsStream("/muster-16-print-samples/cgm-z1-manuel-blechschmidt.pdf"));

        // System.out.println(map.entrySet().stream().map((e) -> "
        // assertEquals(\""+e.getValue().replaceAll("\n", "\\\\n")+"\",
        // map.get(\""+e.getKey()+"\"));").collect(Collectors.joining("\n")));

        String lineSep = System.getProperty("line.separator");

        assertEquals(lineSep, map.get("factor1"));
        assertEquals("Amoxicillin 1000mg N2" + lineSep + "3x täglich alle 8 Std" + lineSep
                + "-  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -" + lineSep, map.get("medication"));
        assertEquals("Blechschmidt" + lineSep + "Manuel               " + lineSep + "Droysenstr. 7" + lineSep + "D 10629 Berlin   "
                + lineSep, map.get("nameAndAddress"));
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
