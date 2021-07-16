package health.ere.ps.model.config;


import javax.json.bind.annotation.JsonbProperty;
import java.util.Properties;

public class UserConfigurations {

    @JsonbProperty("erixa.hotfolder")
    private String erixaHotfolder;

    @JsonbProperty("erixa.drugstore.email")
    private String erixaDrugstoreEmail;

    @JsonbProperty("erixa.user.email")
    private String erixaUserEmail;

    @JsonbProperty("erixa.user.password")
    private String erixaUserPassword;

    @JsonbProperty("extractor.template.profile")
    private String muster16TemplateProfile;

    @JsonbProperty("connector.base.url")
    private String connectorBaseURL;

    @JsonbProperty("connector.context.mandant.id")
    private String mandantId;

    @JsonbProperty("connector.context.workplace.id")
    private String workplaceId;

    @JsonbProperty("connector.context.client.system.id")
    private String clientSystemId;

    @JsonbProperty("connector.context.userId")
    private String userId;

    @JsonbProperty("connector.tvMode")
    private String tvMode;

    public UserConfigurations() {
    }

    public UserConfigurations(Properties properties) {

    }

    public String getErixaHotfolder() {
        return erixaHotfolder;
    }

    public void setErixaHotfolder(String erixaHotfolder) {
        this.erixaHotfolder = erixaHotfolder;
    }

    public String getErixaDrugstoreEmail() {
        return erixaDrugstoreEmail;
    }

    public void setErixaDrugstoreEmail(String erixaDrugstoreEmail) {
        this.erixaDrugstoreEmail = erixaDrugstoreEmail;
    }

    public String getErixaUserEmail() {
        return erixaUserEmail;
    }

    public void setErixaUserEmail(String erixaUserEmail) {
        this.erixaUserEmail = erixaUserEmail;
    }

    public String getErixaUserPassword() {
        return erixaUserPassword;
    }

    public void setErixaUserPassword(String erixaUserPassword) {
        this.erixaUserPassword = erixaUserPassword;
    }

    public String getMuster16TemplateProfile() {
        return muster16TemplateProfile;
    }

    public void setMuster16TemplateProfile(String muster16TemplateProfile) {
        this.muster16TemplateProfile = muster16TemplateProfile;
    }

    public String getConnectorBaseURL() {
        return connectorBaseURL;
    }

    public void setConnectorBaseURL(String connectorBaseURL) {
        this.connectorBaseURL = connectorBaseURL;
    }

    public String getMandantId() {
        return mandantId;
    }

    public void setMandantId(String mandantId) {
        this.mandantId = mandantId;
    }

    public String getWorkplaceId() {
        return workplaceId;
    }

    public void setWorkplaceId(String workplaceId) {
        this.workplaceId = workplaceId;
    }

    public String getClientSystemId() {
        return clientSystemId;
    }

    public void setClientSystemId(String clientSystemId) {
        this.clientSystemId = clientSystemId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTvMode() {
        return tvMode;
    }

    public void setTvMode(String tvMode) {
        this.tvMode = tvMode;
    }
}
