package health.ere.ps.config;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppConfig {

    @ConfigProperty(name = "ere.validator.validate.sign.request.bundles.enabled")
    boolean validateSignRequestBundles;


    public boolean isValidateSignRequestBundles() {
        return validateSignRequestBundles;
    }
}
