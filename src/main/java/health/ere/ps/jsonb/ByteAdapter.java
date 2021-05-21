package health.ere.ps.jsonb;

import java.util.Base64;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;


public class ByteAdapter implements JsonbAdapter<byte[], JsonObject> {

    
    @Override
    public JsonObject adaptToJson(byte[] bytes) throws Exception {
        return Json.createObjectBuilder().add("content", Base64.getEncoder().encodeToString(bytes)).build();
    }

    @Override
    public byte[] adaptFromJson(JsonObject adapted) throws Exception {
        return Base64.getDecoder().decode(adapted.getString("content"));
    }
}