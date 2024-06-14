package health.ere.ps.jmx;

@SuppressWarnings("unused")

public interface ReadEPrescriptionsMXBean {
    long getVSDRead();

    long getVSDReadFailed();

    long getTaskCalled();

    long getTaskCalledFailed();

    long getAccept();

    long getAcceptFailed();

    long getReject();

    long getRejectFailed();
}
