package health.ere.ps.event.erixa;

import health.ere.ps.model.erixa.ErixaSyncLoad;

@Deprecated
public class ErixaSyncEvent {

    public final ErixaSyncLoad load;

    public ErixaSyncEvent(ErixaSyncLoad load) {
        this.load = load;
    }
}
