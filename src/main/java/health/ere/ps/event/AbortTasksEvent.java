package health.ere.ps.event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class AbortTasksEvent {

    private List<AbortTaskEntry> tasks = new ArrayList<>();

    public AbortTasksEvent() {

    }

    public AbortTasksEvent(JsonArray abortTaskEntries) {
        this.tasks = abortTaskEntries.stream().filter(o -> o instanceof JsonObject)
                .map(o -> new AbortTaskEntry((JsonObject) o)).collect(Collectors.toList());
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
