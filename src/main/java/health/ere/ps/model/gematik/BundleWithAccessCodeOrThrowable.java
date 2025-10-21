package health.ere.ps.model.gematik;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.MedicationRequest;

import java.util.Optional;

public class BundleWithAccessCodeOrThrowable {

    private Bundle bundle;
    private String accessCode;
    private String taskId;
    private String medicationRequestId;
    private Throwable throwable;
    private byte[] signedBundle;

    public BundleWithAccessCodeOrThrowable() {
    }

    public BundleWithAccessCodeOrThrowable(Bundle bundle, String accessCode) {
        setBundle(bundle);
        this.accessCode = accessCode;
    }

    public BundleWithAccessCodeOrThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public void setBundle(Bundle bundle) {
        setTaskId(null);
        setMedicationRequestId(null);
        this.bundle = bundle;
        if (bundle == null) {
            return;
        }
        String prescriptionId = bundle.getIdentifier().getValue();
        setTaskId(prescriptionId);
        Optional<BundleEntryComponent> medicationRequestOpt = bundle.getEntry().stream()
            .filter(c -> c.getResource() instanceof MedicationRequest)
            .findAny();
        medicationRequestOpt.ifPresent(beComponent -> setMedicationRequestId(beComponent.getResource().getId()));
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public byte[] getSignedBundle() {
        return this.signedBundle;
    }

    public void setSignedBundle(byte[] signedBundle) {
        this.signedBundle = signedBundle;
    }

    public String getTaskId() {
        return this.taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getMedicationRequestId() {
        return this.medicationRequestId;
    }

    public void setMedicationRequestId(String medicationRequestId) {
        this.medicationRequestId = medicationRequestId;
    }
}