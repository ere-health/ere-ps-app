package health.ere.ps.event;


import jakarta.json.JsonObject;
import jakarta.websocket.Session;


public class GetCardsEvent extends AbstractEvent {

    public GetCardsEvent(JsonObject jsonObject) {
        parseRuntimeConfig(jsonObject);
    }

    public GetCardsEvent(JsonObject object, Session replyTo, String id) {
        this(object);
        this.replyTo = replyTo;
        this.id = id;
    }
}
