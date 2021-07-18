package health.ere.ps.model.config;


import javax.json.bind.annotation.JsonbProperty;

public class UserConfigurations {

    @JsonbProperty(value="erixa.hotfolder", nillable=true)
    private String erixaHotfolder;

    @JsonbProperty(value="erixa.drugstore.email", nillable=true)
    private String erixaDrugstoreEmail;

    @JsonbProperty(value="erixa.user.email", nillable=true)
    private String erixaUserEmail;

    @JsonbProperty(value="erixa.user.password", nillable=true)
    private String erixaUserPassword;

    @JsonbProperty(value="extractor.template.profile", nillable=true)
    private String muster16TemplateProfile;

    @JsonbProperty(value="connector.base-url", nillable=true)
    private String connectorBaseURL;

    @JsonbProperty(value="connector.mandant-id", nillable=true)
    private String mandantId;

    @JsonbProperty(value="connector.workplace-id", nillable=true)
    private String workplaceId;

    @JsonbProperty(value="connector.client-system-id", nillable=true)
    private String clientSystemId;

    @JsonbProperty(value="connector.user-id", nillable=true)
    private String userId;

    @JsonbProperty(value="connector.tvMode", nillable=true)
    private String tvMode;

    public UserConfigurations() {
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
