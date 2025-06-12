package health.ere.ps.jmx;

@SuppressWarnings("unused")
public interface TelematikMXBean {

    long acceptedTasks();

    long rejectedTasks();
}