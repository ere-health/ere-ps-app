package health.ere.ps.jsonb;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.ws.rs.WebApplicationException;


public class ThrowableAdapter implements JsonbAdapter<Throwable, JsonObject> {

    private static final Logger log = Logger.getLogger(ThrowableAdapter.class.getName());

    @Override
    public JsonObject adaptToJson(Throwable e) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        JsonObjectBuilder builder = Json.createObjectBuilder()
            .add("class", e.getClass().getName())
            .add("message", e.getMessage() != null ? e.getMessage() : "null")
            .add("errorCode", extractErrorCode(e))
            .add("stacktrace", sw.toString());
        try {
            if(e instanceof WebApplicationException) {
                WebApplicationException wae = (WebApplicationException) e;
                builder.add("response", wae.getResponse().getEntity().toString());
            }
        } catch(Exception ex) {
            log.log(Level.SEVERE, "Error during response generation", ex);
        }
        return builder.build();
    }

    public static BigInteger extractErrorCode(Throwable e) {
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