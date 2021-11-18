package health.ere.ps.jsonb;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;


public class ThrowableAdapter implements JsonbAdapter<Throwable, JsonObject> {

    @Override
    public JsonObject adaptToJson(Throwable e) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        return Json.createObjectBuilder()
        .add("class", e.getClass().getName())
        .add("message", e.getMessage() != null ? e.getMessage() : "null")
        .add("errorCode", extractErrorCode(e))
        .add("stacktrace", sw.toString())
        .build();
    }

    private BigInteger extractErrorCode(Throwable e) {
        BigInteger errorCode = BigInteger.ZERO;
        do {
            if(e instanceof de.gematik.ws.conn.authsignatureservice.wsdl.v7.FaultMessage) {
                de.gematik.ws.conn.authsignatureservice.wsdl.v7.FaultMessage faultMessage = ((de.gematik.ws.conn.authsignatureservice.wsdl.v7.FaultMessage) e);
                if(faultMessage.getFaultInfo() != null && faultMessage.getFaultInfo().getTrace().size() > 0) {
                    errorCode = faultMessage.getFaultInfo().getTrace().get(0).getCode();
                }
            } else if(e instanceof de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage) {
                de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage faultMessage = ((de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage) e);
                if(faultMessage.getFaultInfo() != null && faultMessage.getFaultInfo().getTrace().size() > 0) {
                    errorCode = faultMessage.getFaultInfo().getTrace().get(0).getCode();
                }
            } else if(e instanceof de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage) {
                de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage faultMessage = ((de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage) e);
                if(faultMessage.getFaultInfo() != null && faultMessage.getFaultInfo().getTrace().size() > 0) {
                    errorCode = faultMessage.getFaultInfo().getTrace().get(0).getCode();
                }
            } else if(e instanceof de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage) {
                de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage faultMessage = ((de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage) e);
                if(faultMessage.getFaultInfo() != null && faultMessage.getFaultInfo().getTrace().size() > 0) {
                    errorCode = faultMessage.getFaultInfo().getTrace().get(0).getCode();
                }
            } else if(e instanceof de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage) {
                de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage faultMessage = ((de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage) e);
                if(faultMessage.getFaultInfo() != null && faultMessage.getFaultInfo().getTrace().size() > 0) {
                    errorCode = faultMessage.getFaultInfo().getTrace().get(0).getCode();
                }
            }
        } while((e = e.getCause()) != null);
        return errorCode;
    }

    @Override
    public Throwable adaptFromJson(JsonObject adapted) {
        return new Exception(adapted.getString("message"));
    }
}