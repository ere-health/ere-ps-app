package health.ere.ps.service.erixa;

import health.ere.ps.event.ErixaEvent;
import health.ere.ps.event.ErixaSyncEvent;
import health.ere.ps.model.erixa.ErixaSyncLoad;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import java.util.logging.Logger;

@ApplicationScoped
public class ErixaService {

    private final Logger log = Logger.getLogger(ErixaService.class.getName());

    @Inject
    Event<ErixaSyncEvent> erixaSyncEvent;

    public void generatePrescriptionBundle(@ObservesAsync ErixaEvent event) {

        if ("sync".equals(event.processType)) {
            ErixaSyncLoad load = new ErixaSyncLoad(event.payload.getString("document"), event.payload.getString("patient"));
            ErixaSyncEvent syncEvent = new ErixaSyncEvent(load);
            erixaSyncEvent.fireAsync(syncEvent);
        }
    }
}

