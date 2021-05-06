package health.ere.ps.event;

import org.hl7.fhir.r4.model.Bundle;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class BundleEvent {

    private Bundle bundle;

    public BundleEvent(Bundle bundle) {
        this.bundle = bundle;
    }

    public Bundle getBundle() {
        return this.bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

}
