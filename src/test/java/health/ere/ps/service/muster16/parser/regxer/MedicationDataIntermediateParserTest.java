package health.ere.ps.service.muster16.parser.regxer;

import health.ere.ps.service.muster16.parser.rgxer.delegate.medication.MedicationEntryParseDelegate;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MedicationDataIntermediateParserTest {

    private final String lineSep = System.lineSeparator();

    @Test
    void testParsePrescription_CGM_Z1() {
        String entry = "Amoxicillin 1000mg N2\n" +
                "3x täglich alle 8 Std\n" +
                "-  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -";

        MedicationEntryParseDelegate parser = new MedicationEntryParseDelegate();
        List<String> result = parser.parse(entry);

        assertEquals(1, result.size());
        assertTrue(result.get(0).startsWith("Amoxicillin"));
    }

    @Test
    void testPrescriptionIntermediateParsing_CGMTurboMed() {
        String entry = "Novalgin AMP N1 5X2 ml\n" +
                "-  -  -  -\n" +
                "-  -  -  -\n" +
                "PZN04527098";


        MedicationEntryParseDelegate parser = new MedicationEntryParseDelegate();
        List<String> result = parser.parse(entry);

        assertEquals(1, result.size());
        assertTrue(result.get(0).startsWith("Novalgin"));
    }

    @Test
    void testPrescriptionIntermediateParsing_Apraxos() {
        String entry = "**************************************************\n" +
                "Ibuprofen 800mg (PZN: 01016144) »1 - 1 - 1«                 \n" +
                "**************************************************";


        MedicationEntryParseDelegate parser = new MedicationEntryParseDelegate();
        List<String> result = parser.parse(entry);

        assertEquals(1, result.size());
        assertTrue(result.get(0).startsWith("Ibuprofen"));
    }

    @Test
    void testPrescriptionInitialParsing_Dens1() {

        String entry = "Ibuprofen 600mg 1-1-1\n" +
                "Omeprazol  40 mg  0-0-1\n" +
                "Amoxicillin 1.000 mg 1-0-1";

        MedicationEntryParseDelegate parser = new MedicationEntryParseDelegate();
        List<String> result = parser.parse(entry);

        assertEquals(3, result.size());
        assertTrue(result.get(0).startsWith("Ibuprofen"));
        assertTrue(result.get(1).startsWith("Omeprazol"));
        assertTrue(result.get(2).startsWith("Amoxicillin"));
    }

    @Test
    void testPrescriptionInitialParsing_DENS3() {

        MedicationEntryParseDelegate parser = new MedicationEntryParseDelegate();
        String entry = "Azithromycin 500mg 1-0-0 für\n" +
                "3 Tage\n" +
                "Amoxicillin 500mg 1-1-1 in\n" +
                "Kombination mit\n" +
                "Metronidazol 400mg 1-0-1\n" +
                "für 5 bis 7 Tage";
        List<String> result = parser.parse(entry);

        assertEquals(3, result.size());
        assertTrue(result.get(0).startsWith("Azithromycin"));
        assertTrue(result.get(1).startsWith("Amoxicillin"));
        assertTrue(result.get(2).startsWith("Metronidazol"));
    }

    @Test
    void testPrescriptionInitialParsing_DENS4() {
        MedicationEntryParseDelegate parser = new MedicationEntryParseDelegate();

        String entry = "Cefuroxim 500mg 1-0-1\n" +
                "Ibuprofen 600mg 1-1-1\n" +
                "Metamizol 20 Topfen/500mg bei\n" +
                "Bedarf";

        List<String> result = parser.parse(entry);

        assertEquals(3, result.size());
        assertTrue(result.get(0).startsWith("Cefuroxim"));
        assertTrue(result.get(1).startsWith("Ibuprofen"));
        assertTrue(result.get(2).startsWith("Metamizol"));
    }

    @Test
    @Disabled
    void testPrescriptionInitialParsing_DENS5() {

        MedicationEntryParseDelegate parser = new MedicationEntryParseDelegate();

        String entry = "Amoxicillin 3.000mg 1 Stunde \n" +
                "vor dem Eingriff\n" +
                "Abschwellende  Nasentropfen  \n" +
                "(z.B.  Xylomet-hazolin) 6x \n" +
                "Inhalationen";

        List<String> result = parser.parse(entry);

        assertEquals(2, result.size());
        assertTrue(result.get(0).startsWith("Amoxicillin"));
        assertTrue(result.get(1).startsWith("Abschwellende"));
    }

    @Test
    void testPrescriptionInitialParsing_DENS6() {

        MedicationEntryParseDelegate parser = new MedicationEntryParseDelegate();

        String entry = "Diclofenac 75mg 1-0-1\n" +
                "Diazepam 5mg 0-0-1\n" +
                "Einnahmedauer begrenzt auf \n" +
                "weniger als 1 Woche ";

        List<String> result = parser.parse(entry);

        assertEquals(2, result.size());
        assertTrue(result.get(0).startsWith("Diclofenac"));
        assertTrue(result.get(1).startsWith("Diazepam"));
    }

    @Test
    @Disabled
    void testPrescriptionInitialParsing_DENS7() {

        MedicationEntryParseDelegate parser = new MedicationEntryParseDelegate();

        String entry = "Ciprofloxacin   500mg   \n" +
                "morgens   und   abends \n" +
                "Clavulansäure 125mg 1-0-1\n" +
                "Xylomet-hazolin 6x täglich";

        List<String> result = parser.parse(entry);

        assertEquals(3, result.size());
        assertTrue(result.get(0).startsWith("Ciprofloxacin"));
        assertTrue(result.get(1).startsWith("Clavulansäure"));
        assertTrue(result.get(2).startsWith("Xylomet-hazolin"));
    }

    @Test
    void testPrescriptionInitialParsing_DENS8() {

        MedicationEntryParseDelegate parser = new MedicationEntryParseDelegate();

        String entry = "Fluoretten 0,25 mg\n" +
                "Zymafluor 0,5mg\n" +
                "Bifluorid 6 %";

        List<String> result = parser.parse(entry);

        assertEquals(3, result.size());
        assertTrue(result.get(0).startsWith("Fluoretten"));
        assertTrue(result.get(1).startsWith("Zymafluor"));
        assertTrue(result.get(2).startsWith("Bifluorid"));
    }

    @Test
    void testPrescriptionInitialParsing_DENS10() {

        MedicationEntryParseDelegate parser = new MedicationEntryParseDelegate();

        String entry = "Duraphat 5 % immer nach dem \n" +
                "Essen auftragen\n" +
                "Fluor Protector bitte \n" +
                "mehrmals täglich einnehmen\n" +
                "Multifluorid verordnet \n" +
                "durch Dr. Mustermann";

        List<String> result = parser.parse(entry);

        assertEquals(3, result.size());
        assertTrue(result.get(0).startsWith("Duraphat"));
        assertTrue(result.get(1).startsWith("Fluor"));
        assertTrue(result.get(2).startsWith("Multifluorid"));
    }
}
