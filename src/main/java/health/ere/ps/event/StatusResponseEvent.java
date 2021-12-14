package health.ere.ps.event;

import health.ere.ps.model.status.Status;

public class StatusResponseEvent extends AbstractEvent {
    private Status status;

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    // TODO: add variables here
}
