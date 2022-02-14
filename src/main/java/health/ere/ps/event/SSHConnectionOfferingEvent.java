package health.ere.ps.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SSHConnectionOfferingEvent implements Serializable {

    private Integer port;
    private String host;
    private String prescriptionServiceURL;
    private String idpBaseURL;
    private String idpAuthRequestRedirectURL;
    private String idpClientId;

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

    public String getPrescriptionServiceURL() {
        return this.prescriptionServiceURL;
    }

    public void setPrescriptionServiceURL(String prescriptionServiceURL) {
        this.prescriptionServiceURL = prescriptionServiceURL;
    }



    public List<Integer> getPorts() {
        return this.ports;
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }

    public String getIdpBaseURL() {
        return this.idpBaseURL;
    }

    public void setIdpBaseURL(String idpBaseURL) {
        this.idpBaseURL = idpBaseURL;
    }

    public String getIdpAuthRequestRedirectURL() {
        return this.idpAuthRequestRedirectURL;
    }

    public void setIdpAuthRequestRedirectURL(String idpAuthRequestRedirectURL) {
        this.idpAuthRequestRedirectURL = idpAuthRequestRedirectURL;
    }

    public String getIdpClientId() {
        return this.idpClientId;
    }

    public void setIdpClientId(String idpClientId) {
        this.idpClientId = idpClientId;
    }
    
}
