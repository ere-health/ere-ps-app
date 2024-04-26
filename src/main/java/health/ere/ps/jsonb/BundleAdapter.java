package health.ere.ps.jsonb;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.bind.adapter.JsonbAdapter;
import java.io.StringReader;

import health.ere.ps.service.fhir.FHIRService;


public class BundleAdapter implements JsonbAdapter<Bundle, JsonObject> {

    private static final FhirContext fhirContext = FHIRService.getFhirContext();
    IParser iParser = fhirContext.newJsonParser();
    
    @Override
    public JsonObject adaptToJson(Bundle b) {
        return Json.createReader(new StringReader(iParser.encodeResourceToString(b))).readObject();
    }

    @Override
    public Bundle adaptFromJson(JsonObject adapted) {
        return iParser.parseResource(Bundle.class, adapted.toString());
    }
}