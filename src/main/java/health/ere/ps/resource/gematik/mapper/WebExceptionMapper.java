package health.ere.ps.resource.gematik.mapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import health.ere.ps.jsonb.ThrowableAdapter;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;


public class WebExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger log = Logger.getLogger(ExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        JsonObjectBuilder builder = Json.createObjectBuilder()
            .add("class", e.getClass().getName())
            .add("message", e.getMessage() != null ? e.getMessage() : "null")
            .add("errorCode", ThrowableAdapter.extractErrorCode(e))
            .add("stacktrace", sw.toString());
        try {
            if(e instanceof WebApplicationException) {
                WebApplicationException wae = (WebApplicationException) e;
                builder.add("response", wae.getResponse().getEntity().toString());
            }
        } catch(Exception ex) {
            log.log(Level.SEVERE, "Error during response generation", ex);
        }
        return Response.serverError().entity(builder.build()).build();
    }

}