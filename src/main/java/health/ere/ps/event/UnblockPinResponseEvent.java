package health.ere.ps.event;

import java.util.Objects;

import javax.websocket.Session;

import health.ere.ps.model.gematik.UnblockPinResponse;

public class UnblockPinResponseEvent extends AbstractEvent {
    
    private UnblockPinResponse unblockPinResponse;

    public UnblockPinResponseEvent() {
    }

    public UnblockPinResponseEvent(UnblockPinResponse unblockPinResponse, Session replyTo, String id) {
        setReplyTo(replyTo);
        setReplyToMessageId(id);
        this.unblockPinResponse = unblockPinResponse;
    }

    public UnblockPinResponse getUnblockPinResponse() {
        return this.unblockPinResponse;
    }

    public void setUnblockPinResponse(UnblockPinResponse unblockPinResponse) {
        this.unblockPinResponse = unblockPinResponse;
    }

    public UnblockPinResponseEvent unblockPinResponse(UnblockPinResponse unblockPinResponse) {
        setUnblockPinResponse(unblockPinResponse);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof UnblockPinResponseEvent)) {
            return false;
        }
        UnblockPinResponseEvent unblockPinResponseEvent = (UnblockPinResponseEvent) o;
        return Objects.equals(unblockPinResponse, unblockPinResponseEvent.unblockPinResponse);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(unblockPinResponse);
    }

    @Override
    public String toString() {
        return "{" +
            " unblockPinResponse='" + getUnblockPinResponse() + "'" +
            "}";
    }

    
}