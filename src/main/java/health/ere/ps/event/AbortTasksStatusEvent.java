package health.ere.ps.event;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.Session;

public class AbortTasksStatusEvent extends AbstractEvent {

    private List<AbortTaskStatus> tasks = new ArrayList<>();

    
    public AbortTasksStatusEvent(List<AbortTaskStatus> tasks, Session replyTo, String replyToMessageId) {
        this.tasks = tasks;
        this.replyTo = replyTo;
        this.replyToMessageId = replyToMessageId;
    }

    public List<AbortTaskStatus> getTasks() {
        return this.tasks;
    }

    public void setTasks(List<AbortTaskStatus> tasks) {
        this.tasks = tasks;
    }

}
