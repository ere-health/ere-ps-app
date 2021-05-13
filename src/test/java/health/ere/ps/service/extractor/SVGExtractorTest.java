package health.ere.ps.service.extractor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.xml.stream.XMLStreamException;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SVGExtractorTest {


    private static Logger log = Logger.getLogger(SVGExtractorTest.class.getName());

    @Disabled
    @Test
    void testExtract() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(getClass().getResource("/svg-extract-templates/Muster-16-Template.svg").toURI(), true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(getClass().getResourceAsStream("/muster-16-print-samples/cgm-z1-manuel-blechschmidt.pdf")));

        // System.out.println(map.entrySet().stream().map((e) -> "        assertEquals(\""+e.getValue().replaceAll("\n", "\\\\n")+"\", map.get(\""+e.getKey()+"\"));").collect(Collectors.joining("\n")));
        
        assertEquals("\n", map.get("factor1"));
        assertEquals("Amoxicillin 1000mg N2\n3x täglich alle 8 Std\n-  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -\n", map.get("medication"));
        assertEquals("Blechschmidt\nManuel               \nDroysenstr. 7\nD 10629 Berlin   \n", map.get("nameAndAddress"));
        assertEquals("\n", map.get("sprBedarf"));
        assertEquals("\n", map.get("factor3"));
        assertEquals("\n", map.get("accidentOrganization"));
        assertEquals("\n", map.get("begrPflicht"));
        assertEquals("30001234  \n", map.get("practitionerNumber"));
        assertEquals(" 30001234  \n", map.get("locationNumber"));
        assertEquals("\n", map.get("aid"));
        assertEquals("1000000\n", map.get("status"));
        assertEquals("\n", map.get("vaccination"));
 
    }

    @Test
    void testExtract2() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(getClass().getResource("/svg-extract-templates/Muster-16-Template.svg").toURI(), true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/CGM-Turbomed/test1.pdf")));

        map.entrySet().stream().forEach(entry -> log.info(entry.getKey() +" = " + entry.getValue()));
    }

}
