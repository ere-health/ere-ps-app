package health.ere.ps.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;

import health.ere.ps.model.config.UserConfigurations;

@Alternative
public class RuntimeConfig extends UserConfig {

    private static Logger log = Logger.getLogger(RuntimeConfig.class.getName());
    
    protected String eHBAHandle = null;
    protected String SMCBHandle = null;
    protected boolean sendPreview = true;

    //todo: shouldn't idp parameter and prescriptionServerURL be part of UserConfig?
    protected String idpBaseURL = null;
    protected String idpAuthRequestRedirectURL = null;
    protected String idpClientId = null;

    protected String prescriptionServerURL = null;

    
    public RuntimeConfig() {
        this.updateProperties(new UserConfigurations());
        try {
            UserConfig userConfig = CDI.current().select( UserConfig.class ).get();
            copyValuesFromUserConfig(userConfig);
        } catch(IllegalStateException ex) {
            log.log(Level.SEVERE, "Was not able to copy values from user config", ex);
        }
    }

    // for unit test
    public RuntimeConfig(String defaultConnectorBaseURI) {
        this.defaultConnectorBaseURI = defaultConnectorBaseURI;
    }

    public void copyValuesFromUserConfig(UserConfig userConfig) {
        try {
            this.defaultConnectorBaseURI = userConfig.getConnectorBaseURL();

            this.defaultMandantId = userConfig.getMandantId();
            this.defaultClientSystemId = userConfig.getClientSystemId();
            this.defaultWorkplaceId = userConfig.getWorkplaceId();
            this.defaultUserId = Optional.ofNullable(userConfig.getUserId());
            
            this.defaultConnectorVersion = userConfig.getConnectorVersion();
            this.defaultTvMode = userConfig.getTvMode();

            this.defaultPruefnummer = userConfig.getPruefnummer();

            this.defaultMuster16TemplateProfile = userConfig.getMuster16TemplateConfiguration();
            this.updateProperties(userConfig.getConfigurations());
        } catch(Exception ex) {
            log.log(Level.SEVERE, "Was not able to copy values from user config", ex);
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
        this.idpBaseURL = httpServletRequest.getHeader("X-idpBaseURL");
        this.idpClientId = httpServletRequest.getHeader("X-idpClientId");
        this.idpAuthRequestRedirectURL = httpServletRequest.getHeader("X-idpAuthRequestRedirectURL");
        this.prescriptionServerURL = httpServletRequest.getHeader("X-prescriptionServerURL");

        if (httpServletRequest.getHeader("X-sendPreview") != null) {
            this.sendPreview = !httpServletRequest.getHeader("X-sendPreview").equalsIgnoreCase("false");
        }

        this.updateProperties(this.getConfigurations().updateWithRequest(httpServletRequest));
    }

    public void updateConfigurationsWithJsonObject(JsonObject object) {
        if(object == null) {
            return;
        }
        JsonObject jsonObject = object.getJsonObject("runtimeConfig");
        if(jsonObject != null) {
            this.eHBAHandle = jsonObject.getString("eHBAHandle", null);
            this.SMCBHandle = jsonObject.getString("SMCBHandle", null);
            this.idpBaseURL = jsonObject.getString("idp.base.url", null);
            this.idpClientId = jsonObject.getString("idp.client.id", null);
            this.idpAuthRequestRedirectURL = jsonObject.getString("idp.auth.request.redirect.url", null);
            this.prescriptionServerURL = jsonObject.getString("ere.workflow-service.prescription.server.url", null);
            this.sendPreview = jsonObject.getBoolean("sendPreview", true);
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

    public boolean isSendPreview() {
        return sendPreview;
    }

    public void setSendPreview(boolean sendPreview) {
        this.sendPreview = sendPreview;
    }

    public String getIdpBaseURL() {
        return this.idpBaseURL;
    }

    public void setIdpBaseURL(String idpBaseURL) {
        this.idpBaseURL = idpBaseURL;
    }

    public String getIdpAuthRequestRedirectURL() {
        return this.idpAuthRequestRedirectURL;
    }

    public void setIdpAuthRequestRedirectURL(String idpAuthRequestRedirectURL) {
        this.idpAuthRequestRedirectURL = idpAuthRequestRedirectURL;
    }

    public String getIdpClientId() {
        return this.idpClientId;
    }

    public void setIdpClientId(String idpClientId) {
        this.idpClientId = idpClientId;
    }

    public String getPrescriptionServerURL() {
        return this.prescriptionServerURL;
    }

    public void setPrescriptionServerURL(String prescriptionServerURL) {
        this.prescriptionServerURL = prescriptionServerURL;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eHBAHandle, SMCBHandle, idpBaseURL, idpAuthRequestRedirectURL, idpClientId, prescriptionServerURL, this.getConfigurations(), super.hashCode());
    }

    public String getConnectorAddress() {
        try {
            return new URL(getConnectorBaseURL()).getHost();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}