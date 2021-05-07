package health.ere.ps.service.gematik;

import org.hl7.fhir.r4.model.Bundle;

public class BundleWithAccessCode {
    Bundle bundle;
    String accessCode;

    public BundleWithAccessCode(Bundle bundle, String accessCode) {
        this.bundle = bundle;
        this.accessCode = accessCode;
    }
}
