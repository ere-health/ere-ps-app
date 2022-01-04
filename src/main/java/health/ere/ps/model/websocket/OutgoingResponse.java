package health.ere.ps.model.websocket;

import java.io.Serializable;
import javax.json.bind.Jsonb;

public class OutgoingResponse {
    private String type;
    private Serializable payload;
    private String replyToMessageId;
    
    public OutgoingResponse(String type, Serializable payload, String replyToMessageId){
        this.type = type;
        this.payload = payload;
        this.replyToMessageId = replyToMessageId;
    }

    public String getType(){
        return this.type;
    }

    public Serializable getPayload(){
        return this.payload;
    }

    public String getReplyToMessageId(){
        return this.replyToMessageId;
    }

    public static String buildJSON(Jsonb jsonbFactory, String type, Serializable payload, String replyToMessageId){
        return jsonbFactory.toJson(new OutgoingResponse(type,payload,replyToMessageId));
    }
}
