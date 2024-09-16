package health.ere.ps.resource.gematik;

import java.util.Collections;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import jakarta.servlet.http.HttpServletRequest;

public class Extractors {
        //todo: refactor - move to RuntimeConfig? (there is already the updateConfigurationsWithHttpServletRequest)
    public static RuntimeConfig extractRuntimeConfigFromHeaders(HttpServletRequest httpServletRequest, UserConfig userConfig) {
        for(Object name : Collections.list(httpServletRequest.getHeaderNames())) {
            if(name.toString().startsWith("X-")) {
                RuntimeConfig runtimeConfig = new RuntimeConfig();
                runtimeConfig.copyValuesFromUserConfig(userConfig);
                runtimeConfig.updateConfigurationsWithHttpServletRequest(httpServletRequest);
                return runtimeConfig;
            }
        }
        return null;
    }
}
