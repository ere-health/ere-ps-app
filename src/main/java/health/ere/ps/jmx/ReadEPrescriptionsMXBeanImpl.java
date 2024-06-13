package health.ere.ps.jmx;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Logger;

@ApplicationScoped
public class ReadEPrescriptionsMXBeanImpl implements ReadEPrescriptionsMXBean {

    private static final Logger log = Logger.getLogger(ReadEPrescriptionsMXBeanImpl.class.getName());

    private final LongAdder numberReads = new LongAdder();
    private final LongAdder numberReadsFailed = new LongAdder();

    void onStart(@Observes StartupEvent ev) {
        log.info("Registering ReadEPrescriptionsMXBean");
        PsMXBeanManager.registerMXBean(this);
    }

    @Override
    public long getNumberEPrescriptionRead() {
        return numberReads.sum();
    }

    public void increaseNumberEPrescriptionRead() {
        numberReads.increment();
    }

    @Override
    public long getNumberEPrescriptionReadFailed() {
        return numberReadsFailed.sum();
    }

    public void increaseNumberEPrescriptionReadFailed() {
        numberReadsFailed.increment();
    }
}
