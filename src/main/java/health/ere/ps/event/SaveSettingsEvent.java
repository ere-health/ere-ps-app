package health.ere.ps.event;

import jakarta.websocket.Session;

import health.ere.ps.model.config.UserConfigurations;

public class SaveSettingsEvent extends AbstractEvent {
    private UserConfigurations userConfigurations;

    public SaveSettingsEvent() {

    }

    public SaveSettingsEvent(UserConfigurations userConfigurations) {
        setUserConfigurations(userConfigurations);
    }

    public SaveSettingsEvent(UserConfigurations userConfigurations, Session senderSession, String messageId) {
        setUserConfigurations(userConfigurations);
        setReplyTo(senderSession);
        setId(messageId);
    }

    public UserConfigurations getUserConfigurations() {
        return this.userConfigurations;
    }

    public void setUserConfigurations(UserConfigurations userConfigurations) {
        this.userConfigurations = userConfigurations;
    }
}
