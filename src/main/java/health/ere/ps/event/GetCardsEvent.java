package health.ere.ps.event;


import javax.json.JsonObject;
import javax.websocket.Session;


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
