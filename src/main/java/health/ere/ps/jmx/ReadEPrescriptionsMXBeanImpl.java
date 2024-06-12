package health.ere.ps.jmx;

import health.ere.ps.exception.common.security.SecretsManagerException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class ReadEPrescriptionsMXBeanImpl implements ReadEPrescriptionsMXBean {
    private final AtomicLong numberReads = new AtomicLong();
    private final AtomicLong numberReadsFailed = new AtomicLong();

    @PostConstruct
    public void init() {
        PsMXBeanManager.registerMXBean(this);
    }

    @Override
    public long getNumberEPrescriptionRead() {
        return numberReads.get();
    }

    @Override
    public void increaseNumberEPrescriptionRead() {
        numberReads.incrementAndGet();
    }

    @Override
    public long getNumberEPrescriptionReadFailed() {
        return numberReadsFailed.get();
    }

    @Override
    public void increaseNumberEPrescriptionReadFailed() {
        numberReadsFailed.incrementAndGet();
    }
}
