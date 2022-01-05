package health.ere.ps.model.websocket;

import java.io.Serializable;

import health.ere.ps.websocket.Websocket;

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

    public static String buildJSON(String type, Serializable payload, String replyToMessageId){
        return Websocket.jsonbFactory.toJson(new OutgoingResponse(type,payload,replyToMessageId));
    }
}
