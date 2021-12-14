package health.ere.ps.model.status;

import java.util.Objects;

public class Status {
    private boolean connectorReachable;
    private String informationConnectorReachable;
    private boolean idpReachable;
    private String informationIdpReachable;
    // ...

    public Status() {
    }

    public Status(boolean connectorReachable, String informationConnectorReachable, boolean idpReachable, String informationIdpReachable) {
        this.connectorReachable = connectorReachable;
        this.informationConnectorReachable = informationConnectorReachable;
        this.idpReachable = idpReachable;
        this.informationIdpReachable = informationIdpReachable;
    }

    public boolean isConnectorReachable() {
        return this.connectorReachable;
    }

    public boolean getConnectorReachable() {
        return this.connectorReachable;
    }

    public void setConnectorReachable(boolean connectorReachable) {
        this.connectorReachable = connectorReachable;
    }

    public String getInformationConnectorReachable() {
        return this.informationConnectorReachable;
    }

    public void setInformationConnectorReachable(String informationConnectorReachable) {
        this.informationConnectorReachable = informationConnectorReachable;
    }

    public boolean isIdpReachable() {
        return this.idpReachable;
    }

    public boolean getIdpReachable() {
        return this.idpReachable;
    }

    public void setIdpReachable(boolean idpReachable) {
        this.idpReachable = idpReachable;
    }

    public String getInformationIdpReachable() {
        return this.informationIdpReachable;
    }

    public void setInformationIdpReachable(String informationIdpReachable) {
        this.informationIdpReachable = informationIdpReachable;
    }

    public Status connectorReachable(boolean connectorReachable) {
        setConnectorReachable(connectorReachable);
        return this;
    }

    public Status informationConnectorReachable(String informationConnectorReachable) {
        setInformationConnectorReachable(informationConnectorReachable);
        return this;
    }

    public Status idpReachable(boolean idpReachable) {
        setIdpReachable(idpReachable);
        return this;
    }

    public Status informationIdpReachable(String informationIdpReachable) {
        setInformationIdpReachable(informationIdpReachable);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Status)) {
            return false;
        }
        Status status = (Status) o;
        return connectorReachable == status.connectorReachable && Objects.equals(informationConnectorReachable, status.informationConnectorReachable) && idpReachable == status.idpReachable && Objects.equals(informationIdpReachable, status.informationIdpReachable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectorReachable, informationConnectorReachable, idpReachable, informationIdpReachable);
    }

    @Override
    public String toString() {
        return "{" +
            " connectorReachable='" + isConnectorReachable() + "'" +
            ", informationConnectorReachable='" + getInformationConnectorReachable() + "'" +
            ", idpReachable='" + isIdpReachable() + "'" +
            ", informationIdpReachable='" + getInformationIdpReachable() + "'" +
            "}";
    }

}
