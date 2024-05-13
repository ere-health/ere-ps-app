package health.ere.ps.event;

import java.util.List;

import jakarta.websocket.Session;

import org.hl7.fhir.r4.model.Bundle;

public final class BundlesEvent extends AbstractEvent {

    private final List<Bundle> bundles;

    public BundlesEvent(List<Bundle> bundles) {
        this.bundles = bundles;
    }

    public BundlesEvent(List<Bundle> bundles, Session replyTo, String id) {
        this(bundles);
        this.replyTo = replyTo;
        this.id = id;
    }

    public List<Bundle> getBundles() {
        return this.bundles;
    }

}
