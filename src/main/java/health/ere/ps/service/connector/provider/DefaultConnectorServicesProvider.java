package health.ere.ps.service.connector.provider;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
