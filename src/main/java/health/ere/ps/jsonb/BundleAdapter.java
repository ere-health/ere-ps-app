package health.ere.ps.jsonb;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;
import java.io.StringReader;

public class BundleAdapter implements JsonbAdapter<Bundle, JsonObject> {


    IParser iParser = FhirContext.forR4().newJsonParser();
    
    @Override
    public JsonObject adaptToJson(Bundle b) {
        return Json.createReader(new StringReader(iParser.encodeResourceToString(b))).readObject();
    }

    @Override
    public Bundle adaptFromJson(JsonObject adapted) {
        return iParser.parseResource(Bundle.class, adapted.toString());
    }
}