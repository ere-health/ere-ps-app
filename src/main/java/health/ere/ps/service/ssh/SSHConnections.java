package health.ere.ps.service.ssh;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
@XmlRootElement
public class SSHConnections {
    private Map<String, SSHConnection> sshConnection = new HashMap<>();

    public SSHConnections() {

    }

    @XmlJavaTypeAdapter(MapAdapter.class)
    public Map<String, SSHConnection> getSshConnection() {
        return sshConnection;
    }

    public void setSshConnection(Map<String, SSHConnection> map) {
        this.sshConnection = map;
    }
}
