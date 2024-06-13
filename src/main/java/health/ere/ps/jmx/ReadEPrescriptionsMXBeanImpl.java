package health.ere.ps.jmx;

import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

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
