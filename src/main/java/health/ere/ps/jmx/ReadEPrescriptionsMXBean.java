package health.ere.ps.jmx;

@SuppressWarnings("unused")

public interface ReadEPrescriptionsMXBean {
    long getNumberEPrescriptionRead();
    void increaseNumberEPrescriptionRead();
    long getNumberEPrescriptionReadFailed();
    void increaseNumberEPrescriptionReadFailed();
}
