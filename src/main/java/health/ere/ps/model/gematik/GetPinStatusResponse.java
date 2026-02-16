package health.ere.ps.model.gematik;

import de.gematik.ws.conn.cardservice.v821.PinStatusEnum;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;

@Data
public class GetPinStatusResponse implements Serializable {

    Status status;
    PinStatusEnum pinResultEnum;
    BigInteger leftTries;
    
    public GetPinStatusResponse(Status status, PinStatusEnum pinResultEnum, BigInteger leftTries) {
        this.status = status;
        this.pinResultEnum = pinResultEnum;
        this.leftTries = leftTries;
    }
}