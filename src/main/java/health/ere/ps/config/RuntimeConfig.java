package health.ere.ps.config;

import java.util.Objects;

import javax.enterprise.inject.Alternative;

@Alternative
public class RuntimeConfig extends UserConfig {
    protected String eHBAHandle;
    protected String SMCBHandle;  

    public RuntimeConfig() {
    }

    public RuntimeConfig(String eHBAHandle, String SMCBHandle) {
        this.eHBAHandle = eHBAHandle;
        this.SMCBHandle = SMCBHandle;
    }

    public String getEHBAHandle() {
        return this.eHBAHandle;
    }

    public void setEHBAHandle(String eHBAHandle) {
        this.eHBAHandle = eHBAHandle;
    }

    public String getSMCBHandle() {
        return this.SMCBHandle;
    }

    public void setSMCBHandle(String SMCBHandle) {
        this.SMCBHandle = SMCBHandle;
    }

    public RuntimeConfig eHBAHandle(String eHBAHandle) {
        setEHBAHandle(eHBAHandle);
        return this;
    }

    public RuntimeConfig SMCBHandle(String SMCBHandle) {
        setSMCBHandle(SMCBHandle);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof RuntimeConfig)) {
            return false;
        }
        RuntimeConfig runtimeConfig = (RuntimeConfig) o;
        return Objects.equals(eHBAHandle, runtimeConfig.eHBAHandle) && Objects.equals(SMCBHandle, runtimeConfig.SMCBHandle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eHBAHandle, SMCBHandle);
    }

    @Override
    public String toString() {
        return "{" +
            " eHBAHandle='" + getEHBAHandle() + "'" +
            ", SMCBHandle='" + getSMCBHandle() + "'" +
            "}";
    }
}
