package health.ere.ps.model.status;

public class Status {
    private boolean connectorReachable;
    private String connectorInformation;
    private boolean idpReachable;
    private String idpInformation;

    public void setConnectorReachable(boolean isReachable, String statusDescription) {
        this.connectorReachable = isReachable;
        this.connectorInformation = statusDescription;
    }

    public void setIdpReachable(boolean isReachable, String statusDescription) {
        this.idpReachable = isReachable;
        this.idpInformation = statusDescription;
    }

    // the following GET-ers are needed in JsonbBuilder...toJson
    // in Websocket.generateJson(StatusResponseEvent ...)
    // to create JSON string from a Status object
    public boolean getConnectorReachable() {
        return this.connectorReachable;
    }

    public String getConnectorInformation() {
        return this.connectorInformation;
    }

    public boolean getIdpReachable() {
        return this.idpReachable;
    }

    public String getIdpInformation() {
        return this.idpInformation;
    }

}
