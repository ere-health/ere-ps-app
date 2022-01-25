package health.ere.ps.event;

public class SSHClientPortForwardEvent {

    private String host = "";

    private Integer port = 0;

    public SSHClientPortForwardEvent(String host, Integer port) {
        this.host = host;
        this.port = port;
    }


    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getPort() {
        return port;
    }
 
    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }   
}
