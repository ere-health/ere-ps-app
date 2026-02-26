package health.ere.ps.startup;

import io.quarkus.runtime.StartupEvent;
import jakarta.inject.Inject;
import lombok.Getter;

import java.util.logging.Logger;

@Getter
public abstract class StartableService implements StartupEventListener {

    public static final int IdpClient = 1000;
    public static final int RegisterSMCB = 2000;

    private final Logger log = Logger.getLogger(getClass().getName());

    @Inject
    StartupConfig startupConfig;

    @Override
    public int getPriority() {
        return 2500;
    }

    public void onStart(StartupEvent ev) throws Exception {
        String className = getClass().getSimpleName();
        if (startupConfig.isStartupEventsDisabled()) {
            log.warning(String.format(
                "[%s] STARTUP events are disabled by config property, initialization is SKIPPED", className)
            );
        } else {
            long start = System.currentTimeMillis();
            doStart();
            long delta = System.currentTimeMillis() - start;
            log.info(String.format("[%s] STARTED in %d ms", className, delta));
        }
    }

    protected abstract void doStart() throws Exception;
}