package health.ere.ps.service.muster16.parser.regxer;

import health.ere.ps.model.muster16.MedicationString;
import health.ere.ps.service.muster16.parser.rgxer.delegate.medication.MedicationParseDelegate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MedicationParserTest {

    private static MedicationParseDelegate parser;

    @BeforeAll
    static void init() {
        parser = new MedicationParseDelegate();
    }


    @Test
    void testParsePrescription_CGM_Z1() {
        String entry = "Amoxicillin 1000mg N2 3x täglich alle 8 Std";
//        String entry = "Amoxicillin 3.000mg 1 Stunde \nvor dem Eingriff\nAbschwellende  Nasentropfen  \n(z.B.  Xylomet-hazolin) 6x \nInhalationen";


        List<MedicationString> result = parser.parse(entry);

        assertEquals(1, result.size());

        MedicationString result1 = result.get(0);
        assertEquals("N2", result1.getSize());
        assertEquals("Amoxicillin 1000mg N2", result1.getName());
        assertEquals("3x täglich alle 8 Std", result1.getDosage());
    }

    @Test
    @Disabled
    void testParseApraxos() throws URISyntaxException, IOException, XMLStreamException {
        String entry = "Ibuprofen 800mg (PZN: 01016144) »1 - 1 - 1«  ";

        List<MedicationString> result = parser.parse(entry);

        assertEquals(1, result.size());

        MedicationString result1 = result.get(0);
        assertEquals("01016144", result1.getPzn());
        assertEquals("N2", result1.getSize());
        assertEquals("Ibuprofen 800mg", result1.getName());
        assertEquals("1-1-1", result1.getDosage());
    }

    @Test
    void testParseDens1() {
        String entry = "Ibuprofen 600mg 1-1-1\n" +
                "Omeprazol  40 mg  0-0-1\n" +
                "Amoxicillin 1.000 mg 1-0-1";


        List<MedicationString> result = parser.parse(entry);

        assertEquals(3, result.size());

        MedicationString result1 = result.get(0);
        assertEquals("Ibuprofen 600mg", result1.getName());
        assertEquals("1-1-1", result1.getDosage());


        MedicationString result2 = result.get(1);
        assertEquals("Omeprazol 40 mg", result2.getName());
        assertEquals("0-0-1", result2.getDosage());

        MedicationString result3 = result.get(2);
        assertEquals("Amoxicillin 1.000 mg", result3.getName());
        assertEquals("1-0-1", result3.getDosage());
    }

    @Test
    void testParseDens2() {
        String entry = "Metamizol 20 Topfen/500mg bei Bedarf, Tageshöchstdosis: 1.5\n" +
                "Pantoprazol 40mg 1-0-0\n" +
                "Clindamycin 600mg 1-0-1 für 5\n" +
                "bis 7 Tage";

        List<MedicationString> result = parser.parse(entry);

        assertEquals(3, result.size());

        MedicationString result1 = result.get(0);
        assertEquals("Metamizol 20 Topfen/500mg", result1.getName());
        assertEquals("bei Bedarf, Tageshöchstdosis: 1.5", result1.getDosage());

        MedicationString result2 = result.get(1);
        assertEquals("Pantoprazol 40mg", result2.getName());
        assertEquals("1-0-0", result2.getDosage());

        MedicationString result3 = result.get(2);
        assertEquals("Clindamycin 600mg", result3.getName());
        assertEquals("1-0-1 für 5 bis 7 Tage", result3.getDosage());
    }

    @Test
    void testParseDens3() {
        String entry = "Azithromycin 500mg 1-0-0 für \n" +
                "3 Tage\n" +
                "Amoxicillin 500mg 1-1-1 in \n" +
                "Kombination mit\n" +
                "Metronidazol  400mg  1-0-1 \n" +
                "für  5  bis  7  Tage ";

        List<MedicationString> result = parser.parse(entry);

        assertEquals(3, result.size());

        MedicationString result1 = result.get(0);
        assertEquals("Azithromycin 500mg", result1.getName());
        assertEquals("1-0-0 für 3 Tage", result1.getDosage());

        MedicationString result2 = result.get(1);
        assertEquals("Amoxicillin 500mg", result2.getName());
        assertEquals("1-1-1 in Kombination mit", result2.getDosage());

        MedicationString result3 = result.get(2);
        assertEquals("Metronidazol 400mg", result3.getName());
        assertEquals("1-0-1 für 5 bis 7 Tage", result3.getDosage());
    }

    @Test
    void testParseDens4() {
        String entry = "Cefuroxim 500mg 1-0-1\n" +
                "Ibuprofen 600mg 1-1-1\n" +
                "Metamizol 20 Topfen/500mg bei\n" +
                "Bedarf";

        List<MedicationString> result = parser.parse(entry);

        assertEquals(3, result.size());

        MedicationString result1 = result.get(0);
        assertEquals("Cefuroxim 500mg", result1.getName());
        assertEquals("1-0-1", result1.getDosage());

        MedicationString result2 = result.get(1);
        assertEquals("Ibuprofen 600mg", result2.getName());
        assertEquals("1-1-1", result2.getDosage());

        MedicationString result3 = result.get(2);
        assertEquals("Metamizol 20 Topfen/500mg", result3.getName());
        assertEquals("bei Bedarf", result3.getDosage());
    }

    @Test
    void testParseDens6() {
        String entry = "Diclofenac 75mg 1-0-1\n" +
                "Diazepam 5mg 0-0-1\n" +
                "Einnahmedauer begrenzt auf \n" +
                "weniger als 1 Woche ";

        List<MedicationString> result = parser.parse(entry);

        assertEquals(2, result.size());

        MedicationString result1 = result.get(0);
        assertEquals("Diclofenac 75mg", result1.getName());
        assertEquals("1-0-1", result1.getDosage());

        MedicationString result2 = result.get(1);
        assertEquals("Diazepam 5mg", result2.getName());
        assertEquals("0-0-1 Einnahmedauer begrenzt auf weniger als 1 Woche", result2.getDosage());
    }

    @Test
    @Disabled
    void testParseDens7() {
        String entry = "Ciprofloxacin   500mg   \n" +
                "morgens   und   abends \n" +
                "Clavulansäure 125mg 1-0-1\n" +
                "Xylometazolin 6x täglich";

        List<MedicationString> result = parser.parse(entry);

        assertEquals(2, result.size());

        MedicationString result1 = result.get(0);
        assertEquals("Ciprofloxacin 500mg", result1.getName());
        assertEquals("morgens und abends", result1.getDosage());

        MedicationString result2 = result.get(1);
        assertEquals("Clavulansäure 125mg", result2.getName());
        assertEquals("1-0-1", result2.getDosage());

        MedicationString result3 = result.get(3);
        assertEquals("Xylomet-hazolin", result2.getName());
        assertEquals("6x täglich", result2.getDosage());
    }

    @Test
    void testParseDens8() {
        String entry = "Fluoretten 0,25 mg \n" +
                "Zymafluor 0,5mg\n" +
                "Bifluorid 6 %";

        List<MedicationString> result = parser.parse(entry);

        assertEquals(3, result.size());

        MedicationString result1 = result.get(0);
        assertEquals("Fluoretten 0,25 mg", result1.getName());
        assertEquals("", result1.getDosage());

        MedicationString result2 = result.get(1);
        assertEquals("Zymafluor 0,5mg", result2.getName());
        assertEquals("", result2.getDosage());

        MedicationString result3 = result.get(2);
        assertEquals("Bifluorid 6 %", result3.getName());
        assertEquals("", result3.getDosage());

    }

    @Test
    @Disabled
    void testParseDens10() {
        String entry = "Duraphat 5 % immer nach dem \n" +
                "Essen auftragen\n" +
                "Fluor Protector bitte \n" +
                "mehrmals täglich einnehmen\n" +
                "Multifluorid verordnet \n" +
                "durch Dr. Mustermann";

        List<MedicationString> result = parser.parse(entry);

        assertEquals(3, result.size());

        MedicationString result1 = result.get(0);
        assertEquals("Duraphat 5 %", result1.getName());
        assertEquals("immer nach dem Essen auftragen", result1.getDosage());

        MedicationString result2 = result.get(1);
        assertEquals("Fluor Protector", result2.getName());
        assertEquals("bitte mehrmals täglich einnehmen", result2.getDosage());

        MedicationString result3 = result.get(2);
        assertEquals("Multifluorid", result3.getName());
        assertEquals("verordnet durch Dr. Mustermann", result3.getDosage());
    }

}
