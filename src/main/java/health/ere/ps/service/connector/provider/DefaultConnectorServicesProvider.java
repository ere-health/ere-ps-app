package health.ere.ps.service.connector.provider;

import de.health.service.cetp.config.UserRuntimeConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DefaultConnectorServicesProvider extends AbstractConnectorServicesProvider {

    @Inject
    UserRuntimeConfig userConfig;

    @PostConstruct
    void init() {
        initializeServices();
    }

    @Override
    public UserRuntimeConfig getUserConfig() {
        return userConfig;
    }
    
}
