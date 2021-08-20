package health.ere.ps.websocket;

import javax.json.JsonObject;

public interface MessageProcessor {
    public boolean canProcess(String type);
    public void process(JsonObject message);
}
