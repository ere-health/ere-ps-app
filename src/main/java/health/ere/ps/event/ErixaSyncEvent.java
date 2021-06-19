package health.ere.ps.event;

import health.ere.ps.model.erixa.ErixaSyncLoad;

public class ErixaSyncEvent {

    public final ErixaSyncLoad load;

    public ErixaSyncEvent(ErixaSyncLoad load) {
        this.load = load;
    }
}
