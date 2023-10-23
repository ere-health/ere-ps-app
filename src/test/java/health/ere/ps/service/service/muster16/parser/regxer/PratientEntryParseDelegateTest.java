package health.ere.ps.service.muster16.parser.regxer;

import static health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field.PATIENT_STREET_NAME;
import static health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field.PATIENT_STREET_NUMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import health.ere.ps.service.muster16.parser.rgxer.delegate.patient.PatientEntryParseDelegate;

class PatientEntryParseDelegateTest {

    @Test
    public void test() {
        PatientEntryParseDelegate practitionerEntryParseDelegate = new PatientEntryParseDelegate("Ababo\nTest-Ulafa          \nAugusta Str. 6 b\nD 12345 Berlin        \n");
        assertEquals("Augusta Str. ", practitionerEntryParseDelegate.getDetails().get(PATIENT_STREET_NAME));
        assertEquals("6 b", practitionerEntryParseDelegate.getDetails().get(PATIENT_STREET_NUMBER));
    }
}
