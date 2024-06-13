package health.ere.ps.jmx;

@SuppressWarnings("unused")

public interface ReadEPrescriptionsMXBean {
    long getNumberEPrescriptionRead();

    long getNumberEPrescriptionReadFailed();
}
