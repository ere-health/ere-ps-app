package health.ere.ps.event;

import java.util.ArrayList;
import java.util.List;

public class SSHConnectionOfferingEvent {

    private Integer port;
    private String host;
    private String prescriptionUrl;
    private String identityServerUrl;
    private List<Integer> ports = new ArrayList<>();

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPrescriptionUrl() {
        return this.prescriptionUrl;
    }

    public void setPrescriptionUrl(String prescriptionUrl) {
        this.prescriptionUrl = prescriptionUrl;
    }

    public String getIdentityServerUrl() {
        return this.identityServerUrl;
    }

    public void setIdentityServerUrl(String identityServerUrl) {
        this.identityServerUrl = identityServerUrl;
    }

    public List<Integer> getPorts() {
        return this.ports;
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }
    
}
