package health.ere.ps.event;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.ere.ps.service.fhir.FHIRService;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.websocket.Session;
import org.hl7.fhir.r4.model.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SignAndUploadBundlesEvent extends AbstractEvent {

    public List<List<Bundle>> listOfListOfBundles = new ArrayList<>();

    public String flowtype = "160";
    
    public String toKimAddress;
    
    public String noteToPharmacy;

    public Map<String,String> kimConfigMap = new HashMap<>();

    private static final FhirContext fhirContext = FHIRService.getFhirContext();


    public SignAndUploadBundlesEvent(JsonObject jsonObject) {
        parseRuntimeConfig(jsonObject);
        if(jsonObject.containsKey("flowtype")) {        	
        	setFlowtype(jsonObject.getString("flowtype"));
        }
        if(jsonObject.containsKey("toKimAddress")) {        	
        	setToKimAddress(jsonObject.getString("toKimAddress"));
        }
        if(jsonObject.containsKey("noteToPharmacy")) {        	
        	setNoteToPharmacy(jsonObject.getString("noteToPharmacy"));
        }
        if(jsonObject.containsKey("kimConfig")) {
            JsonObject kimConfig = jsonObject.getJsonObject("kimConfig");
            for(Entry<String,JsonValue> entry : kimConfig.entrySet()) {
                kimConfigMap.put(entry.getKey(), ((JsonString)entry.getValue()).getString());
            }
        }
        
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

    public SignAndUploadBundlesEvent(JsonObject jsonObject, Session replyTo, String id) {
        this(jsonObject);
        this.replyTo = replyTo;
        this.id = id;
    }

    public SignAndUploadBundlesEvent(List<Bundle> bundles) {
        listOfListOfBundles.add(bundles);
    }

    public SignAndUploadBundlesEvent(Bundle[] bundles, JsonObject jsonObject, Session senderSession, String id) {
        parseRuntimeConfig(jsonObject);

        //todo: here some keys from above are ignored - refactor & include (probably own process json method?)
        
        if (jsonObject.containsKey("flowtype")) {
            setFlowtype(jsonObject.getString("flowtype"));
        }
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

	public String getNoteToPharmacy() {
		return noteToPharmacy;
	}

	public void setNoteToPharmacy(String noteToPharmacy) {
		this.noteToPharmacy = noteToPharmacy;
	}

    public Map<String,String> getKimConfigMap() {
        return this.kimConfigMap;
    }

    public void setKimConfigMap(Map<String,String> kimConfigMap) {
        this.kimConfigMap = kimConfigMap;
    }
}
