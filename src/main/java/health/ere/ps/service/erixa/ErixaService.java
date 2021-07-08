package health.ere.ps.service.erixa;

import health.ere.ps.event.erixa.ErixaEvent;
import health.ere.ps.event.erixa.ErixaSyncEvent;
import health.ere.ps.event.erixa.SendToPharmacyEvent;
import health.ere.ps.model.erixa.ErixaSyncLoad;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import java.util.logging.Logger;

@Deprecated
@ApplicationScoped
public class ErixaService {

    private final Logger log = Logger.getLogger(ErixaService.class.getName());

    @Inject
    Event<ErixaSyncEvent> erixaSyncEvent;

    @Inject
    Event<SendToPharmacyEvent> sendToPharmacyEvent;

    public void generatePrescriptionBundle(@ObservesAsync ErixaEvent erixaEvent) {

        if ("sync".equals(erixaEvent.processType)) {
            ErixaSyncLoad load = new ErixaSyncLoad(erixaEvent.payload.getString("document"), erixaEvent.payload.getString("patient"));
            ErixaSyncEvent event = new ErixaSyncEvent(load);
            erixaSyncEvent.fireAsync(event);
        } else if("SendToPharmacy".equals(erixaEvent.processType)){
            SendToPharmacyEvent event = new SendToPharmacyEvent(erixaEvent.payload);
            sendToPharmacyEvent.fireAsync(event);
        }
    }
}

