package health.ere.ps.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.Session;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SSHConnectionOfferingEvent implements Serializable {

    private String user;
    private String secret;

    private Integer port;
    private String host;
    private String prescriptionServiceURL;
    private String idpBaseURL;
    private String idpAuthRequestRedirectURL;
    private String idpClientId;
    private Session session;

    private List<Integer> ports = new ArrayList<>();


    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSecret() {
        return this.secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

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

    @JsonIgnore
    @XmlTransient
    public Session getSession() {
        return this.session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
