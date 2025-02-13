package health.ere.ps;

import de.health.service.config.api.IFeatureConfig;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FeatureConfig implements IFeatureConfig {

    @Override
    public boolean isMutualTlsEnabled() {
        return false;
    }

    @Override
    public boolean isCetpEnabled() {
        return true;
    }

    @Override
    public boolean isCardlinkEnabled() {
        return true;
    }

    @Override
    public boolean isNativeFhirEnabled() {
        return false;
    }

    @Override
    public boolean isExternalPnwEnabled() {
        return false;
    }
}
