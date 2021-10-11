package health.ere.ps.event;

import javax.json.JsonObject;
import javax.websocket.Session;

public class GetSignatureModeEvent extends AbstractEvent {

    public GetSignatureModeEvent(JsonObject object) {
    }

    
    public GetSignatureModeEvent(JsonObject object, Session replyTo, String id) {
        this.replyTo = replyTo;
        this.id = id;
    }

    public GetSignatureModeEvent(Session replyTo, String id) {
        this(null, replyTo, id);
    }
    
}
