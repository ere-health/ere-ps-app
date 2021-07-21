package health.ere.ps.event;

import java.util.List;

public class HTMLBundlesEvent {
    private final List<String> bundles;

    public HTMLBundlesEvent(List<String> bundles) {
        this.bundles = bundles;
    }

    public List<String> getBundles() {
        return this.bundles;
    }
}
