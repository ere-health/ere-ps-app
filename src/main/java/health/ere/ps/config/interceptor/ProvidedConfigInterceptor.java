package health.ere.ps.config.interceptor;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.connector.provider.AbstractConnectorServicesProvider;
import health.ere.ps.service.connector.provider.DefaultConnectorServicesProvider;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;

@Priority(600)
@Interceptor
@ProvidedConfig
public class ProvidedConfigInterceptor {

    private final Logger log = Logger.getLogger(getClass().getName());

    @Inject
    ConfigUpdateObserver observer;

    @Inject
    MultiConnectorServicesProvider multiConnectorServicesProvider;
    
    @Inject
    DefaultConnectorServicesProvider defaultConnectorServicesProvider;

    @Inject
    SecretsManagerService secrectsManagerService;

    @AroundInvoke
    public Object capture(InvocationContext invocationContext) throws Exception {
        if (observer.pullValue()) {
            log.info("Detected change in user configurations. Connector services will be re-initialized.");
            secrectsManagerService.updateSSLContext();
            AbstractConnectorServicesProvider provider = (AbstractConnectorServicesProvider) invocationContext.getTarget();
            try {
                provider.initializeServices(true);
            } catch(Exception ex) {
                log.log(Level.WARNING, "Could not init connector with new settings", ex);
            }
            multiConnectorServicesProvider.clearAll();
        }
        return invocationContext.proceed();
    }
}
