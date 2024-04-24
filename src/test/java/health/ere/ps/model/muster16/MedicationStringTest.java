package health.ere.ps.model.muster16;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class MedicationStringTest {

    @Inject
    MedicationString medicationString = new MedicationString(
        "name",
        "size",
        "form",
        "dosage",
        "instructions",
        "pzn");

    @Test
    public void testMedicalStringObjectName() {
        assertEquals(medicationString.getName(), "name", "Wrong name");
    }

    @Test
    public void testMedicalStringObjectSize() {
        assertEquals(medicationString.getSize(), "size", "Wrong size");
    }

    @Test
    public void testMedicalStringObjectForm() {
        assertEquals(medicationString.getForm(), "form", "Wrong form");
    }

    @Test
    public void testMedicalStringObjectDosage() {
        assertEquals(medicationString.getDosage(), "dosage", "Wrong dosage");
    }

    @Test
    public void testMedicalStringObjectInstructions() {
        assertEquals(medicationString.getInstructions(), "instructions", "Wrong instructions");
    }

    @Test
    public void testMedicalStringObjectPzn() {
        assertEquals(medicationString.getPzn(), "pzn", "Wrong PZN");
    }

}
