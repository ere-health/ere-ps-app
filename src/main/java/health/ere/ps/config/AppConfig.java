package health.ere.ps.config;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppConfig {

    @ConfigProperty(name = "ere.validator.validate.sign.request.bundles.enabled", defaultValue = "no")
    String validateSignRequestBundles;

    @ConfigProperty(name = "connector.version", defaultValue="PTV4")
    String connectorVersion;


    public boolean isValidateSignRequestBundles() {
        return StringUtils.isNotBlank(validateSignRequestBundles) &&
                validateSignRequestBundles.equalsIgnoreCase("Yes");
    }

    public String getConnectorVersion() {
        return connectorVersion;
    }

    public void setConnectorVersion(String connectorVersion) {
        this.connectorVersion = connectorVersion;
    }
}
