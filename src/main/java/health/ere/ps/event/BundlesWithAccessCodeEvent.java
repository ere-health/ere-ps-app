package health.ere.ps.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.Session;

import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;

public class BundlesWithAccessCodeEvent extends AbstractEvent {
    private final List<List<BundleWithAccessCodeOrThrowable>> bundleWithAccessCodeOrThrowable;

    private String flowtype = "160";
    private String toKimAddress;
    private String noteForPharmacy;
    private Map<String,String> kimConfigMap = new HashMap<>();

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

    public String getFlowtype() {
        return this.flowtype;
    }

    public void setFlowtype(String flowtype) {
        this.flowtype = flowtype;
    }

    public String getToKimAddress() {
        return this.toKimAddress;
    }

    public void setToKimAddress(String toKimAddress) {
        this.toKimAddress = toKimAddress;
    }

    public String getNoteForPharmacy() {
        return this.noteForPharmacy;
    }

    public void setNoteForPharmacy(String noteForPharmacy) {
        this.noteForPharmacy = noteForPharmacy;
    }

    public Map<String,String> getKimConfigMap() {
        return this.kimConfigMap;
    }

    public void setKimConfigMap(Map<String,String> kimConfigMap) {
        this.kimConfigMap = kimConfigMap;
    }
}
