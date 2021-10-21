package health.ere.ps.event.erixa;


import javax.json.JsonObject;
import javax.websocket.Session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import health.ere.ps.event.AbstractEvent;
import health.ere.ps.model.erixa.PrescriptionTransferEntry;


public class SendToPharmacyEvent extends AbstractEvent {

    private final String document;
    private final PrescriptionTransferEntry details;


    public SendToPharmacyEvent(JsonObject payload) throws JsonProcessingException {
        document = payload.getString("document");
        details = parseDetails(payload.getJsonObject("details"));
    }

    public SendToPharmacyEvent(JsonObject payload, Session replyTo, String replyToMessageId) throws JsonProcessingException {
        this(payload);
        this.replyTo = replyTo;
        this.replyToMessageId = replyToMessageId;
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
