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

public class ReadyToSignBundlesEvent extends AbstractEvent {

    public List<List<Bundle>> listOfListOfBundles = new ArrayList<>();

    public ReadyToSignBundlesEvent(JsonObject jsonObject) {
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
    
    public ReadyToSignBundlesEvent(List<Bundle> bundles) {
        listOfListOfBundles.add(bundles);
    }

    public ReadyToSignBundlesEvent(List<Bundle> bundles, Session replyTo, String replyToMessageId) {
        this(bundles);
        this.replyTo = replyTo;
        this.replyToMessageId = replyToMessageId;
    }

    public ReadyToSignBundlesEvent(JsonObject object, Session replyTo, String messageId) {
        this(object);
        this.replyTo = replyTo;
        this.replyToMessageId = messageId;
    }
}
