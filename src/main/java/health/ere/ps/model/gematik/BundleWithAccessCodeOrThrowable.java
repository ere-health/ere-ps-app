package health.ere.ps.model.gematik;

import org.hl7.fhir.r4.model.Bundle;

public class BundleWithAccessCodeOrThrowable {
    private Bundle bundle;
    private String accessCode;
    private Throwable throwable;

    public BundleWithAccessCodeOrThrowable(Bundle bundle, String accessCode) {
        this.bundle = bundle;
        this.accessCode = accessCode;
    }

    public BundleWithAccessCodeOrThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
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
}
