package health.ere.ps.event;

import java.util.Objects;

import javax.json.JsonObject;
import javax.websocket.Session;

public class ChangePinEvent extends AbstractEvent {
    
    private String cardHandle;
    private String pinType;

    public ChangePinEvent() {
    }

    public ChangePinEvent(JsonObject jsonObject, Session replyTo, String id) {
        parseRuntimeConfig(jsonObject);
        setReplyTo(replyTo);
        setId(id);
        this.cardHandle = jsonObject.getJsonObject("payload").getString("cardHandle");
        this.pinType = jsonObject.getJsonObject("payload").getString("pinType");
    }

    public ChangePinEvent(String cardHandle, String pinType) {
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

    public ChangePinEvent cardHandle(String cardHandle) {
        setCardHandle(cardHandle);
        return this;
    }

    public ChangePinEvent pinType(String pinType) {
        setPinType(pinType);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ChangePinEvent)) {
            return false;
        }
        ChangePinEvent changePinEvent = (ChangePinEvent) o;
        return Objects.equals(cardHandle, changePinEvent.cardHandle) && Objects.equals(pinType, changePinEvent.pinType);
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
