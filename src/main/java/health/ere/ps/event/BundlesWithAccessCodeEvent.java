package health.ere.ps.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.websocket.Session;

import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;

public class BundlesWithAccessCodeEvent extends AbstractEvent {
    private final List<List<BundleWithAccessCodeOrThrowable>> bundleWithAccessCodeOrThrowable;

    private String flowtype = "160";
    private String toKimAddress;
    private String noteToPharmacy;
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

    public String getNoteToPharmacy() {
        return this.noteToPharmacy;
    }

    public void setNoteToPharmacy(String noteToPharmacy) {
        this.noteToPharmacy = noteToPharmacy;
    }

    public Map<String,String> getKimConfigMap() {
        return this.kimConfigMap;
    }

    public void setKimConfigMap(Map<String,String> kimConfigMap) {
        this.kimConfigMap = kimConfigMap;
    }
}
