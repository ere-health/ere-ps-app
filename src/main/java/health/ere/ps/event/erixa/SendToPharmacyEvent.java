package health.ere.ps.event.erixa;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import health.ere.ps.model.erixa.PrescriptionTransferEntry;

import javax.json.JsonObject;


public class SendToPharmacyEvent {

    private final String document;
    private final PrescriptionTransferEntry details;


    public SendToPharmacyEvent(JsonObject payload) throws JsonProcessingException {
        document = payload.getString("document");
        details = parseDetails(payload.getJsonObject("details"));
    }

    private PrescriptionTransferEntry parseDetails(JsonObject jsonObject) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonObject.toString(), PrescriptionTransferEntry.class);
    }

    public String getDocument() {
        return document;
    }

    public PrescriptionTransferEntry getDetails() {
        return details;
    }
}
