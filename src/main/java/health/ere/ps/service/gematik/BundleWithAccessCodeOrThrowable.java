package health.ere.ps.service.gematik;

import org.hl7.fhir.r4.model.Bundle;

public class BundleWithAccessCodeOrThrowable {
    Bundle bundle;
    String accessCode;
    Throwable throwable;

    public BundleWithAccessCodeOrThrowable(Bundle bundle, String accessCode) {
        this.bundle = bundle;
        this.accessCode = accessCode;
    }

    public BundleWithAccessCodeOrThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

}
