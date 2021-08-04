package health.ere.ps.service.erixa;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;

import health.ere.ps.event.erixa.ErixaEvent;
import health.ere.ps.event.erixa.ErixaSyncEvent;
import health.ere.ps.event.erixa.SendToPharmacyEvent;
import health.ere.ps.model.erixa.ErixaSyncLoad;

@Deprecated
@ApplicationScoped
public class ErixaService {

    private final Logger log = Logger.getLogger(ErixaService.class.getName());

    @Inject
    Event<ErixaSyncEvent> erixaSyncEvent;

    @Inject
    Event<SendToPharmacyEvent> sendToPharmacyEvent;

    @Inject
    Event<Exception> exceptionEvent;

    public void generatePrescriptionBundle(@ObservesAsync ErixaEvent erixaEvent) {

        if ("sync".equals(erixaEvent.processType)) {
            ErixaSyncLoad load = new ErixaSyncLoad(erixaEvent.payload.getString("document"), erixaEvent.payload.getString("patient"));
            ErixaSyncEvent event = new ErixaSyncEvent(load);
            erixaSyncEvent.fireAsync(event);
        } else if("SendToPharmacy".equals(erixaEvent.processType)){
            try {
                SendToPharmacyEvent event = new SendToPharmacyEvent(erixaEvent.payload);
                sendToPharmacyEvent.fireAsync(event);
            } catch (JsonProcessingException e) {
                log.severe("JsonProcessingException: " + e.getMessage());
                exceptionEvent.fireAsync(e);
            }
        }
    }
}

