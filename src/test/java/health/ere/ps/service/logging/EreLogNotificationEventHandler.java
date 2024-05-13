package health.ere.ps.service.logging;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;

import health.ere.ps.event.EreLogNotificationEvent;

@ApplicationScoped
public class EreLogNotificationEventHandler {
    @Inject
    Logger logger;

    void logEventHandler(@ObservesAsync EreLogNotificationEvent ereLogNotificationEvent) {

        logger.info(ereLogNotificationEvent.toString());
    }
}
