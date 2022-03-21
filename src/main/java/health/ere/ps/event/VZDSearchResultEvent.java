package health.ere.ps.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.websocket.Session;

public class VZDSearchResultEvent extends AbstractEvent implements ReplyableEvent {

    private List<Map<String, Object>> results = new ArrayList<>();

    
    public VZDSearchResultEvent(List<Map<String,Object>> results) {
        this.results = results;
    }
    
    public VZDSearchResultEvent(List<Map<String,Object>> results, Session replyTo, String replyToMessageId) {
        this(results);
        this.replyTo = replyTo;
        this.replyToMessageId = replyToMessageId; 
    }
    public List<Map<String,Object>> getResults() {
        return this.results;
    }

    public void setResults(List<Map<String,Object>> results) {
        this.results = results;
    }

    @Override
    public String getType() {
        return "VZDSearchResult";
    }

    @Override
    public Serializable getPayload() {
        return (Serializable) results;
    }

}