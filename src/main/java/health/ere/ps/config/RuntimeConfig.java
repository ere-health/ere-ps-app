package health.ere.ps.config;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.spi.CDI;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;

import health.ere.ps.model.config.UserConfigurations;

@Alternative
public class RuntimeConfig extends UserConfig {

    private static Logger log = Logger.getLogger(RuntimeConfig.class.getName());
    
    protected String eHBAHandle;
    protected String SMCBHandle;  

    
    public RuntimeConfig() {
        this.updateProperties(new UserConfigurations());
        try {
            UserConfig userConfig = CDI.current().select( UserConfig.class ).get();
            copyValuesFromUserConfig(userConfig);
        } catch(IllegalStateException ex) {
            log.log(Level.SEVERE, "Was not able to copy values from user config", ex);
        }
    }

    public void copyValuesFromUserConfig(UserConfig userConfig) {
        this.defaultConnectorBaseURI = userConfig.getConnectorBaseURL();

        this.defaultMandantId = userConfig.getMandantId();
        this.defaultClientSystemId = userConfig.getClientSystemId();
        this.defaultWorkplaceId = userConfig.getWorkplaceId();
        this.defaultUserId = userConfig.getUserId();
        
        this.defaultConnectorVersion = userConfig.getConnectorVersion();
        this.defaultTvMode = userConfig.getTvMode();

        this.defaultPruefnummer = userConfig.getPruefnummer();

        this.defaultMuster16TemplateProfile = userConfig.getMuster16TemplateConfiguration();
    }

    public RuntimeConfig(String eHBAHandle, String SMCBHandle) {
        this.eHBAHandle = eHBAHandle;
        this.SMCBHandle = SMCBHandle;
    }

    public RuntimeConfig(JsonObject object) {
        this();
        updateConfigurationsWithJsonObject(object);
    }

    public RuntimeConfig(HttpServletRequest httpServletRequest) {
        this();
        updateConfigurationsWithHttpServletRequest(httpServletRequest);
    }

    public void updateConfigurationsWithHttpServletRequest(HttpServletRequest httpServletRequest) {
        if(httpServletRequest == null) {
            return;
        }
        this.eHBAHandle = httpServletRequest.getHeader("X-eHBAHandle");
        this.SMCBHandle = httpServletRequest.getHeader("X-SMCBHandle");
        this.updateProperties(new UserConfigurations(httpServletRequest));
    }

    public void updateConfigurationsWithJsonObject(JsonObject object) {
        if(object == null) {
            return;
        }
        JsonObject jsonObject = object.getJsonObject("runtimeConfig");
        if(jsonObject != null) {
            this.eHBAHandle = jsonObject.getString("eHBAHandle", null);
            this.SMCBHandle = jsonObject.getString("SMCBHandle", null);
            this.updateProperties(new UserConfigurations(jsonObject));
        }
    }

    public String getEHBAHandle() {
        return this.eHBAHandle;
    }

    public void setEHBAHandle(String eHBAHandle) {
        this.eHBAHandle = eHBAHandle;
    }

    public String getSMCBHandle() {
        return this.SMCBHandle;
    }

    public void setSMCBHandle(String SMCBHandle) {
        this.SMCBHandle = SMCBHandle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eHBAHandle, SMCBHandle, this.getConfigurations());
    }

}
