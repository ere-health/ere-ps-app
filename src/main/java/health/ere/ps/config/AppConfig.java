package health.ere.ps.config;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppConfig {

    @ConfigProperty(name = "ere.validator.validate.sign.request.bundles.enabled", defaultValue = "no")
    String validateSignRequestBundles;


    public boolean isValidateSignRequestBundles() {
        return StringUtils.isNotBlank(validateSignRequestBundles) &&
                validateSignRequestBundles.equalsIgnoreCase("Yes");
    }
}
