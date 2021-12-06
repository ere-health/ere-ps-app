package health.ere.ps.model.gematik;

import java.math.BigInteger;

import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import de.gematik.ws.conn.connectorcommon.v5.Status;

public class ChangePinResponse {
    private Status status;
    private PinResultEnum pinResultEnum;
    private BigInteger leftTries;

    public ChangePinResponse() {
    }

    public ChangePinResponse(Status status, PinResultEnum pinResultEnum, BigInteger leftTries) {
        this.status = status;
        this.pinResultEnum = pinResultEnum;
        this.leftTries = leftTries;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public PinResultEnum getPinResultEnum() {
        return this.pinResultEnum;
    }

    public void setPinResultEnum(PinResultEnum pinResultEnum) {
        this.pinResultEnum = pinResultEnum;
    }

    public BigInteger getLeftTries() {
        return this.leftTries;
    }

    public void setLeftTries(BigInteger leftTries) {
        this.leftTries = leftTries;
    }

    public ChangePinResponse status(Status status) {
        setStatus(status);
        return this;
    }

    public ChangePinResponse pinResultEnum(PinResultEnum pinResultEnum) {
        setPinResultEnum(pinResultEnum);
        return this;
    }

    public ChangePinResponse leftTries(BigInteger leftTries) {
        setLeftTries(leftTries);
        return this;
    }

    @Override
    public String toString() {
        return "{" +
            " status='" + getStatus() + "'" +
            ", pinResultEnum='" + getPinResultEnum() + "'" +
            ", leftTries='" + getLeftTries() + "'" +
            "}";
    }
}