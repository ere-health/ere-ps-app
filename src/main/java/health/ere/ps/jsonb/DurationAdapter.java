package health.ere.ps.jsonb;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.bind.adapter.JsonbAdapter;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

public class DurationAdapter implements JsonbAdapter<Duration, JsonString> {


    @Override
    public JsonString adaptToJson(Duration obj) throws Exception {
        return Json.createValue(obj.toString());
    }

    @Override
    public Duration adaptFromJson(JsonString obj) throws Exception {
        return DatatypeFactory.newDefaultInstance().newDuration(obj.getString());
    }

}
