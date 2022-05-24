package health.ere.ps.event;

import java.io.Serializable;

import javax.websocket.Session;

import health.ere.ps.model.gematik.GetPinStatusResponse;

public class GetPinStatusResponseEvent extends AbstractEvent implements ReplyableEvent {
    private GetPinStatusResponse getPinStatus;

    public GetPinStatusResponseEvent() {
    }

    public GetPinStatusResponseEvent(GetPinStatusResponse getPinStatus) {
        this.getPinStatus = getPinStatus;
    }

    public GetPinStatusResponseEvent(GetPinStatusResponse getPinStatus2, Session replyTo, String id) {
        setReplyTo(replyTo);
        setReplyToMessageId(id);
        this.getPinStatus = getPinStatus2;
    }

    public GetPinStatusResponse getGetPinStatusResponse() {
        return this.getPinStatus;
    }

    public void setGetPinStatusResponse(GetPinStatusResponse getPinStatus) {
        this.getPinStatus = getPinStatus;
    }

    public GetPinStatusResponseEvent getPinStatus(GetPinStatusResponse getPinStatus) {
        setGetPinStatusResponse(getPinStatus);
        return this;
    }

    @Override
    public String toString() {
        return "{" +
            " getPinStatus='" + getGetPinStatusResponse() + "'" +
            "}";
    }

    @Override
    public String getType() {
        return "GetPinStatusResponse";
    }

    @Override
    public Serializable getPayload() {
        return (Serializable) getPinStatus;
    }
    
}
