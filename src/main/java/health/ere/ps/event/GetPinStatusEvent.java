package health.ere.ps.event;

import java.util.Objects;

import jakarta.json.JsonObject;
import jakarta.websocket.Session;

public class GetPinStatusEvent extends AbstractEvent {
    
    private String cardHandle;
    private String pinType;

    public GetPinStatusEvent() {
    }

    public GetPinStatusEvent(JsonObject jsonObject, Session replyTo, String id) {
        parseRuntimeConfig(jsonObject);
        setReplyTo(replyTo);
        setId(id);
        this.cardHandle = jsonObject.getJsonObject("payload").getString("cardHandle");
        this.pinType = jsonObject.getJsonObject("payload").getString("pinType");
    }

    public GetPinStatusEvent(String cardHandle, String pinType) {
        this.cardHandle = cardHandle;
        this.pinType = pinType;
    }

    public String getCardHandle() {
        return this.cardHandle;
    }

    public void setCardHandle(String cardHandle) {
        this.cardHandle = cardHandle;
    }

    public String getPinType() {
        return this.pinType;
    }

    public void setPinType(String pinType) {
        this.pinType = pinType;
    }

    public GetPinStatusEvent cardHandle(String cardHandle) {
        setCardHandle(cardHandle);
        return this;
    }

    public GetPinStatusEvent pinType(String pinType) {
        setPinType(pinType);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof GetPinStatusEvent)) {
            return false;
        }
        GetPinStatusEvent getPinStatusEvent = (GetPinStatusEvent) o;
        return Objects.equals(cardHandle, getPinStatusEvent.cardHandle) && Objects.equals(pinType, getPinStatusEvent.pinType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardHandle, pinType);
    }

    @Override
    public String toString() {
        return "{" +
            " cardHandle='" + getCardHandle() + "'" +
            ", pinType='" + getPinType() + "'" +
            "}";
    }
    
}
