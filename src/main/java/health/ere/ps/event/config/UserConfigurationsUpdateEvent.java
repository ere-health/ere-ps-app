package health.ere.ps.event.config;

import health.ere.ps.model.config.UserConfigurations;

public class UserConfigurationsUpdateEvent {

    private final UserConfigurations configurations;

    public UserConfigurationsUpdateEvent(UserConfigurations configurations) {
        this.configurations = configurations;
    }

    public UserConfigurations getConfigurations() {
        return configurations;
    }
}
