package health.ere.ps.event;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.websocket.Session;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class SignAndUploadBundlesEvent extends AbstractEvent {

    public List<List<Bundle>> listOfListOfBundles = new ArrayList<>();

    public SignAndUploadBundlesEvent(JsonObject jsonObject) {
        parseRuntimeConfig(jsonObject);
        for (JsonValue jsonValue : jsonObject.getJsonArray("payload")) {
            List<Bundle> bundles = new ArrayList<>();

            if (jsonValue instanceof JsonArray) {
                for (JsonValue singleBundle : (JsonArray) jsonValue) {
                    IParser jsonParser = FhirContext.forR4().newJsonParser();

                    Bundle bundle = jsonParser.parseResource(Bundle.class, singleBundle.toString());
                    bundles.add(bundle);
                }
            }
            listOfListOfBundles.add(bundles);
        }
    }

    public SignAndUploadBundlesEvent(JsonObject jsonObject, Session replyTo, String id) {
        this(jsonObject);
        this.replyTo = replyTo;
        this.id = id;
    }

    public SignAndUploadBundlesEvent(List<Bundle> bundles) {
        listOfListOfBundles.add(bundles);
    }
}
