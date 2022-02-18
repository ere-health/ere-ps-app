package health.ere.ps.event;

import java.util.ArrayList;
import java.util.Arrays;
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

    public String flowtype = "160";
    
    public String toKimAddress;
    
    public String noteForPharmacy;

    public SignAndUploadBundlesEvent(JsonObject jsonObject) {
        parseRuntimeConfig(jsonObject);
        if(jsonObject.containsKey("flowtype")) {        	
        	setFlowtype(jsonObject.getString("flowtype"));
        }
        if(jsonObject.containsKey("toKimAddress")) {        	
        	setToKimAddress(jsonObject.getString("toKimAddress"));
        }
        if(jsonObject.containsKey("noteForPharmacy")) {        	
        	setNoteForPharmacy(jsonObject.getString("noteForPharmacy"));
        }
        
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

    public SignAndUploadBundlesEvent(Bundle[] bundles, Session senderSession, String id) {
        this.replyTo = senderSession;
        this.id = id;
        listOfListOfBundles.add(Arrays.asList(bundles));
    }

    public List<List<Bundle>> getListOfListOfBundles() {
        return this.listOfListOfBundles;
    }

    public void setListOfListOfBundles(List<List<Bundle>> listOfListOfBundles) {
        this.listOfListOfBundles = listOfListOfBundles;
    }

    public String getFlowtype() {
        return this.flowtype;
    }

    public void setFlowtype(String flowtype) {
        this.flowtype = flowtype;
    }

	public String getToKimAddress() {
		return toKimAddress;
	}

	public void setToKimAddress(String toKimAddress) {
		this.toKimAddress = toKimAddress;
	}

	public String getNoteForPharmacy() {
		return noteForPharmacy;
	}

	public void setNoteForPharmacy(String noteForPharmacy) {
		this.noteForPharmacy = noteForPharmacy;
	}
}
