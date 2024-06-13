package health.ere.ps.jmx;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.LongAdder;

@ApplicationScoped
public class ReadEPrescriptionsMXBeanImpl implements ReadEPrescriptionsMXBean {
    private final LongAdder numberReads = new LongAdder();
    private final LongAdder numberReadsFailed = new LongAdder();

    @PostConstruct
    public void init() {
        PsMXBeanManager.registerMXBean(this);
    }

    @Override
    public long getNumberEPrescriptionRead() {
        return numberReads.sum();
    }

    @Override
    public void increaseNumberEPrescriptionRead() {
        numberReads.increment();
    }

    @Override
    public long getNumberEPrescriptionReadFailed() {
        return numberReadsFailed.sum();
    }

    @Override
    public void increaseNumberEPrescriptionReadFailed() {
        numberReadsFailed.increment();
    }
}
