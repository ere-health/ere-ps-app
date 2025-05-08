package health.ere.ps.jmx;

@SuppressWarnings("unused") //used by jmx
public interface ReadEPrescriptionsMXBean {

    int getTelematikIdAccepted(String telematikId);

    int getTelematikIdRejected(String telematikId);

    long getVSDRead();

    long getVSDReadFailed();

    long getTaskCalled();

    long getTaskCalledFailed();

    long getAccept();

    long getAcceptFailed();

    long getReject();

    long getRejectFailed();
}
