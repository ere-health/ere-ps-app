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

import static health.ere.ps.service.extractor.TemplateProfile.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SVGExtractorTest {


    private static Logger log = Logger.getLogger(SVGExtractorTest.class.getName());

    private String lineSep = System.lineSeparator();

    @Test
    void testExtractData_CGM_Z1() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(CGM_Z1.configuration, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(getClass().getResourceAsStream("/muster-16-print-samples/cgm-z1-manuel-blechschmidt.pdf")));

        // logExtraction(map);

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


    @Disabled("Currently failing. Reference is being made to file test1.pdf which " +
            "cannot be found, particularly on the machine of a developer who does not have access " +
            "to this file after checking out the main branch.")
    @Test
    void testExtractData_CGMTurboMed() throws URISyntaxException, IOException, XMLStreamException {

        SVGExtractor svgExtractor = new SVGExtractor(CGM_TURBO_MED.configuration, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/CGM-Turbomed/test1.pdf")));
        // logExtraction(map);

        assertEquals("Bahn - BKK                  " + lineSep, map.get("insurance"));
        assertEquals("X" + lineSep, map.get("withPayment"));
        assertEquals(lineSep, map.get("withoutPayment"));
        //TODO the order of the field rows is incorrect, debug transformation matrix in pdfbox  
        assertEquals("D 56070 Koblenz " + lineSep + "Maria Trost 21" + lineSep + "Dominik" + lineSep + "Banholzer" + lineSep, map.get("nameAndAddress"));
        assertEquals("19.07.87" + lineSep + "         " + lineSep , map.get("birthdate"));

        assertEquals("109938331" + lineSep, map.get("payor"));
        //no insurance number available
        assertEquals("5000000" + lineSep, map.get("status"));

        assertEquals("999123456" + lineSep, map.get("locationNumber"));
        assertEquals("471100815" + lineSep, map.get("practitionerNumber"));
        assertEquals("30.04.21" + lineSep, map.get("date"));

        assertEquals("Novalgin AMP N1 5X2 ml" + lineSep + "-  -  -  -" + lineSep + "-  -  -  -" + lineSep + "PZN04527098" + lineSep, map.get("medication"));
        assertEquals("0261-110110" + lineSep + "56068 Koblenz" + lineSep + "Neustraße 10" + lineSep + "Arzt--Hausarzt" + lineSep + "Dr. E-Reze pt Testarzt 2" + lineSep, map.get("practitionerText"));
    }


    @Test  @Disabled
    void testExtractApraxos() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(APRAXOS.configuration, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(getClass().getResourceAsStream("/muster-16-print-samples/apraxos_DIN_A4_Output_F-job_222.pdf")));

        //logExtraction(map);

        assertEquals("AOK Bayern Die Gesundh.       " + lineSep, map.get("insurance"));
        assertEquals(lineSep, map.get("withPayment"));
        assertEquals(lineSep, map.get("additionalPayment"));
        assertEquals("x" + lineSep, map.get("withoutPayment"));
        assertEquals("              " + lineSep + "Blechschmidt  " + lineSep + "Manuel        " + lineSep + "Droysenstr. 7 " + lineSep + "D 10629 Berlin" + lineSep, map.get("nameAndAddress"));
        assertEquals("                " + lineSep + "                " + lineSep + "        16.07.86" + lineSep + "                " + lineSep + "                " + lineSep, map.get("birthdate"));
        
        assertEquals("          " + lineSep + "108916641 " + lineSep, map.get("payor"));
        assertEquals("            " + lineSep + "            " + lineSep, map.get("insuranceNumber"));
        assertEquals("        " + lineSep + " 1000000" + lineSep, map.get("status"));
        
        assertEquals("          " + lineSep + "781234567 " + lineSep, map.get("locationNumber"));
        assertEquals("          " + lineSep + "123456767 " + lineSep, map.get("practitionerNumber"));
        assertEquals("          " + lineSep + " 25.04.21 " + lineSep, map.get("date"));
        
        assertEquals("**************************************************" + lineSep + "Ibuprofen 800mg (PZN: 01016144) »1 - 1 - 1«                 " + lineSep + "**************************************************" + lineSep, map.get("medication"));
        assertEquals("Dr. Hans Topp-Glücklich  " + lineSep + "Musterstr. 1             " + lineSep + "18107 Rostock            " + lineSep + "Tel 06151 1111111        " + lineSep + "Fax 06151 2222222        " + lineSep + "BSNR 781234567           " + lineSep + "LANR 123456767           " + lineSep + "Topp-Gluecklich@praxis.de" + lineSep, map.get("practitionerText"));
       
    }

    @Test @Disabled
    void testExtractDens1() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(DENS.configuration, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept1.pdf")));

        //logExtraction(map);

        assertEquals("DENS GmbH                     " + lineSep, map.get("insurance"));
        assertEquals("X" + lineSep, map.get("withPayment"));
        assertEquals(lineSep, map.get("withoutPayment"));
        assertEquals(lineSep, map.get("additionalPayment"));
        assertEquals("Heckner" + lineSep + "Dr. Markus       " + lineSep + "Berliner Str. 12" + lineSep + "D 14513 Teltow   " + lineSep, map.get("nameAndAddress"));
        assertEquals("     14.02.76" + lineSep + "        " + lineSep, map.get("birthdate"));
       
        assertEquals("          " + lineSep, map.get("payor"));
        assertEquals("           " + lineSep, map.get("insuranceNumber"));
        assertEquals("  3000000" + lineSep, map.get("status"));
        
        assertEquals(" 30000000" + lineSep, map.get("locationNumber"));
        assertEquals("  30000000" + lineSep, map.get("practitionerNumber"));
        assertEquals("  29.04.21" + lineSep, map.get("date"));
        
        assertEquals("Ibuprofen 600mg 1-1-1" + lineSep + "Omeprazol  40 mg  0-0-1" + lineSep + "Amoxicillin 1.000 mg 1-0-1" + lineSep, map.get("medication"));
        assertEquals("DENS GmbH" + lineSep + "Berliner Str. 13" + lineSep + "14513 Teltow" + lineSep + "03328-334540" + lineSep + "Fax: 03328-334547" + lineSep, map.get("practitionerText"));
    }

    @Disabled("Currently failing. Reference is being made to file DENSoffice - Rezept2.pdf which " +
            "cannot be found, particularly on the machine of a developer who does not have access " +
            "to this file after checking out the main branch.")
    @Test
    void testExtractDens2() throws URISyntaxException, IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(DENS.configuration, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept2.pdf")));

        //logExtraction(map);
        
        assertEquals("DENS GmbH                     " + lineSep, map.get("insurance"));
        assertEquals("X" + lineSep, map.get("withPayment"));
        assertEquals(lineSep, map.get("withoutPayment"));
        assertEquals(lineSep, map.get("additionalPayment"));
        assertEquals("Heckner" + lineSep + "Dr. Markus       " + lineSep + "Berliner Str. 12" + lineSep + "D 14513 Teltow   " + lineSep, map.get("nameAndAddress"));
        assertEquals("     14.02.76" + lineSep + "        " + lineSep, map.get("birthdate"));
        
        assertEquals("          " + lineSep, map.get("payor"));
        assertEquals("           " + lineSep, map.get("insuranceNumber"));
        assertEquals("  3000000" + lineSep, map.get("status"));
        
        assertEquals(" 30000000" + lineSep, map.get("locationNumber"));
        assertEquals("  30000000" + lineSep, map.get("practitionerNumber"));
    
        assertEquals("  29.04.21" + lineSep, map.get("date"));
        
        assertEquals("Metamizol 20 Topfen/500mg bei" + lineSep + "Bedarf, Tageshöchstdosis: 1.5Pantoprazol 40mg 1-0-0" + lineSep + "Clindamycin 600mg 1-0-1 für 5" + lineSep + "bis 7 Tage" + lineSep, map.get("medication"));
        assertEquals("DENS GmbH" + lineSep + "Berliner Str. 13" + lineSep + "14513 Teltow" + lineSep + "03328-334540" + lineSep + "Fax: 03328-334547" + lineSep, map.get("practitionerText"));
        
    }

   
    @Test @Disabled
    void testExtractDens3() throws URISyntaxException, IOException, XMLStreamException {

        SVGExtractor svgExtractor = new SVGExtractor(DENS.configuration, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept3.pdf")));

        //logExtraction(map);
        
        assertEquals("DENS GmbH                     " + lineSep, map.get("insurance"));
        assertEquals("X" + lineSep, map.get("withPayment"));
        assertEquals(lineSep, map.get("withoutPayment"));
        assertEquals(lineSep, map.get("additionalPayment"));
        assertEquals("Heckner" + lineSep + "Dr. Markus       " + lineSep + "Berliner Str. 12" + lineSep + "D 14513 Teltow   " + lineSep, map.get("nameAndAddress"));
        assertEquals("     14.02.76" + lineSep + "        " + lineSep, map.get("birthdate"));

        assertEquals("          " + lineSep, map.get("payor"));
        assertEquals("           " + lineSep, map.get("insuranceNumber"));
        assertEquals("  3000000" + lineSep, map.get("status"));

        assertEquals(" 30000000" + lineSep, map.get("locationNumber"));
        assertEquals("  30000000" + lineSep, map.get("practitionerNumber"));
        assertEquals("  29.04.21" + lineSep, map.get("date"));

        assertEquals("Azithromycin 500mg 1-0-0 für " + lineSep + "3 Tage" + lineSep + "Amoxicillin 500mg 1-1-1 in " + lineSep + "Kombination mit" + lineSep + "Metronidazol  400mg  1-0-1 " + lineSep + "für  5  bis  7  Tage " + lineSep, map.get("medication"));
        assertEquals("DENS GmbH" + lineSep + "Berliner Str. 13" + lineSep + "14513 Teltow" + lineSep + "03328-334540" + lineSep + "Fax: 03328-334547" + lineSep, map.get("practitionerText"));

    }
    
    @Test @Disabled
    void testExtractDens4() throws URISyntaxException, IOException, XMLStreamException {

        SVGExtractor svgExtractor = new SVGExtractor(DENS.configuration, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept4.pdf")));

        //logExtraction(map);
        
        assertEquals("DENS GmbH                     " + lineSep, map.get("insurance"));
        assertEquals("X" + lineSep, map.get("withPayment"));
        assertEquals(lineSep, map.get("withoutPayment"));
        assertEquals(lineSep, map.get("additionalPayment"));
        assertEquals("Heckner" + lineSep + "Dr. Markus       " + lineSep + "Berliner Str. 12" + lineSep + "D 14513 Teltow   " + lineSep, map.get("nameAndAddress"));
        assertEquals("     14.02.76" + lineSep + "        " + lineSep, map.get("birthdate"));
    
        assertEquals("          " + lineSep, map.get("payor"));
        assertEquals("           " + lineSep, map.get("insuranceNumber"));
        assertEquals("  3000000" + lineSep, map.get("status"));

        assertEquals(" 30000000" + lineSep, map.get("locationNumber"));
        assertEquals("  30000000" + lineSep, map.get("practitionerNumber"));
        assertEquals("  29.04.21" + lineSep, map.get("date"));

        assertEquals("Cefuroxim 500mg 1-0-1" + lineSep + "Ibuprofen 600mg 1-1-1" + lineSep + "Metamizol 20 Topfen/500mg bei" + lineSep + "Bedarf" + lineSep, map.get("medication"));
        assertEquals("DENS GmbH" + lineSep + "Berliner Str. 13" + lineSep + "14513 Teltow" + lineSep + "03328-334540" + lineSep + "Fax: 03328-334547" + lineSep, map.get("practitionerText"));
       
    }
    
    @Test @Disabled
    void testExtractDens5() throws URISyntaxException, IOException, XMLStreamException {

        SVGExtractor svgExtractor = new SVGExtractor(DENS.configuration, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept5.pdf")));

        //logExtraction(map);
        
        assertEquals("DENS GmbH                     " + lineSep, map.get("insurance"));
        assertEquals("X" + lineSep, map.get("withPayment"));
        assertEquals(lineSep, map.get("withoutPayment"));
        assertEquals(lineSep, map.get("additionalPayment"));
        assertEquals("Heckner" + lineSep + "Dr. Markus       " + lineSep + "Berliner Str. 12" + lineSep + "D 14513 Teltow   " + lineSep, map.get("nameAndAddress"));
        assertEquals("     14.02.76" + lineSep + "        " + lineSep, map.get("birthdate"));

        assertEquals("          " + lineSep, map.get("payor"));
        assertEquals("           " + lineSep, map.get("insuranceNumber"));
        assertEquals("  3000000" + lineSep, map.get("status"));

        assertEquals(" 30000000" + lineSep, map.get("locationNumber"));
        assertEquals("  30000000" + lineSep, map.get("practitionerNumber"));
        assertEquals("  29.04.21" + lineSep, map.get("date"));

        assertEquals("Amoxicillin 3.000mg 1 Stunde " + lineSep + "vor dem Eingriff" + lineSep + "Abschwellende  Nasentropfen  " + lineSep + "(z.B.  Xylomet-hazolin) 6x " + lineSep + "Inhalationen" + lineSep, map.get("medication"));
        assertEquals("DENS GmbH" + lineSep + "Berliner Str. 13" + lineSep + "14513 Teltow" + lineSep + "03328-334540" + lineSep + "Fax: 03328-334547" + lineSep, map.get("practitionerText"));

    }
    
    @Test @Disabled
    void testExtractDens6() throws URISyntaxException, IOException, XMLStreamException {

        SVGExtractor svgExtractor = new SVGExtractor(DENS.configuration, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept6.pdf")));

        //logExtraction(map);

        assertEquals("DENS GmbH                     " + lineSep, map.get("insurance"));
        assertEquals("X" + lineSep, map.get("withPayment"));
        assertEquals(lineSep, map.get("withoutPayment"));
        assertEquals(lineSep, map.get("additionalPayment"));
        assertEquals("Heckner" + lineSep + "Dr. Markus       " + lineSep + "Berliner Str. 12" + lineSep + "D 14513 Teltow   " + lineSep, map.get("nameAndAddress"));
        assertEquals("     14.02.76" + lineSep + "        " + lineSep, map.get("birthdate"));

        assertEquals("          " + lineSep, map.get("payor"));
        assertEquals("           " + lineSep, map.get("insuranceNumber"));
        assertEquals("  3000000" + lineSep, map.get("status"));

        assertEquals(" 30000000" + lineSep, map.get("locationNumber"));
        assertEquals("  30000000" + lineSep, map.get("practitionerNumber"));
        assertEquals("  29.04.21" + lineSep, map.get("date"));

        assertEquals("Diclofenac 75mg 1-0-1" + lineSep + "Diazepam 5mg 0-0-1" + lineSep + "Einnahmedauer begrenzt auf " + lineSep + "weniger als 1 Woche " + lineSep, map.get("medication"));
        assertEquals("DENS GmbH" + lineSep + "Berliner Str. 13" + lineSep + "14513 Teltow" + lineSep + "03328-334540" + lineSep + "Fax: 03328-334547" + lineSep, map.get("practitionerText"));
    }
    
    @Test @Disabled
    void testExtractDens7() throws URISyntaxException, IOException, XMLStreamException {

        SVGExtractor svgExtractor = new SVGExtractor(DENS.configuration, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept7.pdf")));

        //logExtraction(map);

        assertEquals("DENS GmbH                     " + lineSep, map.get("insurance"));
        assertEquals("X" + lineSep, map.get("withPayment"));
        assertEquals(lineSep, map.get("withoutPayment"));
        assertEquals(lineSep, map.get("additionalPayment"));
        assertEquals("Heckner" + lineSep + "Dr. Markus       " + lineSep + "Berliner Str. 12" + lineSep + "D 14513 Teltow   " + lineSep, map.get("nameAndAddress"));
        assertEquals("     14.02.76" + lineSep + "        " + lineSep, map.get("birthdate"));

        assertEquals("          " + lineSep, map.get("payor"));
        assertEquals("           " + lineSep, map.get("insuranceNumber"));
        assertEquals("  3000000" + lineSep, map.get("status"));

        assertEquals(" 30000000" + lineSep, map.get("locationNumber"));
        assertEquals("  30000000" + lineSep, map.get("practitionerNumber"));
        assertEquals("  29.04.21" + lineSep, map.get("date"));

        assertEquals("Ciprofloxacin   500mg   " + lineSep + "morgens   und   abends " + lineSep + "Clavulansäure 125mg 1-0-1" + lineSep + "Xylomet-hazolin 6x täglich" + lineSep, map.get("medication"));
        assertEquals("DENS GmbH" + lineSep + "Berliner Str. 13" + lineSep + "14513 Teltow" + lineSep + "03328-334540" + lineSep + "Fax: 03328-334547" + lineSep, map.get("practitionerText"));
    }
    
    @Test @Disabled
    void testExtractDens8() throws URISyntaxException, IOException, XMLStreamException {

        SVGExtractor svgExtractor = new SVGExtractor(DENS.configuration, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept8.pdf")));

        //logExtraction(map);

        assertEquals("DENS GmbH                     " + lineSep, map.get("insurance"));
        assertEquals("X" + lineSep, map.get("withPayment"));
        assertEquals(lineSep, map.get("withoutPayment"));
        assertEquals(lineSep, map.get("additionalPayment"));
        assertEquals("Heckner" + lineSep + "Dr. Markus       " + lineSep + "Berliner Str. 12" + lineSep + "D 14513 Teltow   " + lineSep, map.get("nameAndAddress"));
        assertEquals("     14.02.76" + lineSep + "        " + lineSep, map.get("birthdate"));

        assertEquals("          " + lineSep, map.get("payor"));
        assertEquals("           " + lineSep, map.get("insuranceNumber"));
        assertEquals("  3000000" + lineSep, map.get("status"));

        assertEquals(" 30000000" + lineSep, map.get("locationNumber"));
        assertEquals("  30000000" + lineSep, map.get("practitionerNumber"));
        assertEquals("  29.04.21" + lineSep, map.get("date"));

        assertEquals("Fluoretten 0,25 mg " + lineSep + "Zymafluor 0,5mg" + lineSep + "Bifluorid 6 %" + lineSep, map.get("medication"));
        assertEquals("DENS GmbH" + lineSep + "Berliner Str. 13" + lineSep + "14513 Teltow" + lineSep + "03328-334540" + lineSep + "Fax: 03328-334547" + lineSep, map.get("practitionerText"));
    }
    
    @Test @Disabled
    void testExtractDens10() throws URISyntaxException, IOException, XMLStreamException {

        SVGExtractor svgExtractor = new SVGExtractor(DENS.configuration, true);
        Map<String, String> map = svgExtractor.extract(PDDocument.load(new FileInputStream("../secret-test-print-samples/DENS-GmbH/DENSoffice - Rezept10.pdf")));

        logExtraction(map);

        assertEquals("DENS GmbH                     " + lineSep, map.get("insurance"));
        assertEquals("X" + lineSep, map.get("withPayment"));
        assertEquals(lineSep, map.get("withoutPayment"));
        assertEquals(lineSep, map.get("additionalPayment"));
        assertEquals("Heckner" + lineSep + "Dr. Markus       " + lineSep + "Berliner Str. 12" + lineSep + "D 14513 Teltow   " + lineSep, map.get("nameAndAddress"));
        assertEquals("     14.02.76" + lineSep + "        " + lineSep, map.get("birthdate"));

        assertEquals("          " + lineSep, map.get("payor"));
        assertEquals("           " + lineSep, map.get("insuranceNumber"));
        assertEquals("  3000000" + lineSep, map.get("status"));

        assertEquals(" 30000000" + lineSep, map.get("locationNumber"));
        assertEquals("  30000000" + lineSep, map.get("practitionerNumber"));
        assertEquals("  29.04.21" + lineSep, map.get("date"));

        assertEquals("Duraphat 5 % immer nach dem " + lineSep + "Essen auftragen" + lineSep + "Fluor Protector bitte " + lineSep + "mehrmals täglich einnehmen" + lineSep + "Multifluorid verordnet " + lineSep + "durch Dr. Mustermann" + lineSep, map.get("medication"));
        assertEquals("DENS GmbH" + lineSep + "Berliner Str. 13" + lineSep + "14513 Teltow" + lineSep + "03328-334540" + lineSep + "Fax: 03328-334547" + lineSep, map.get("practitionerText"));
    }


    private void logExtraction(Map<String, String> map) {
        System.out.println(map.entrySet().stream().map((e) -> "        assertEquals(\""+e.getValue().replaceAll(lineSep, "\\\\n")+"\", map.get(\""+e.getKey()+"\"));").collect(Collectors.joining(lineSep)));
    }
    

}
