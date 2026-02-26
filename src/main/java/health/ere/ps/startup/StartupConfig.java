package health.ere.ps.startup;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Getter
@ApplicationScoped
public class StartupConfig {

    @ConfigProperty(name = "startup-events.disabled", defaultValue = "false")
    boolean startupEventsDisabled;

}
