package health.ere.ps.service.ssh;

import java.util.ArrayList;
import java.util.List;

import health.ere.ps.event.SSHConnectionOfferingEvent;

public class SSHConnection {
    private String user;
    private String secret;
    private List<Integer> ports = new ArrayList<>();

    public SSHConnection() {
        
    }

    public SSHConnection(SSHConnectionOfferingEvent sSHConnectionOfferingEvent) {
        this.user = sSHConnectionOfferingEvent.getUser();
        this.secret = sSHConnectionOfferingEvent.getSecret();
        this.ports = sSHConnectionOfferingEvent.getPorts();
    }

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

    public List<Integer> getPorts() {
        return this.ports;
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }
}
