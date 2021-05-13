package health.ere.ps.service.fhir;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import health.ere.ps.event.BundleEvent;

@ApplicationScoped
public class FhirEventPublisher {
    @Inject
    BundleEvent bundleEvent;


}
