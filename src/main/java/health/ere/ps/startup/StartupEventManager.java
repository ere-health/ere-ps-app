package health.ere.ps.startup;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

@ApplicationScoped
public class StartupEventManager {

    private static final Logger log = LoggerFactory.getLogger(StartupEventManager.class.getName());

    @Any
    @Inject
    Instance<StartupEventListener> listeners;

    public void onStart(@Observes StartupEvent ev) {
        listeners.stream()
            .sorted(Comparator.comparingInt(StartupEventListener::getPriority))
            .forEach(listener -> {
                try {
                    listener.onStart(ev);
                } catch (Throwable e) {
                    String msg = String.format(
                        "Error while starting %s -> %s", listener.getClass().getSimpleName(), e.getMessage()
                    );
                    log.error(msg, e);
                    throw new Error(e);
                }
            });
    }
}