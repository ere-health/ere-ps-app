package health.ere.ps.service.logging;

import health.ere.ps.event.EreLogNotificationEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;

import java.util.logging.Logger;

@ApplicationScoped
public class EreLogNotificationEventHandler {

    private static final Logger logger = Logger.getLogger(EreLogNotificationEventHandler.class.getName());

    void logEventHandler(@ObservesAsync EreLogNotificationEvent ereLogNotificationEvent) {

        logger.info(ereLogNotificationEvent.toString());
    }
}
