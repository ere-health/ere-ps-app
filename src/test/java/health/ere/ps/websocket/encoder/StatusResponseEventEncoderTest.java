package health.ere.ps.websocket.encoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import health.ere.ps.event.StatusResponseEvent;
import health.ere.ps.model.websocket.OutgoingResponse;

public class StatusResponseEventEncoderTest {

    @Test
    public void testEncode() {

        StatusResponseEvent statusResponseEvent = new StatusResponseEvent("Test", null, "ID");
        

        String response = OutgoingResponse.buildJSON(statusResponseEvent.getType(),
                                                    statusResponseEvent.getPayload(),
                                                    statusResponseEvent.getReplyToMessageId());
        
        assertEquals("{\"payload\":\"Test\",\"replyToMessageId\":\"ID\",\"type\":\"StatusResponse\"}", response.replaceAll("\\s+",""));
    }
    
}
