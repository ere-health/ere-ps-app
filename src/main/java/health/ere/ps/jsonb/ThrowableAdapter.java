package health.ere.ps.jsonb;

import java.io.PrintWriter;
import java.io.StringWriter;

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
        .add("message", e.getMessage())
        .add("stacktrace", sw.toString())
        .build();
    }

    @Override
    public Throwable adaptFromJson(JsonObject adapted) {
        // TODO: do not loose all these information
        return new Exception(adapted.getString("message"));
    }
}