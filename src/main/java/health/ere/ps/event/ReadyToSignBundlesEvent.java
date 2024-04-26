package health.ere.ps.event;

import java.util.ArrayList;
import java.util.List;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.websocket.Session;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.parser.IParser;

import health.ere.ps.service.fhir.FHIRService;

public class ReadyToSignBundlesEvent extends AbstractEvent {

    private static final FhirContext fhirContext = FHIRService.getFhirContext();
    public List<List<Bundle>> listOfListOfBundles = new ArrayList<>();

    public ReadyToSignBundlesEvent(JsonObject jsonObject) {
        for (JsonValue jsonValue : jsonObject.getJsonArray("payload")) {
            List<Bundle> bundles = new ArrayList<>();

            if (jsonValue instanceof JsonArray) {
                for (JsonValue singleBundle : (JsonArray) jsonValue) {
                    IParser jsonParser = fhirContext.newJsonParser();

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
