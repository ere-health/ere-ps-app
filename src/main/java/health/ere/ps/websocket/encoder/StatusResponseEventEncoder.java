package health.ere.ps.websocket.encoder;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import health.ere.ps.event.StatusResponseEvent;
import health.ere.ps.model.websocket.OutgoingResponse;

public class StatusResponseEventEncoder implements Encoder.Text<StatusResponseEvent> {

    @Override
    public void init(EndpointConfig config) {
        
    }

    @Override
    public void destroy() {
 
    }

    @Override
    public String encode(StatusResponseEvent statusResponseEvent) throws EncodeException {
        String response = OutgoingResponse.buildJSON(statusResponseEvent.getType(),
                                                    statusResponseEvent.getPayload(),
                                                    statusResponseEvent.getReplyToMessageId());
        
        return response;
    }
    
}
