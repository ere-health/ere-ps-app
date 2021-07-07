package health.ere.ps.event;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;

public class SignAndUploadBundlesEvent {

    private final FhirContext ctx = FhirContext.forR4();
    public List<List<Bundle>> listOfListOfBundles = new ArrayList<>();
    public String bearerToken;

    public SignAndUploadBundlesEvent(JsonObject jsonObject) {
        bearerToken = jsonObject.getString("bearerToken", "");

        for (JsonValue jsonValue : jsonObject.getJsonArray("payload")) {
            List<Bundle> bundles = new ArrayList<>();

            if (jsonValue instanceof JsonArray) {
                for (JsonValue singleBundle : (JsonArray) jsonValue) {
                    IParser jsonParser = ctx.newJsonParser();

                    Bundle bundle = jsonParser.parseResource(Bundle.class, singleBundle.toString());
                    bundles.add(bundle);
                }
            }
            listOfListOfBundles.add(bundles);
        }
    }
}
