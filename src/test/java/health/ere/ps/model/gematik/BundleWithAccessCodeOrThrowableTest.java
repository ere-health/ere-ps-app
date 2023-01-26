package health.ere.ps.model.gematik;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.jupiter.api.Test;

public class BundleWithAccessCodeOrThrowableTest {
    @Test
    public void testNullContructor() {
        new BundleWithAccessCodeOrThrowable(null, null);
    }
    
    @Test
    public void testTaskIdAndMedicationId() {
        Bundle bundle = new Bundle();
        
        BundleWithAccessCodeOrThrowable b = new BundleWithAccessCodeOrThrowable(bundle, null);
        assertNull(b.getTaskId());
        assertNull(b.getMedicationRequestId());
        
        Identifier taskId = new Identifier();
        taskId.setValue("160.1234.12345");
        bundle.setIdentifier(taskId);

        b = new BundleWithAccessCodeOrThrowable(bundle, null);
        assertEquals("160.1234.12345", b.getTaskId());
        assertNull(b.getMedicationRequestId());

        BundleEntryComponent bec = new BundleEntryComponent();
        MedicationRequest mr = new MedicationRequest();
        mr.setId("12345679");
        bec.setResource(mr);
        bundle.addEntry(bec);

        b = new BundleWithAccessCodeOrThrowable(bundle, null);
        assertEquals("160.1234.12345", b.getTaskId());
        assertEquals("12345679", b.getMedicationRequestId());


    }
}
