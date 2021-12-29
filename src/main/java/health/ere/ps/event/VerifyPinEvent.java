package health.ere.ps.event;

import java.util.Objects;

import javax.json.JsonObject;
import javax.websocket.Session;

public class VerifyPinEvent extends AbstractEvent {
    
    private String cardHandle;

    public VerifyPinEvent() {
    }

    public VerifyPinEvent(JsonObject jsonObject, Session replyTo, String id) {
        parseRuntimeConfig(jsonObject);
        setReplyTo(replyTo);
        setId(id);
        this.cardHandle = jsonObject.getJsonObject("payload").getString("cardHandle");
    }

    public VerifyPinEvent(String cardHandle) {
        this.cardHandle = cardHandle;
    }

    public String getCardHandle() {
        return this.cardHandle;
    }

    public void setCardHandle(String cardHandle) {
        this.cardHandle = cardHandle;
    }

    public VerifyPinEvent cardHandle(String cardHandle) {
        setCardHandle(cardHandle);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof VerifyPinEvent)) {
            return false;
        }
        VerifyPinEvent changePinEvent = (VerifyPinEvent) o;
        return Objects.equals(cardHandle, changePinEvent.cardHandle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardHandle);
    }

    @Override
    public String toString() {
        return "{" +
            " cardHandle='" + getCardHandle() + "'" +
            "}";
    }
    
}
