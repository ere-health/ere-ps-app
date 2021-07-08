package health.ere.ps.event;

public class AbortTaskStatus {


    public enum Status {
        OK, ERROR
    }

    private AbortTaskEntry abortTaskEntry;
    private Status status;
    private Throwable throwable;

    public AbortTaskStatus() {

    }

    public AbortTaskStatus(AbortTaskEntry abortTaskEntry) {
        this.abortTaskEntry = abortTaskEntry;
    }

    public AbortTaskEntry getAbortTaskEntry() {
        return this.abortTaskEntry;
    }

    public void setAbortTaskEntry(AbortTaskEntry abortTaskEntry) {
        this.abortTaskEntry = abortTaskEntry;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }


}
