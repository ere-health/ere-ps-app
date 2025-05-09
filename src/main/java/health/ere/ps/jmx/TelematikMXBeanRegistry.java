package health.ere.ps.jmx;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import static health.ere.ps.jmx.PsMXBeanManager.getMXBean;
import static health.ere.ps.jmx.PsMXBeanManager.registerMXBean;

@ApplicationScoped
public class TelematikMXBeanRegistry {

    private static final Logger log = LoggerFactory.getLogger(TelematikMXBeanRegistry.class.getName());

    private final Map<String, LongAdder> acceptedCount = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> rejectedCount = new ConcurrentHashMap<>();

    public void registerTelematikIdBean(String telematikId) {
        String name = telematikId + ":type=TelematikMXBean";
        if (getMXBean(name, TelematikMXBean.class) != null) {
            return;
        }
        log.info("Registering TelematikMXBean " + telematikId);
        acceptedCount.put(telematikId, new LongAdder());
        rejectedCount.put(telematikId, new LongAdder());

        TelematikMXBean telematikMXBean = new TelematikMXBean() {
            @Override
            public long acceptedTasks() {
                return acceptedCount.get(telematikId).sum();
            }
            @Override
            public long rejectedTasks() {
                return rejectedCount.get(telematikId).sum();
            }
        };
        registerMXBean(name, telematikMXBean);
    }

    public void countAccepted(String telematikId) {
        acceptedCount.computeIfAbsent(telematikId, b -> new LongAdder()).increment();
    }

    public void countRejected(String telematikId) {
        rejectedCount.computeIfAbsent(telematikId, b -> new LongAdder()).decrement();
    }
}