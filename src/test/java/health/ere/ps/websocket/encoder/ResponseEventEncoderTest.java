package health.ere.ps.websocket.encoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.websocket.EncodeException;

import org.junit.jupiter.api.Test;

import health.ere.ps.event.ReplyableEvent;
import health.ere.ps.event.StatusResponseEvent;

public class ResponseEventEncoderTest {

    @Test
    public void testEncode() {

        ReplyableEvent statusResponseEvent = new StatusResponseEvent("TestPayload", null, "ReplyToID");
        ResponseEventEncoder responseEventEncoder = new ResponseEventEncoder();

        String response;
        try {
            response = responseEventEncoder.encode(statusResponseEvent);
        } catch (EncodeException e) {
            response = e.getMessage();
        }
        
        assertEquals("{\"payload\":\"TestPayload\",\"replyToMessageId\":\"ReplyToID\",\"type\":\"StatusResponse\"}", 
                     response.replaceAll("\\s+",""));
    }
    
}
