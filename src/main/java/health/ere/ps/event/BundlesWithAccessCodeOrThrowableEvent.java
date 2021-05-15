package health.ere.ps.event;

import java.util.List;

import health.ere.ps.service.gematik.BundleWithAccessCodeOrThrowable;

public class BundlesWithAccessCodeOrThrowableEvent {
    List<List<BundleWithAccessCodeOrThrowable>> bundleWithAccessCodeOrThrowable;
    public BundlesWithAccessCodeOrThrowableEvent(List<List<BundleWithAccessCodeOrThrowable>> bundleWithAccessCodeOrThrowable) {
        this.bundleWithAccessCodeOrThrowable = bundleWithAccessCodeOrThrowable;
    }
    
}
