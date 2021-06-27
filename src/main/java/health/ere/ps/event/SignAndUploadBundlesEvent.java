package health.ere.ps.event;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.hl7.fhir.r4.model.Bundle;
import org.jboss.logging.Logger;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.ere.ps.websocket.Websocket;

public class SignAndUploadBundlesEvent {

    public List<List<Bundle>> listOfListOfBundles = new ArrayList<>();
    public String bearerToken;

    private static Logger log = Logger.getLogger(SignAndUploadBundlesEvent.class.getName());

    public SignAndUploadBundlesEvent() {
        
    }

    public SignAndUploadBundlesEvent(JsonObject jsonObject) {
        log.info("Inside SignAndUploadBundlesEvent object - about to get bearer token.");

        bearerToken = jsonObject.getString("bearerToken", "");

        log.info(String.format("Inside SignAndUploadBundlesEvent object - got bearer token: %s",
                bearerToken));
        log.info("Inside SignAndUploadBundlesEvent object - about to process payload");

        int i = 0, j = 0;

        for(JsonValue jsonValue : jsonObject.getJsonArray("payload")) {
            log.info("Inside SignAndUploadBundlesEvent object - about to process payload array");
            List<Bundle> bundles = new ArrayList<>();

            if(jsonValue instanceof JsonArray) {
                log.info("Inside SignAndUploadBundlesEvent object - payload array present");
                for(JsonValue singleBundle : (JsonArray) jsonValue) {
                    log.info(String.format("Inside SignAndUploadBundlesEvent object - processing " +
                            "payload array bundle element[%d]: %s", j++, singleBundle.toString()));
                    log.info("Inside SignAndUploadBundlesEvent object - about to parse bundle " +
                            "json to Bundle object");
                    IParser jsonParser = FhirContext.forR4().newJsonParser();

                    try {
                        Bundle bundle = jsonParser.parseResource(Bundle.class, singleBundle.toString());

                        log.info("Inside SignAndUploadBundlesEvent object - Parsed bundle " +
                                "json to Bundle object");

                        bundles.add(bundle);
                    } catch (Throwable e) {
                        log.error("Inside SignAndUploadBundlesEvent object - Exception " +
                                "occurred while parsing bundle json to Bundle object", e);
                    }
                }
            }

            listOfListOfBundles.add(bundles);

            log.info(String.format("Inside SignAndUploadBundlesEvent object - added %d" +
                    " extracted bundles list as element to bundles collector list", ++i));
        }
    }

}
