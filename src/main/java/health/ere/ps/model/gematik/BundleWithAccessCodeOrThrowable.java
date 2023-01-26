package health.ere.ps.model.gematik;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.MedicationRequest;

public class BundleWithAccessCodeOrThrowable {

    private static Logger log = Logger.getLogger(BundleWithAccessCodeOrThrowable.class.getName());
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
        if(bundle == null) {
            log.warning("Setting bundle to null");
            return;
        }
        try {
            String prescriptionId = bundle.getIdentifier().getValue();
            setTaskId(prescriptionId);
            Optional<BundleEntryComponent> optionalMedicationRequest = bundle.getEntry().stream().filter(c -> {
                return (c.getResource() instanceof MedicationRequest);
            }).findAny();
            if(optionalMedicationRequest.isPresent()) {
                setMedicationRequestId(optionalMedicationRequest.get().getResource().getId());
            }
        } catch(Throwable t) {
            log.log(Level.WARNING, "Could not extract taskId and/or medicationRequest Id from Bundle", t);
        }
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
