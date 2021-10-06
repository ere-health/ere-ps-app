package health.ere.ps.service.connector.provider;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;

public class SingleConnectorServicesProvider extends AbstractConnectorServicesProvider {
    private final static Logger log = Logger.getLogger(SingleConnectorServicesProvider.class.getName());

    RuntimeConfig runtimeConfig;

    public SingleConnectorServicesProvider(RuntimeConfig runtimeConfig) {
        this.runtimeConfig = runtimeConfig;
    }

    @PostConstruct
    void init() {
        initializeServices();
    }
    public UserConfig getUserConfig() {
        return runtimeConfig;
    }
}
