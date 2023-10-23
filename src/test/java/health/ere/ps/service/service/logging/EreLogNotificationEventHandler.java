package health.ere.ps.service.logging;

import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import health.ere.ps.event.EreLogNotificationEvent;

@ApplicationScoped
public class EreLogNotificationEventHandler {
    @Inject
    Logger logger;

    void logEventHandler(@ObservesAsync EreLogNotificationEvent ereLogNotificationEvent) {

        logger.info(ereLogNotificationEvent.toString());
    }
}
