package health.ere.ps.websocket.encoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;

import jakarta.websocket.EncodeException;

import org.junit.jupiter.api.Test;

import health.ere.ps.event.ReplyableEvent;
import health.ere.ps.event.StatusResponseEvent;

public class ResponseEventEncoderTest {

    @Test
    public void testEncode() {
        ResponsePayload payload = new ResponsePayload();
        ReplyableEvent statusResponseEvent = new StatusResponseEvent(payload, null, "ReplyToID");
        ResponseEventEncoder responseEventEncoder = new ResponseEventEncoder();

        String response;
        try {
            response = responseEventEncoder.encode(statusResponseEvent);
        } catch (EncodeException e) {
            response = e.getMessage();
        }
        
        assertEquals("{\"payload\":{\"attribute\":\"TestPayload\"},\"replyToMessageId\":\"ReplyToID\",\"type\":\"StatusResponse\"}", 
                     response.replaceAll("\\s+",""));
    }

    public class ResponsePayload implements Serializable{
        String attribute = "TestPayload";
        public String getAttribute(){
            return this.attribute;
        }
    }
    
}
