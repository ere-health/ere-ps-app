package health.ere.ps.event;

import java.util.List;

import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;

public class BundlesWithAccessCodeEvent {
    public List<List<BundleWithAccessCodeOrThrowable>> bundleWithAccessCodeOrThrowable;
    public BundlesWithAccessCodeEvent(List<List<BundleWithAccessCodeOrThrowable>> bundleWithAccessCodeOrThrowable) {
        this.bundleWithAccessCodeOrThrowable = bundleWithAccessCodeOrThrowable;
    }
    
}
