package health.ere.ps.event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.websocket.Session;

public class AbortTasksEvent extends AbstractEvent {

    private List<AbortTaskEntry> tasks = new ArrayList<>();

    public AbortTasksEvent() {

    }

    public AbortTasksEvent(JsonArray abortTaskEntries) {
        if(abortTaskEntries != null) {
            this.tasks = abortTaskEntries.stream().filter(o -> o instanceof JsonObject)
                .map(o -> new AbortTaskEntry((JsonObject) o)).collect(Collectors.toList());
        }
    }

    public AbortTasksEvent(JsonObject object, Session replyTo, String id) {
        this(object.getJsonArray("payload"));
        parseRuntimeConfig(object);
        this.replyTo = replyTo;
        this.id = id;
    }

    public List<AbortTaskEntry> getTasks() {
        return this.tasks;
    }

    public void setTasks(List<AbortTaskEntry> tasks) {
        this.tasks = tasks;
    }

    public void addAbortTaskEntry(AbortTaskEntry abortTaskEntry) {
        this.tasks.add(abortTaskEntry);
    }

}
