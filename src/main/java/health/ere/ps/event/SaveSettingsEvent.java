package health.ere.ps.event;

import health.ere.ps.model.config.UserConfigurations;

public class SaveSettingsEvent {
    private UserConfigurations userConfigurations;

    public SaveSettingsEvent() {

    }

    public SaveSettingsEvent(UserConfigurations userConfigurations) {
        setUserConfigurations(userConfigurations);
    }

    public UserConfigurations getUserConfigurations() {
        return this.userConfigurations;
    }

    public void setUserConfigurations(UserConfigurations userConfigurations) {
        this.userConfigurations = userConfigurations;
    }
}
