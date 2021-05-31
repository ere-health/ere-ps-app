package health.ere.ps.service.muster16.parser;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class Muster16SvgExtractorRegexParserTest {

    private final String lineSep = System.lineSeparator();

    @Test
    void testParseData_CGM_Z1() {

        Map<String, String> map = new HashMap<>();

        map.put("insurance", "TK > Brandenburg            83" + lineSep);
        map.put("withPayment", "X" + lineSep);
        map.put("withoutPayment", "" + lineSep);
        map.put("nameAndAddress", "Blechschmidt" + lineSep + "Manuel               " + lineSep + "Droysenstr. 7" + lineSep + "D 10629 Berlin   " + lineSep);
        map.put("birthdate", "16.07.86" + lineSep + "       " + lineSep);
        map.put("payor", "100696012 " + lineSep);
        map.put("insuranceNumber", "V062074590   " + lineSep);
        map.put("status", "1000000" + lineSep);
        map.put("locationNumber", " 30001234  " + lineSep);
        map.put("practitionerNumber", "30001234  " + lineSep);
        map.put("date", "13.04.21" + lineSep);
        map.put("medication", "Amoxicillin 1000mg N2" + lineSep + "3x täglich alle 8 Std" + lineSep + "-  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -" + lineSep);
        map.put("practitionerText", "1234" + lineSep + "Zahnärzte" + lineSep + "Dr. Zahnarzt Ein & Dr. Zahnarzt Zwei" + lineSep + "In der tollen Str.115" + lineSep + "12345 Berlin" + lineSep + "Tel. 030/123 4567" + lineSep);

        Muster16SvgExtractorRegexParser parser = new Muster16SvgExtractorRegexParser(map);

        assertEquals("TK > Brandenburg 83", parser.parseInsuranceCompany());
        assertEquals("100696012", parser.parseInsuranceCompanyId());
        assertEquals("Manuel", parser.parsePatientFirstName());
        assertEquals("Blechschmidt", parser.parsePatientLastName());
        assertEquals("Droysenstr.", parser.parsePatientStreetName());
        assertEquals("7", parser.parsePatientStreetNumber());
        assertEquals("10629", parser.parsePatientZipCode());
        assertEquals("Berlin", parser.parsePatientCity());
        assertEquals("16.07.86", parser.parsePatientDateOfBirth());
        assertEquals("30001234", parser.parseClinicId());
        assertEquals("30001234", parser.parseDoctorId());
        assertEquals("13.04.21", parser.parsePrescriptionDate());
        assertEquals("V062074590", parser.parsePatientInsuranceId());
    }

    @Test
    void testParse_CGMTurboMed() {

        Map<String, String> map = new HashMap<>();

        map.put("insurance", "Bahn - BKK                  " + lineSep);
        map.put("withPayment", "X" + lineSep);
        map.put("withoutPayment", lineSep);
        map.put("nameAndAddress", "D 56070 Koblenz " + lineSep + "Maria Trost 21" + lineSep + "Dominik" + lineSep + "Banholzer" + lineSep);
        map.put("birthdate", "19.07.87" + lineSep + "         " + lineSep);
        map.put("payor", "109938331" + lineSep);
        map.put("status", "5000000" + lineSep);
        map.put("locationNumber", "999123456" + lineSep);
        map.put("practitionerNumber", "471100815" + lineSep);
        map.put("date", "30.04.21" + lineSep);
        map.put("medication", "Novalgin AMP N1 5X2 ml" + lineSep + "-  -  -  -" + lineSep + "-  -  -  -" + lineSep + "PZN04527098" + lineSep);
        map.put("practitionerText", "0261-110110" + lineSep + "56068 Koblenz" + lineSep + "Neustraße 10" + lineSep + "Arzt--Hausarzt" + lineSep + "Dr. E-Reze pt Testarzt 2" + lineSep);

        Muster16SvgExtractorRegexParser parser = new Muster16SvgExtractorRegexParser(map);

        assertEquals("Bahn - BKK", parser.parseInsuranceCompany());
        assertEquals("109938331", parser.parseInsuranceCompanyId());
        assertEquals("Banholzer", parser.parsePatientFirstName());
        assertEquals("Dominik", parser.parsePatientLastName());
        assertEquals("Maria Trost", parser.parsePatientStreetName());
        assertEquals("21", parser.parsePatientStreetNumber());
        assertEquals("56070", parser.parsePatientZipCode());
        assertEquals("Koblenz", parser.parsePatientCity());
        assertEquals("19.07.87", parser.parsePatientDateOfBirth());
        assertEquals("999123456", parser.parseClinicId());
        assertEquals("471100815", parser.parseDoctorId());
        assertEquals("30.04.21", parser.parsePrescriptionDate());
    }

    @Test
    void testParse_Dens1() {

        Map<String, String> map = new HashMap<>();

        map.put("insurance", "DENS GmbH                     " + lineSep);
        map.put("withPayment", "X" + lineSep);
        map.put("withoutPayment", lineSep);
        map.put("additionalPayment", lineSep);
        map.put("nameAndAddress", "Heckner" + lineSep + "Dr. Markus       " + lineSep + "Berliner Str. 12" + lineSep + "D 14513 Teltow   " + lineSep);
        map.put("birthdate", "     14.02.76" + lineSep + "        " + lineSep);
        map.put("payor", "          " + lineSep);
        map.put("insuranceNumber", "           " + lineSep);
        map.put("status", "  3000000" + lineSep);
        map.put("locationNumber", " 30000000" + lineSep);
        map.put("practitionerNumber", "  30000000" + lineSep);
        map.put("date", "  29.04.21" + lineSep);
        map.put("medication", "Ibuprofen 600mg 1-1-1" + lineSep + "Omeprazol  40 mg  0-0-1" + lineSep + "Amoxicillin 1.000 mg 1-0-1" + lineSep);
        map.put("practitionerText", "DENS GmbH" + lineSep + "Berliner Str. 13" + lineSep + "14513 Teltow" + lineSep + "03328-334540" + lineSep + "Fax: 03328-334547" + lineSep);

        Muster16SvgExtractorRegexParser parser = new Muster16SvgExtractorRegexParser(map);

        assertEquals("DENS GmbH", parser.parseInsuranceCompany());
        assertEquals("Dr. Markus", parser.parsePatientFirstName());
        assertEquals("Heckner", parser.parsePatientLastName());
        assertEquals("Berliner Str.", parser.parsePatientStreetName());
        assertEquals("12", parser.parsePatientStreetNumber());
        assertEquals("14513", parser.parsePatientZipCode());
        assertEquals("Teltow", parser.parsePatientCity());
        assertEquals("14.02.76", parser.parsePatientDateOfBirth());
        assertEquals("30000000", parser.parseClinicId());
        assertEquals("30000000", parser.parseDoctorId());
        assertEquals("29.04.21", parser.parsePrescriptionDate());
    }
}
