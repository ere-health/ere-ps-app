package health.ere.ps.service.extractor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SVGExtractorTest {


    private static Logger log = Logger.getLogger(SVGExtractorTest.class.getName());

    @Test
    void testExtract() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(SVGExtractorConfiguration.CGM_Z1, true);
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

    @Test @Disabled
    void testExtract2() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(SVGExtractorConfiguration.CGM_TURBO_MED, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/CGM-Turbomed/test1.pdf")));

        // map.entrySet().stream().forEach(entry -> log.info(entry.getKey() +" = " + entry.getValue()));
        // System.out.println(map.entrySet().stream().map((e) -> "        assertEquals(\""+e.getValue().replaceAll("\n", "\\\\n")+"\", map.get(\""+e.getKey()+"\"));").collect(Collectors.joining("\n")));
       
        assertEquals("\n", map.get("insuranceNumber"));
        assertEquals("\n", map.get("tax2"));
        assertEquals("\n", map.get("bvg"));
        assertEquals("109938331\n", map.get("payor"));
        assertEquals("\n", map.get("prescription1"));
        assertEquals("\n", map.get("pharmayNumber"));
        assertEquals("\n", map.get("prescription2"));
        assertEquals("\n", map.get("prescription3"));
        assertEquals("\n", map.get("autIdem1"));
        assertEquals("\n", map.get("tax3"));
        assertEquals("\n", map.get("withPayment"));
        assertEquals("\n", map.get("autIdem2"));
        assertEquals("\n", map.get("accidentDate"));
        assertEquals("\n", map.get("additionalPayment"));
        assertEquals("\n", map.get("withoutPayment"));
        assertEquals("\n", map.get("workAccident"));
        assertEquals("\n", map.get("factor2"));
        assertEquals("\n", map.get("factor1"));
        assertEquals("Novalgin AMP N1 5X2 ml\n-  -  -  -\nPZN04527098\n", map.get("medication"));
        assertEquals("D 56070 Koblenz\nMaria Trost 21\nDominik\nBanholzer\n", map.get("nameAndAddress"));
        assertEquals("\n", map.get("sprBedarf"));
        assertEquals("\n", map.get("factor3"));
        assertEquals("\n", map.get("accidentOrganization"));
        assertEquals("\n", map.get("begrPflicht"));
        assertEquals("471100815\n", map.get("practitionerNumber"));
        assertEquals("999123456\n", map.get("locationNumber"));
        assertEquals("\n", map.get("aid"));
        assertEquals("5000000\n", map.get("status"));
        assertEquals("\n", map.get("vaccination"));


    }


    @Test
    void testExtractApraxos() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(SVGExtractorConfiguration.APRAXOS, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(getClass().getResourceAsStream("/muster-16-print-samples/apraxos_DIN_A4_Output_F-job_222.pdf")));

        // System.out.println(map.entrySet().stream().map((e) -> "        assertEquals(\""+e.getValue().replaceAll("\n", "\\\\n")+"\", map.get(\""+e.getKey()+"\"));").collect(Collectors.joining("\n")));
        // map.entrySet().stream().forEach(entry -> log.info(entry.getKey() +" = " + entry.getValue()));
        // System.out.println(map.entrySet().stream().map((e) -> "        assertEquals(\""+e.getValue().replaceAll("\n", "\\\\n")+"\", map.get(\""+e.getKey()+"\"));").collect(Collectors.joining("\n")));
        assertEquals("AOK Bayern Die Gesundh.       \n", map.get("insurance"));
        assertEquals("\n", map.get("autIdem3"));
        assertEquals("          \n 25.04.21 \n", map.get("date"));
        assertEquals("\n", map.get("noctu"));
        assertEquals("\n", map.get("other"));
        assertEquals("                \n                \n        16.07.86\n                \n                \n", map.get("birthdate"));
        assertEquals("\n", map.get("pharmacyDate"));
        assertEquals("\n", map.get("grossTotal"));
        assertEquals("\n", map.get("accident"));
        assertEquals("\n", map.get("tax1"));
        assertEquals("Dr. Hans Topp-Glücklich  \nMusterstr. 1             \n18107 Rostock            \nTel 06151 1111111        \nFax 06151 2222222        \nBSNR 781234567           \nLANR 123456767           \nTopp-Gluecklich@praxis.de\n", map.get("practitionerText"));
        assertEquals("            \n            \n", map.get("insuranceNumber"));
        assertEquals("\n", map.get("tax2"));
        assertEquals("\n", map.get("bvg"));
        assertEquals("          \n108916641 \n", map.get("payor"));

    }

    @Test @Disabled
    void testExtractDens() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(SVGExtractorConfiguration.DENS, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept1.pdf")));

        // map.entrySet().stream().forEach(entry -> log.info(entry.getKey() +" = " + entry.getValue()));
        System.out.println(map.entrySet().stream().map((e) -> "        assertEquals(\""+e.getValue().replaceAll("\n", "\\\\n")+"\", map.get(\""+e.getKey()+"\"));").collect(Collectors.joining("\n")));
       
    }

}
