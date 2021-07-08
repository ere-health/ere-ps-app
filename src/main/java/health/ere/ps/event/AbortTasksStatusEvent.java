package health.ere.ps.event;

import java.util.ArrayList;
import java.util.List;

public class AbortTasksStatusEvent {

    private List<AbortTaskStatus> tasks = new ArrayList<>();

    
    public AbortTasksStatusEvent(List<AbortTaskStatus> tasks) {
        this.tasks = tasks;
    }

    public List<AbortTaskStatus> getTasks() {
        return this.tasks;
    }

    public void setTasks(List<AbortTaskStatus> tasks) {
        this.tasks = tasks;
    }

}
