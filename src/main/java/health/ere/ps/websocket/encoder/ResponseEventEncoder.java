package health.ere.ps.websocket.encoder;

import java.io.Serializable;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

import health.ere.ps.event.ReplyableEvent;
import health.ere.ps.websocket.Websocket;

public class ResponseEventEncoder implements Encoder.Text<ReplyableEvent> {

    @Override
    public void init(EndpointConfig config) {
        
    }

    @Override
    public void destroy() {
 
    }

    @Override
    public String encode(ReplyableEvent replyableEvent) throws EncodeException {
        Response response = new Response(replyableEvent.getType(),
                                         replyableEvent.getPayload(),
                                         replyableEvent.getReplyToMessageId());
        return Websocket.jsonbFactory.toJson(response);
    }

    public class Response {
        public String       type;
        public Serializable payload;
        public String       replyToMessageId;
        
        public Response(String type, Serializable payload, String replyToMessageId){
            this.type             = type;
            this.payload          = payload;
            this.replyToMessageId = replyToMessageId;
        }
    }
}
