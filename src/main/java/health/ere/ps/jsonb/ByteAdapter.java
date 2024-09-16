package health.ere.ps.jsonb;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.bind.adapter.JsonbAdapter;
import java.util.Base64;


public class ByteAdapter implements JsonbAdapter<byte[], JsonObject> {

    @Override
    public JsonObject adaptToJson(byte[] bytes) {
        return Json.createObjectBuilder().add("content", Base64.getEncoder().encodeToString(bytes)).build();
    }

    @Override
    public byte[] adaptFromJson(JsonObject adapted) {
        return Base64.getDecoder().decode(adapted.getString("content"));
    }
}