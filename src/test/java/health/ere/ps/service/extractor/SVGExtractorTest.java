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
    
    private String lineSep = System.lineSeparator();

    @Test
    void testExtractData_CGM_Z1() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(SVGExtractorConfiguration.CGM_Z1, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(getClass().getResourceAsStream("/muster-16-print-samples/cgm-z1-manuel-blechschmidt.pdf")));

        logExtraction(map);
        
        assertEquals("TK > Brandenburg            83" + lineSep, map.get("insurance"));
        assertEquals("X" + lineSep, map.get("withPayment"));
        assertEquals("" + lineSep, map.get("withoutPayment"));
        assertEquals("Blechschmidt" + lineSep + "Manuel               " + lineSep + "Droysenstr. 7" + lineSep + "D 10629 Berlin   " + lineSep, map.get("nameAndAddress"));
        assertEquals("16.07.86" + lineSep + "       " + lineSep, map.get("birthdate"));

        assertEquals("100696012 " + lineSep, map.get("payor"));
        assertEquals("V062074590   " + lineSep, map.get("insuranceNumber"));
        assertEquals("1000000" + lineSep, map.get("status"));

        assertEquals(" 30001234  " + lineSep, map.get("locationNumber"));
        assertEquals("30001234  " + lineSep, map.get("practitionerNumber"));
        assertEquals("13.04.21" + lineSep, map.get("date"));

        assertEquals("Amoxicillin 1000mg N2" + lineSep + "3x täglich alle 8 Std" + lineSep + "-  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -" + lineSep, map.get("medication"));
        assertEquals("1234" + lineSep + "Zahnärzte" + lineSep + "Dr. Zahnarzt Ein & Dr. Zahnarzt Zwei" + lineSep + "In der tollen Str.115" + lineSep + "12345 Berlin" + lineSep + "Tel. 030/123 4567" + lineSep, map.get("practitionerText"));
    }


    @Test
    void testExtractData_CGMTurboMed() throws URISyntaxException, IOException, XMLStreamException {

        SVGExtractor svgExtractor = new SVGExtractor(SVGExtractorConfiguration.CGM_TURBO_MED, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/CGM-Turbomed/test1.pdf")));
        logExtraction(map);
        
        assertEquals("Bahn - BKK                  " + lineSep, map.get("insurance"));
        assertEquals("X" + lineSep, map.get("withPayment"));
        assertEquals(lineSep, map.get("withoutPayment"));


       // assertEquals("19.07.87" + lineSep + "         " + lineSep , map.get("birthdate"));
      
       
        assertEquals("109938331" + lineSep, map.get("payor"));
        //no insurance number available
        assertEquals("5000000" + lineSep, map.get("status"));
       
        
        assertEquals("999123456" + lineSep, map.get("locationNumber"));
        assertEquals("471100815" + lineSep, map.get("practitionerNumber"));
        assertEquals("30.04.21" + lineSep, map.get("date"));
      
       
        assertEquals("Novalgin AMP N1 5X2 ml" + lineSep + "-  -  -  -" + lineSep + "PZN04527098" + lineSep, map.get("medication"));
        
        
        // assertEquals("D 56070 Koblenz   " + lineSep + "Maria Trost 21" + lineSep + "Dominik" + lineSep + "Banholzer" + lineSep, map.get("nameAndAddress"));
         // assertEquals("0261-110110\n56068 Koblenz\nNeustraße 10\nArzt--Hausarzt\nDr. E-Reze pt Testarzt 2\n", map.get("practitionerText"));


    }


    @Test @Disabled
    void testExtractApraxos() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(SVGExtractorConfiguration.APRAXOS, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(getClass().getResourceAsStream("/muster-16-print-samples/apraxos_DIN_A4_Output_F-job_222.pdf")));

        // map.entrySet().stream().forEach(entry -> log.info(entry.getKey() +" = " + entry.getValue()));
        // System.out.println(map.entrySet().stream().map((e) -> "        assertEquals(\""+e.getValue().replaceAll(lineSep, "\\\\n")+"\", map.get(\""+e.getKey()+"\"));").collect(Collectors.joining(lineSep)));
        assertEquals("AOK Bayern Die Gesundh.       " + lineSep, map.get("insurance"));
        assertEquals(lineSep, map.get("autIdem3"));
        assertEquals("          " + lineSep + " 25.04.21 " + lineSep, map.get("date"));
        assertEquals(lineSep, map.get("noctu"));
        assertEquals(lineSep, map.get("other"));
        assertEquals("                " + lineSep + "                " + lineSep + "        16.07.86" + lineSep + "                " + lineSep + "                " + lineSep, map.get("birthdate"));
        assertEquals(lineSep, map.get("pharmacyDate"));
        assertEquals(lineSep, map.get("grossTotal"));
        assertEquals(lineSep, map.get("accident"));
        assertEquals(lineSep, map.get("tax1"));
        assertEquals("Dr. Hans Topp-Glücklich  " + lineSep + "Musterstr. 1             " + lineSep + "18107 Rostock            " + lineSep + "Tel 06151 1111111        " + lineSep + "Fax 06151 2222222        " + lineSep + "BSNR 781234567           " + lineSep +  "LANR 123456767           " + lineSep +  "Topp-Gluecklich@praxis.de" + lineSep, map.get("practitionerText"));
        assertEquals("            " + lineSep + "            " + lineSep, map.get("insuranceNumber"));
        assertEquals(lineSep, map.get("tax2"));
        assertEquals(lineSep, map.get("bvg"));
        assertEquals("          " + lineSep + "108916641 " + lineSep, map.get("payor"));

    }

    @Test @Disabled
    void testExtractDens1() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(SVGExtractorConfiguration.DENS, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept1.pdf")));

        // map.entrySet().stream().forEach(entry -> log.info(entry.getKey() +" = " + entry.getValue()));
        // System.out.println(map.entrySet().stream().map((e) -> "        assertEquals(\""+e.getValue().replaceAll(lineSep, "\\\\n")+"\", map.get(\""+e.getKey()+"\"));").collect(Collectors.joining(lineSep)));
        
        assertEquals("DENS GmbH                     " + lineSep, map.get("insurance"));
        assertEquals(lineSep, map.get("autIdem3"));
        assertEquals("  29.04.21" + lineSep, map.get("date"));
        assertEquals(lineSep, map.get("noctu"));
        assertEquals(lineSep, map.get("other"));
        assertEquals("        14.02.76" + lineSep + "12" + lineSep + "           " + lineSep, map.get("birthdate"));
        assertEquals(lineSep, map.get("pharmacyDate"));
        assertEquals(lineSep, map.get("grossTotal"));
        assertEquals(lineSep, map.get("accident"));
        assertEquals(lineSep, map.get("tax1"));
        assertEquals("DENS GmbH" + lineSep + "Berliner Str. 13" + lineSep + "14513 Teltow" + lineSep + "03328-334540" + lineSep + "Fax: 03328-334547" + lineSep, map.get("practitionerText"));
        assertEquals("           " + lineSep, map.get("insuranceNumber"));
        assertEquals(lineSep, map.get("tax2"));
        assertEquals(lineSep, map.get("bvg"));
        assertEquals("          " + lineSep, map.get("payor"));
        assertEquals(lineSep, map.get("prescription1"));
        assertEquals(lineSep, map.get("pharmayNumber"));
        assertEquals(lineSep, map.get("prescription2"));
        assertEquals(lineSep, map.get("prescription3"));
        assertEquals(lineSep, map.get("autIdem1"));
        assertEquals(lineSep, map.get("tax3"));
        assertEquals("X" + lineSep, map.get("withPayment"));
        assertEquals(lineSep, map.get("autIdem2"));
        assertEquals(lineSep, map.get("accidentDate"));
        assertEquals(lineSep, map.get("additionalPayment"));
        assertEquals(lineSep, map.get("withoutPayment"));
        assertEquals(lineSep, map.get("workAccident"));
        assertEquals(lineSep, map.get("factor2"));
        assertEquals(lineSep, map.get("factor1"));
        assertEquals("Ibuprofen 600mg 1-1-1" + lineSep + "Omeprazol  40 mg  0-0-1" + lineSep + "Amoxicillin 1.000 mg 1-0-1" + lineSep, map.get("medication"));
        assertEquals("Heckner" + lineSep + "Dr. Markus    " + lineSep + "Berliner Str. " + lineSep + "D 14513 Teltow" + lineSep, map.get("nameAndAddress"));
        assertEquals(lineSep, map.get("sprBedarf"));
        assertEquals(lineSep, map.get("factor3"));
        assertEquals(lineSep, map.get("accidentOrganization"));
        assertEquals(lineSep, map.get("begrPflicht"));
        assertEquals("  30000000" + lineSep, map.get("practitionerNumber"));
        assertEquals(" 30000000" + lineSep, map.get("locationNumber"));
        assertEquals(lineSep, map.get("aid"));
        assertEquals("  3000000" + lineSep, map.get("status"));
        assertEquals(lineSep, map.get("vaccination"));

    }
    
    @Test
    void testExtractDens2() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(SVGExtractorConfiguration.DENS, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept2.pdf")));

        // map.entrySet().stream().forEach(entry -> log.info(entry.getKey() +" = " + entry.getValue()));
        //logExtraction(map);
        
        assertEquals("DENS GmbH                     " + lineSep, map.get("insurance"));
        assertEquals(lineSep, map.get("autIdem3"));
        assertEquals("  29.04.21" + lineSep, map.get("date"));
        assertEquals(lineSep, map.get("noctu"));
        assertEquals(lineSep, map.get("other"));
        assertEquals("        14.02.76" + lineSep + "12" + lineSep + "           " + lineSep, map.get("birthdate"));
        assertEquals("age" + lineSep, map.get("pharmacyDate"));
        assertEquals(lineSep, map.get("grossTotal"));
        assertEquals(lineSep, map.get("accident"));
        assertEquals(lineSep, map.get("tax1"));
        assertEquals("DENS GmbH" + lineSep + "Berliner Str. 13" + lineSep + "14513 Teltow" + lineSep + "03328-334540" + lineSep + "Fax: 03328-334547" + lineSep, map.get("practitionerText"));
        assertEquals("           " + lineSep, map.get("insuranceNumber"));
        assertEquals(lineSep, map.get("tax2"));
        assertEquals(lineSep, map.get("bvg"));
        assertEquals("          " + lineSep, map.get("payor"));
        assertEquals(lineSep, map.get("prescription1"));
        assertEquals(lineSep, map.get("pharmayNumber"));
        assertEquals(lineSep, map.get("prescription2"));
        assertEquals(lineSep, map.get("prescription3"));
        assertEquals(lineSep, map.get("autIdem1"));
        assertEquals(lineSep, map.get("tax3"));
        assertEquals("X" + lineSep, map.get("withPayment"));
        assertEquals(lineSep, map.get("autIdem2"));
        assertEquals(lineSep, map.get("accidentDate"));
        assertEquals(lineSep, map.get("additionalPayment"));
        assertEquals(lineSep, map.get("withoutPayment"));
        assertEquals(lineSep, map.get("workAccident"));
        assertEquals(lineSep, map.get("factor2"));
        assertEquals(lineSep, map.get("factor1"));
        assertEquals("Metamizol 20 Topfen/500mg bei" + lineSep + "Bedarf, Tageshöchstdosis: 1.5Pantoprazol 40mg 1-0-0" + lineSep + "Clindamycin 600mg 1-0-1 für 5" + lineSep, map.get("medication"));
        assertEquals("Heckner" + lineSep + "Dr. Markus    " + lineSep + "Berliner Str. " + lineSep + "D 14513 Teltow" + lineSep, map.get("nameAndAddress"));
        assertEquals(lineSep, map.get("sprBedarf"));
        assertEquals(lineSep, map.get("factor3"));
        assertEquals(lineSep, map.get("accidentOrganization"));
        assertEquals(lineSep, map.get("begrPflicht"));
        assertEquals("  30000000" + lineSep, map.get("practitionerNumber"));
        assertEquals(" 30000000" + lineSep, map.get("locationNumber"));
        assertEquals(lineSep, map.get("aid"));
        assertEquals("  3000000" + lineSep, map.get("status"));
        assertEquals(lineSep, map.get("vaccination"));
    }

   
    @Test @Disabled
    void testExtractDens3() throws URISyntaxException, IOException, XMLStreamException {

        SVGExtractor svgExtractor = new SVGExtractor(SVGExtractorConfiguration.DENS, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept3.pdf")));

        // map.entrySet().stream().forEach(entry -> log.info(entry.getKey() +" = " + entry.getValue()));
        //logExtraction(map);
        
        assertEquals("DENS GmbH                     " + lineSep, map.get("insurance"));
        assertEquals(lineSep, map.get("autIdem3"));
        assertEquals("  29.04.21" + lineSep, map.get("date"));
        assertEquals(lineSep, map.get("noctu"));
        assertEquals(lineSep, map.get("other"));
        assertEquals("        14.02.76" + lineSep + "12" + lineSep + "           " + lineSep, map.get("birthdate"));
        assertEquals(lineSep, map.get("pharmacyDate"));
        assertEquals(lineSep, map.get("grossTotal"));
        assertEquals(lineSep, map.get("accident"));
        assertEquals(lineSep, map.get("tax1"));
        assertEquals("DENS GmbH" + lineSep + "Berliner Str. 13" + lineSep + "14513 Teltow" + lineSep + "03328-334540" + lineSep + "Fax: 03328-334547" + lineSep, map.get("practitionerText"));
        assertEquals("           " + lineSep, map.get("insuranceNumber"));
        assertEquals(lineSep, map.get("tax2"));
        assertEquals(lineSep, map.get("bvg"));
        assertEquals("          " + lineSep, map.get("payor"));
        assertEquals(lineSep, map.get("prescription1"));
        assertEquals(lineSep, map.get("pharmayNumber"));
        assertEquals(lineSep, map.get("prescription2"));
        assertEquals(lineSep, map.get("prescription3"));
        assertEquals(lineSep, map.get("autIdem1"));
        assertEquals(lineSep, map.get("tax3"));
        assertEquals("X" + lineSep, map.get("withPayment"));
        assertEquals(lineSep, map.get("autIdem2"));
        assertEquals(lineSep, map.get("accidentDate"));
        assertEquals(lineSep, map.get("additionalPayment"));
        assertEquals(lineSep, map.get("withoutPayment"));
        assertEquals(lineSep, map.get("workAccident"));
        assertEquals(lineSep, map.get("factor2"));
        assertEquals(lineSep, map.get("factor1"));
        assertEquals("Azithromycin 500mg 1-0-0 für " + lineSep + "3 Tage" + lineSep + "Amoxicillin 500mg 1-1-1 in " + lineSep + "Kombination mit" + lineSep + "Metronidazol  400mg  1-0-1 " + lineSep, map.get("medication"));
        assertEquals("Heckner" + lineSep + "Dr. Markus    " + lineSep + "Berliner Str. " + lineSep + "D 14513 Teltow" + lineSep, map.get("nameAndAddress"));
        assertEquals(lineSep, map.get("sprBedarf"));
        assertEquals(lineSep, map.get("factor3"));
        assertEquals(lineSep, map.get("accidentOrganization"));
        assertEquals(lineSep, map.get("begrPflicht"));
        assertEquals("  30000000" + lineSep, map.get("practitionerNumber"));
        assertEquals(" 30000000" + lineSep, map.get("locationNumber"));
        assertEquals(lineSep, map.get("aid"));
        assertEquals("  3000000" + lineSep, map.get("status"));
        assertEquals(lineSep, map.get("vaccination"));

    }
    
    private void logExtraction(Map<String, String> map) {
        System.out.println(map.entrySet().stream().map((e) -> "        assertEquals(\""+e.getValue().replaceAll(lineSep, "\\\\n")+"\", map.get(\""+e.getKey()+"\"));").collect(Collectors.joining(lineSep)));
    }
    

}
