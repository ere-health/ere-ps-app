package health.ere.ps.event;

import health.ere.ps.model.config.UserConfigurations;

public class CheckSettingsEvent {
    private UserConfigurations userConfigurations;

    public CheckSettingsEvent() {

    }

    public CheckSettingsEvent(UserConfigurations userConfigurations) {
        setUserConfigurations(userConfigurations);
    }

    public UserConfigurations getUserConfigurations() {
        return this.userConfigurations;
    }

    public void setUserConfigurations(UserConfigurations userConfigurations) {
        this.userConfigurations = userConfigurations;
    }
}
