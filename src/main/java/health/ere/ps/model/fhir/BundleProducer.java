package health.ere.ps.model.fhir;

import org.hl7.fhir.r4.model.Bundle;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@Singleton
public class BundleProducer {
    @Produces
    @RequestScoped
    public Bundle bundleProducer() {
        return new Bundle();
    }
}
