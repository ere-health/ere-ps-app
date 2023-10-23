package health.ere.ps.service.muster16.parser.regxer;

import static health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field.PRACTITIONER_FIRST_NAME;
import static health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field.PRACTITIONER_LAST_NAME;
import static health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field.PRACTITIONER_NAME_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import health.ere.ps.service.muster16.parser.rgxer.delegate.practitioner.PractitionerEntryParseDelegate;

class PractitionerEntryParseDelegateTest {

    @Test
    public void test() {
        PractitionerEntryParseDelegate practitionerEntryParseDelegate = new PractitionerEntryParseDelegate("4033\nZahnärztin\nDr.dr.med.dent. Melinda Ecsédy-Heckner\nHermann-Piper Str. 37\n13403 Berlin\nTel.: 030 / 411 6725\n");
        assertEquals("Dr.dr.med.dent.", practitionerEntryParseDelegate.getDetails().get(PRACTITIONER_NAME_PREFIX));
        assertEquals("Melinda", practitionerEntryParseDelegate.getDetails().get(PRACTITIONER_FIRST_NAME));
        assertEquals("Ecsédy-Heckner", practitionerEntryParseDelegate.getDetails().get(PRACTITIONER_LAST_NAME));
    }
}
