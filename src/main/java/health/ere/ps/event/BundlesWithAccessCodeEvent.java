package health.ere.ps.event;

import java.util.List;

import javax.websocket.Session;

import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;

public class BundlesWithAccessCodeEvent extends AbstractEvent {
    private final List<List<BundleWithAccessCodeOrThrowable>> bundleWithAccessCodeOrThrowable;

    public BundlesWithAccessCodeEvent(List<List<BundleWithAccessCodeOrThrowable>> bundleWithAccessCodeOrThrowable) {
        this.bundleWithAccessCodeOrThrowable = bundleWithAccessCodeOrThrowable;
    }

    public BundlesWithAccessCodeEvent(List<List<BundleWithAccessCodeOrThrowable>> bundleWithAccessCodeOrThrowable2,
            Session replyTo, String replyToMessageId) {
        this(bundleWithAccessCodeOrThrowable2);
        this.replyTo = replyTo;
        this.replyToMessageId = replyToMessageId;
    }

    public List<List<BundleWithAccessCodeOrThrowable>> getBundleWithAccessCodeOrThrowable() {
        return bundleWithAccessCodeOrThrowable;
    }
}
