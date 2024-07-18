package health.ere.ps.service.gematik;

import health.ere.ps.config.RuntimeConfig;

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

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(String prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public RuntimeConfig getRuntimeConfig() {
        return runtimeConfig;
    }

    public void setRuntimeConfig(RuntimeConfig runtimeConfig) {
        this.runtimeConfig = runtimeConfig;
    }
}
