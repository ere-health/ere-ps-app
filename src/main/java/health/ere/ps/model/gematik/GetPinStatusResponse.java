package health.ere.ps.model.gematik;

import java.io.Serializable;
import java.math.BigInteger;

import de.gematik.ws.conn.cardservice.v8.PinStatusEnum;
import de.gematik.ws.conn.connectorcommon.v5.Status;

public class GetPinStatusResponse implements Serializable {

    Status status;
    PinStatusEnum pinResultEnum;
    BigInteger leftTries;
    
    public GetPinStatusResponse(Status status, PinStatusEnum pinResultEnum, BigInteger leftTries) {
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

    public PinStatusEnum getPinStatusEnum() {
        return this.pinResultEnum;
    }

    public void setPinStatusEnum(PinStatusEnum pinResultEnum) {
        this.pinResultEnum = pinResultEnum;
    }

    public BigInteger getLeftTries() {
        return this.leftTries;
    }

    public void setLeftTries(BigInteger leftTries) {
        this.leftTries = leftTries;
    }

}
