package health.ere.ps.event;


import javax.json.JsonObject;
import javax.websocket.Session;


public class VZDSearchEvent extends AbstractEvent {

    private String search;
    
    public VZDSearchEvent(JsonObject jsonObject) {
        parseRuntimeConfig(jsonObject);
        this.search = jsonObject.getString("search");
    }

    public VZDSearchEvent(JsonObject object, Session replyTo, String id) {
        this(object);
        this.replyTo = replyTo;
        this.id = id;
    }

    public String getSearch() {
        return this.search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
