package health.ere.ps.jsonb;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class BundleAdapter implements JsonbAdapter<Bundle, JsonObject> {

    IParser iParser = FhirContext.forR4().newJsonParser();
    
    @Override
    public JsonObject adaptToJson(Bundle b) throws Exception {
        return Json.createReader(new StringReader(iParser.encodeResourceToString(b))).readObject();
    }

    @Override
    public Bundle adaptFromJson(JsonObject adapted) throws Exception {
        return iParser.parseResource(Bundle.class, adapted.toString());
    }
}