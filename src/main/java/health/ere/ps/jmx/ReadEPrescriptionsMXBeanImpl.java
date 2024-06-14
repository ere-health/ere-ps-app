package health.ere.ps.jmx;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Logger;

@ApplicationScoped
public class ReadEPrescriptionsMXBeanImpl implements ReadEPrescriptionsMXBean {

    private static final Logger log = Logger.getLogger(ReadEPrescriptionsMXBeanImpl.class.getName());

    private final LongAdder readVSD = new LongAdder();
    private final LongAdder readVSDFailed = new LongAdder();
    private final LongAdder getTask = new LongAdder();
    private final LongAdder getTaskFailed = new LongAdder();
    private final LongAdder getAccept = new LongAdder();
    private final LongAdder getAcceptFailed = new LongAdder();
    private final LongAdder getReject = new LongAdder();
    private final LongAdder getRejectFailed = new LongAdder();

    void onStart(@Observes StartupEvent ev) {
        log.info("Registering " + getClass().getSimpleName());
        PsMXBeanManager.registerMXBean(this);
    }

    @Override
    public long getVSDRead() {
        return readVSD.sum();
    }

    public void increaseVSDRead() {
        readVSD.increment();
    }

    @Override
    public long getVSDReadFailed() {
        return readVSDFailed.sum();
    }

    public void increaseVSDFailed() {
        readVSDFailed.increment();
    }

    @Override
    public long getTaskCalled() {
        return getTask.sum();
    }

    @Override
    public long getTaskCalledFailed() {
        return getTaskFailed.sum();
    }

    public void increaseTasks() {
        getTask.increment();
    }

    public void increaseTasksFailed() {
        getTaskFailed.increment();
    }

    @Override
    public long getAccept() {
        return getAccept.sum();
    }

    public void increaseAccept() {
        getAccept.increment();
    }

    @Override
    public long getAcceptFailed() {
        return getAcceptFailed.sum();
    }

    public void increaseAcceptFailed() {
        getAcceptFailed.increment();
    }

    @Override
    public long getReject() {
        return getReject.sum();
    }

    public void increaseReject() {
        getReject.increment();
    }

    @Override
    public long getRejectFailed() {
        return getRejectFailed.sum();
    }

    public void increaseRejectFailed() {
        getRejectFailed.increment();
    }
}
