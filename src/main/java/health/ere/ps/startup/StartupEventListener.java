package health.ere.ps.startup;

import io.quarkus.runtime.StartupEvent;

public interface StartupEventListener {

    int getPriority();

    void onStart(StartupEvent ev) throws Exception;
}