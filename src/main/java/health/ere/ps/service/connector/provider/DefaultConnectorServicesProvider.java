package health.ere.ps.service.connector.provider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import health.ere.ps.config.UserConfig;

@ApplicationScoped
public class DefaultConnectorServicesProvider extends AbstractConnectorServicesProvider {

    @Inject
    UserConfig userConfig;

    @Override
    public UserConfig getUserConfig() {
        return userConfig;
    }
    
}
