package health.ere.ps.config.interceptor;

import health.ere.ps.event.config.UserConfigurationsUpdateEvent;
import health.ere.ps.model.config.UserConfigurations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class ConfigUpdateObserverTest {

    @Test
    public void handleUpdateProperties_UpdatesValue() {
        ConfigUpdateObserver observer = new ConfigUpdateObserver();
        observer.handleUpdateProperties(new UserConfigurationsUpdateEvent(new UserConfigurations()));
        boolean result = observer.pullValue();

        Assertions.assertTrue(result);
    }

    @Test
    public void handleUpdateProperties_MultipleUpdates_ReturnsTrueOnlyOnce() {
        ConfigUpdateObserver observer = new ConfigUpdateObserver();
        observer.handleUpdateProperties(new UserConfigurationsUpdateEvent(new UserConfigurations()));
        boolean result1 = observer.pullValue();

        observer.handleUpdateProperties(new UserConfigurationsUpdateEvent(new UserConfigurations()));
        boolean result2 = observer.pullValue();

        Assertions.assertTrue(result1);
        Assertions.assertFalse(result2);
    }

    @Test
    public void pullValue_NoUpdates_ReturnsFalse() {
        ConfigUpdateObserver observer = new ConfigUpdateObserver();

        boolean result = observer.pullValue();

        Assertions.assertFalse(result);
    }
}