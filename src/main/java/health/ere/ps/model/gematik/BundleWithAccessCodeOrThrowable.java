package health.ere.ps.model.gematik;

import org.hl7.fhir.r4.model.Bundle;

public class BundleWithAccessCodeOrThrowable {
    public Bundle bundle;
    public String accessCode;
    public Throwable throwable;

    public BundleWithAccessCodeOrThrowable(Bundle bundle, String accessCode) {
        this.bundle = bundle;
        this.accessCode = accessCode;
    }

    public BundleWithAccessCodeOrThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

}
