package health.ere.ps.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;

public class BundlesEvent {

    private List<Bundle> bundles = new ArrayList<>();

    public BundlesEvent(Bundle... bundles) {
        this.bundles = Arrays.asList(bundles);
    }

    public List<Bundle> getBundles() {
        return this.bundles;
    }

    public void setBundle(List<Bundle> bundles) {
        this.bundles = bundles;
    }

    public void addBundle(Bundle bundle){
        this.bundles.add(bundle);
    }

}
