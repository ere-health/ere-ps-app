package health.ere.ps.event;

import java.io.Serializable;

import jakarta.websocket.Session;

import health.ere.ps.model.gematik.ChangePinResponse;

public class ChangePinResponseEvent extends AbstractEvent implements ReplyableEvent {
    private ChangePinResponse changePinResponse;

    public ChangePinResponseEvent() {
    }

    public ChangePinResponseEvent(ChangePinResponse changePinResponse) {
        this.changePinResponse = changePinResponse;
    }

    public ChangePinResponseEvent(ChangePinResponse changePinResponse2, Session replyTo, String id) {
        setReplyTo(replyTo);
        setReplyToMessageId(id);
        this.changePinResponse = changePinResponse2;
    }

    public ChangePinResponse getChangePinResponse() {
        return this.changePinResponse;
    }

    public void setChangePinResponse(ChangePinResponse changePinResponse) {
        this.changePinResponse = changePinResponse;
    }

    public ChangePinResponseEvent changePinResponse(ChangePinResponse changePinResponse) {
        setChangePinResponse(changePinResponse);
        return this;
    }

    @Override
    public String toString() {
        return "{" +
            " changePinResponse='" + getChangePinResponse() + "'" +
            "}";
    }

    @Override
    public String getType() {
        return "ChangePinResponse";
    }

    @Override
    public Serializable getPayload() {
        return (Serializable) changePinResponse;
    }
    
}
