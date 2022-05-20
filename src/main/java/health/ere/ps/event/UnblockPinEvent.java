package health.ere.ps.event;

import java.util.Objects;

import javax.json.JsonObject;
import javax.websocket.Session;

public class UnblockPinEvent extends AbstractEvent {
    
    private String cardHandle;
    private String pinType;
    private Boolean setNewPin;


    public UnblockPinEvent() {
    }

    public UnblockPinEvent(JsonObject jsonObject, Session replyTo, String id) {
        parseRuntimeConfig(jsonObject);
        setReplyTo(replyTo);
        setId(id);
        this.cardHandle = jsonObject.getJsonObject("payload").getString("cardHandle");
        this.pinType = jsonObject.getJsonObject("payload").getString("pinType");
        this.setNewPin = jsonObject.getJsonObject("payload").getBoolean("setNewPin");
    }

    public UnblockPinEvent(String cardHandle, String pinType, Boolean setNewPin) {
        this.cardHandle = cardHandle;
        this.pinType = pinType;
        this.setNewPin = setNewPin;
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

    public Boolean isSetNewPin() {
        return this.setNewPin;
    }

    public Boolean getSetNewPin() {
        return this.setNewPin;
    }

    public void setSetNewPin(Boolean setNewPin) {
        this.setNewPin = setNewPin;
    }

    public UnblockPinEvent cardHandle(String cardHandle) {
        setCardHandle(cardHandle);
        return this;
    }

    public UnblockPinEvent pinType(String pinType) {
        setPinType(pinType);
        return this;
    }

    public UnblockPinEvent setNewPin(Boolean setNewPin) {
        setSetNewPin(setNewPin);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof UnblockPinEvent)) {
            return false;
        }
        UnblockPinEvent unblockPinEvent = (UnblockPinEvent) o;
        return Objects.equals(cardHandle, unblockPinEvent.cardHandle) && Objects.equals(pinType, unblockPinEvent.pinType) && Objects.equals(setNewPin, unblockPinEvent.setNewPin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardHandle, pinType, setNewPin);
    }

    @Override
    public String toString() {
        return "{" +
            " cardHandle='" + getCardHandle() + "'" +
            ", pinType='" + getPinType() + "'" +
            ", setNewPin='" + isSetNewPin() + "'" +
            "}";
    }

   
    
}
