package health.ere.ps.service.connector.provider;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import health.ere.ps.config.UserConfig;

@ApplicationScoped
public class DefaultConnectorServicesProvider extends AbstractConnectorServicesProvider {

    @Inject
    UserConfig userConfig;

    @PostConstruct
    void init() {
        initializeServices();
    }

    @Override
    public UserConfig getUserConfig() {
        return userConfig;
    }
    
}
