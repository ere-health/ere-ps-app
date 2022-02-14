package health.ere.ps.websocket.encoder;

import java.io.Serializable;

public class Response {
    public String       type;
    public Serializable payload;
    public String       replyToMessageId;
    
    public Response(String type, Serializable payload, String replyToMessageId){
        this.type             = type;
        this.payload          = payload;
        this.replyToMessageId = replyToMessageId;
    }

    public Response(Serializable payload) {
        this.type = payload.getClass().getSimpleName();
        this.payload = payload;
    }
}