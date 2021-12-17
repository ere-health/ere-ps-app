package health.ere.ps.model.status;

public class Status {
    private boolean connectorReachable;
    private String informationConnectorReachable;
    // private boolean idpReachable;
    // private String informationIdpReachable;


    public void setConnectorReachable(boolean isReachable, String statusDescription) {
        this.connectorReachable = isReachable;
        this.informationConnectorReachable = statusDescription;
    }

    public boolean getConnectorReachable() {
        return this.connectorReachable;
    }
    
    public String getInformationConnectorReachable() {
        return this.informationConnectorReachable;
    }

}
