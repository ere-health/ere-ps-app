package health.ere.ps.config.interceptor;

import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.connector.provider.AbstractConnectorServicesProvider;

@Priority(600)
@Interceptor
@ProvidedConfig
public class ProvidedConfigInterceptor {

    private final Logger log = Logger.getLogger(getClass().getName());

    @Inject
    ConfigUpdateObserver observer;

    @Inject
    SecretsManagerService secrectsManagerService;

    @AroundInvoke
    public Object capture(InvocationContext invocationContext) throws Exception {
        if (observer.pullValue()) {
            log.info("Detected change in user configurations. Connector services will be re-initialized.");
            secrectsManagerService.updateSSLContext();
            AbstractConnectorServicesProvider provider = (AbstractConnectorServicesProvider) invocationContext.getTarget();
            provider.initializeServices();
        }
        return invocationContext.proceed();
    }
}
