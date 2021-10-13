package health.ere.ps.config;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.spi.CDI;
import javax.json.JsonObject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

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
        for(Field field : UserConfig.class.getDeclaredFields()) {
            try {
                ConfigProperty property = field.getAnnotation(ConfigProperty.class);
                if(property != null) {
                    field.set(this, field.get(userConfig));
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "Could not process runtime config", e);
            }
        }
    }

    public RuntimeConfig(String eHBAHandle, String SMCBHandle) {
        this.eHBAHandle = eHBAHandle;
        this.SMCBHandle = SMCBHandle;
    }

    public RuntimeConfig(JsonObject object) {
        this();
        updateConfigurationsWithJsonObject(object);
    }

    public void updateConfigurationsWithJsonObject(JsonObject object) {
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
