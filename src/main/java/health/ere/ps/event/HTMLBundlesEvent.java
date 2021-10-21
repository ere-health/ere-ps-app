package health.ere.ps.event;

import java.util.List;

import javax.websocket.Session;

public class HTMLBundlesEvent extends AbstractEvent {
    private final List<String> bundles;

    public HTMLBundlesEvent(List<String> bundles) {
        this.bundles = bundles;
    }

    public HTMLBundlesEvent(List<String> bundles, Session replyTo, String replyToMessageId) {
        this(bundles);
        this.replyTo = replyTo;
        this.replyToMessageId = replyToMessageId; 
    }

    public List<String> getBundles() {
        return this.bundles;
    }
}
