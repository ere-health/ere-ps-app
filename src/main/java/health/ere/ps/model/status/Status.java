package health.ere.ps.model.status;

import java.io.Serializable;

public class Status implements Serializable {
    private volatile boolean connectorReachable;
    private volatile String connectorInformation;
    private volatile boolean idpReachable;
    private volatile String idpInformation;
    private volatile String bearerToken;
    private volatile boolean idpaccesstokenObtainable;
    private volatile String idpaccesstokenInformation;
    private volatile boolean smcbAvailable;
    private volatile String smcbInformation;
    private volatile boolean cautReadable;
    private volatile String cautInformation;
    private volatile boolean ehbaAvailable;
    private volatile String ehbaInformation;
    private volatile boolean comfortsignatureAvailable;
    private volatile String comfortsignatureInformation;
    private volatile boolean fachdienstReachable;
    private volatile String fachdienstInformation;

    public void setConnectorReachable(boolean isOK, String statusDescription) {
        this.connectorReachable = isOK;
        this.connectorInformation = statusDescription;
    }

    public void setIdpReachable(boolean isOK, String statusDescription) {
        this.idpReachable = isOK;
        this.idpInformation = statusDescription;
    }

    public void setSmcbAvailable(boolean isOK, String statusDescription) {
        this.smcbAvailable = isOK;
        this.smcbInformation = statusDescription;
    }

    public void setCautReadable(boolean isOK, String statusDescription) {
        this.cautReadable = isOK;
        this.cautInformation = statusDescription;
    }

    public void setEhbaAvailable(boolean isOK, String statusDescription) {
        this.ehbaAvailable = isOK;
        this.ehbaInformation = statusDescription;
    }

    public void setComfortsignatureAvailable(boolean isOK, String statusDescription) {
        this.comfortsignatureAvailable = isOK;
        this.comfortsignatureInformation = statusDescription;
    }

    public void setIdpaccesstokenObtainable(boolean isOK, String statusDescription) {
        this.setIdpaccesstokenObtainable(isOK, statusDescription, null);
    }

    public void setIdpaccesstokenObtainable(boolean isOK, String statusDescription, String bearerToken) {
        this.idpaccesstokenObtainable = isOK;
        this.idpaccesstokenInformation = statusDescription;
        this.bearerToken = bearerToken;
    }

    public void setFachdienstReachable(boolean isOK, String statusDescription) {
        this.fachdienstReachable = isOK;
        this.fachdienstInformation = statusDescription;
    }

    // the following GET-ers are needed in JsonbBuilder...toJson
    // in Websocket to create a JSON string from this object
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


    public boolean getIdpaccesstokenObtainable() {
        return this.idpaccesstokenObtainable;
    }

    public String getIdpaccesstokenInformation() {
        return this.idpaccesstokenInformation;
    }

    public boolean getSmcbAvailable() {
        return this.smcbAvailable;
    }

    public String getSmcbInformation() {
        return this.smcbInformation;
    }

    public boolean getCautReadable() {
        return this.cautReadable;
    }

    public String getCautInformation() {
        return this.cautInformation;
    }

    public boolean getEhbaAvailable() {
        return this.ehbaAvailable;
    }

    public String getEhbaInformation() {
        return this.ehbaInformation;
    }

    public boolean getComfortsignatureAvailable() {
        return this.comfortsignatureAvailable;
    }

    public String getComfortsignatureInformation() {
        return this.comfortsignatureInformation;
    }

    public boolean getFachdienstReachable() {
        return this.fachdienstReachable;
    }

    public String getFachdienstInformation() {
        return this.fachdienstInformation;
    }

    public String getBearerToken() {
        return this.bearerToken;
    }

}
