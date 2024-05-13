package health.ere.ps.event;

import java.io.Serializable;
import java.util.Objects;

import jakarta.websocket.Session;

import health.ere.ps.model.gematik.VerifyPinResponse;

public class VerifyPinResponseEvent extends AbstractEvent implements ReplyableEvent {
    
    private VerifyPinResponse verifyPinResponse;

    public VerifyPinResponseEvent() {
    }

    public VerifyPinResponseEvent(VerifyPinResponse verifyPinResponse, Session replyTo, String id) {
        setReplyTo(replyTo);
        setReplyToMessageId(id);
        this.verifyPinResponse = verifyPinResponse;
    }

    public VerifyPinResponse getVerifyPinResponse() {
        return this.verifyPinResponse;
    }

    public void setVerifyPinResponse(VerifyPinResponse verifyPinResponse) {
        this.verifyPinResponse = verifyPinResponse;
    }

    public VerifyPinResponseEvent verifyPinResponse(VerifyPinResponse verifyPinResponse) {
        setVerifyPinResponse(verifyPinResponse);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof VerifyPinResponseEvent)) {
            return false;
        }
        VerifyPinResponseEvent verifyPinResponseEvent = (VerifyPinResponseEvent) o;
        return Objects.equals(verifyPinResponse, verifyPinResponseEvent.verifyPinResponse);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(verifyPinResponse);
    }

    @Override
    public String toString() {
        return "{" +
            " verifyPinResponse='" + getVerifyPinResponse() + "'" +
            "}";
    }

    @Override
    public Serializable getPayload() {
        return (Serializable) verifyPinResponse;
    }

    @Override
    public String getType() {
        return "VerifyPinResponse";
    }

    
}
