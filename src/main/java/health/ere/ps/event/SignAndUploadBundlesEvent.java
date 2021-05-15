package health.ere.ps.event;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonObject;

import org.hl7.fhir.r4.model.Bundle;

public class SignAndUploadBundlesEvent {
    public List<List<Bundle>> listOfListOfBundles = new ArrayList<>();
    public String bearerToken;

    public SignAndUploadBundlesEvent() {
        
    }

    public SignAndUploadBundlesEvent(JsonObject jsonObject) {

    }

}
