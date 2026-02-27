package health.ere.ps.service.gematik;

import health.ere.ps.config.RuntimeConfig;
import lombok.Data;

@Data
public class FailedRejectEntry {

    private String prescriptionId;
    private String secret;
    private RuntimeConfig runtimeConfig;

    public FailedRejectEntry() {
    }

    public FailedRejectEntry(String prescriptionId, String secret, RuntimeConfig runtimeConfig) {
        this.prescriptionId = prescriptionId;
        this.secret = secret;
        this.runtimeConfig = runtimeConfig;
    }
}
