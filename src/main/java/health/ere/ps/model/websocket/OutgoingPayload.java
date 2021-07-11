package health.ere.ps.model.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

public class OutgoingPayload<T extends Serializable> implements Serializable {
    private String type;
    private T payload;

    public OutgoingPayload(T payload) {
        this.payload = payload;
    }

    public OutgoingPayload() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{}";

        if(payload == null) {
            return json;
        }

        try {
            json = mapper.writeValueAsString( this );
        } catch (JsonProcessingException e) {

        }

        return json;
    }
}
