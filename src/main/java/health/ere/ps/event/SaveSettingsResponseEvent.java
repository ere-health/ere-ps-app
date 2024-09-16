package health.ere.ps.event;

import health.ere.ps.model.config.UserConfigurations;

public class SaveSettingsResponseEvent extends AbstractEvent {
    private UserConfigurations userConfigurations;

    public SaveSettingsResponseEvent() {

    }

    public SaveSettingsResponseEvent(UserConfigurations userConfigurations) {
        setUserConfigurations(userConfigurations);
    }

    public UserConfigurations getUserConfigurations() {
        return this.userConfigurations;
    }

    public void setUserConfigurations(UserConfigurations userConfigurations) {
        this.userConfigurations = userConfigurations;
    }
}
