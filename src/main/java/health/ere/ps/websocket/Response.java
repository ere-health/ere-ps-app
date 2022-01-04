package health.ere.ps.websocket;

public class Response {
    private String type;
    private Object payload;
    private String replyToMessageId;
    
    public Response(String type, Object payload, String replyToMessageId){
        this.type = type;
        this.payload = payload;
        this.replyToMessageId = replyToMessageId;
    }

    public String getType(){
        return this.type;
    }

    public Object getPayload(){
        return this.payload;
    }

    public String getReplyToMessageId(){
        return this.replyToMessageId;
    }
}
