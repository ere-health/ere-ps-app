package health.ere.ps.config;

import java.util.Objects;

public class SimpleUserConfig {
	
	private String erixaHotfolder;

	private String erixaDrugstoreEmail;

	private String erixaUserEmail;
	
	private String erixaApiKey;

	private String muster16TemplateProfile;

	private String connectorBaseURL;

	private String mandantId;

	private String workplaceId;

	private String clientSystemId;

	private String userId;

	private String version;

	private String tvMode;

//	private String clientCertificate;
//
//	private String clientCertificatePassword;
//
//	private String basicAuthUsername;
//
//	private String basicAuthPassword;

	private String pruefnummer;

//	private String erixaUserPassword;
	
	
	//the following attributes are for RuntimeConfig
	
	private String eHBAHandle;
    private String SMCBHandle;
    private boolean sendPreview;
    private String idpBaseURL;
    private String idpAuthRequestRedirectURL;
    private String idpClientId;

	
	
	public SimpleUserConfig(UserConfig userConfig) {
		setValues(userConfig);
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

	public String getErixaApiKey() {
		return erixaApiKey;
	}

	public void setErixaApiKey(String erixaApiKey) {
		this.erixaApiKey = erixaApiKey;
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTvMode() {
		return tvMode;
	}

	public void setTvMode(String tvMode) {
		this.tvMode = tvMode;
	}

	public String getPruefnummer() {
		return pruefnummer;
	}

	public void setPruefnummer(String pruefnummer) {
		this.pruefnummer = pruefnummer;
	}
	
	
	
	public String geteHBAHandle() {
		return eHBAHandle;
	}

	public void seteHBAHandle(String eHBAHandle) {
		this.eHBAHandle = eHBAHandle;
	}

	public String getSMCBHandle() {
		return SMCBHandle;
	}

	public void setSMCBHandle(String sMCBHandle) {
		SMCBHandle = sMCBHandle;
	}

	public boolean isSendPreview() {
		return sendPreview;
	}

	public void setSendPreview(boolean sendPreview) {
		this.sendPreview = sendPreview;
	}

	public String getIdpBaseURL() {
		return idpBaseURL;
	}

	public void setIdpBaseURL(String idpBaseURL) {
		this.idpBaseURL = idpBaseURL;
	}

	public String getIdpAuthRequestRedirectURL() {
		return idpAuthRequestRedirectURL;
	}

	public void setIdpAuthRequestRedirectURL(String idpAuthRequestRedirectURL) {
		this.idpAuthRequestRedirectURL = idpAuthRequestRedirectURL;
	}

	public String getIdpClientId() {
		return idpClientId;
	}

	public void setIdpClientId(String idpClientId) {
		this.idpClientId = idpClientId;
	}

	private void setValues(UserConfig userConfig) {
		this.erixaHotfolder = userConfig.getConfigurations().getErixaHotfolder();
		this.erixaDrugstoreEmail =  userConfig.getConfigurations().getErixaDrugstoreEmail();
		this.erixaUserEmail = userConfig.getConfigurations().getErixaUserEmail();
		this.erixaApiKey = userConfig.getConfigurations().getErixaApiKey();
		this.muster16TemplateProfile = userConfig.getConfigurations().getMuster16TemplateProfile();
		this.connectorBaseURL = userConfig.getConfigurations().getConnectorBaseURL();
		this.mandantId = userConfig.getConfigurations().getMandantId();
		this.workplaceId = userConfig.getConfigurations().getWorkplaceId();
		this.clientSystemId = userConfig.getConfigurations().getClientSystemId();
		this.userId = userConfig.getConfigurations().getUserId();
		this.version = userConfig.getConfigurations().getVersion();
		this.tvMode = userConfig.getConfigurations().getTvMode();
		if(userConfig.getClass().getName().contains("RuntimeConfig")) {
			this.eHBAHandle = ((RuntimeConfig)userConfig).getEHBAHandle();
			this.SMCBHandle = ((RuntimeConfig)userConfig).getSMCBHandle();
			this.sendPreview = ((RuntimeConfig)userConfig).isSendPreview();
			this.idpAuthRequestRedirectURL = ((RuntimeConfig)userConfig).getIdpAuthRequestRedirectURL();
			this.idpClientId = ((RuntimeConfig)userConfig).getIdpClientId();
		}		
	}
	
	@Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        SimpleUserConfig other = (SimpleUserConfig) obj;
        return Objects.equals(erixaHotfolder, other.erixaHotfolder) && Objects.equals(erixaDrugstoreEmail, other.erixaDrugstoreEmail)
                && Objects.equals(erixaUserEmail, other.erixaUserEmail)
                && Objects.equals(erixaApiKey, other.erixaApiKey) && Objects.equals(muster16TemplateProfile, other.muster16TemplateProfile)
                && Objects.equals(connectorBaseURL, other.connectorBaseURL) && Objects.equals(mandantId, other.mandantId)
                && Objects.equals(workplaceId, other.workplaceId) && Objects.equals(clientSystemId, other.clientSystemId)
                && Objects.equals(userId, other.userId) && Objects.equals(version, other.version)
				&& Objects.equals(tvMode, other.tvMode) && Objects.equals(eHBAHandle, other.eHBAHandle) && Objects.equals(SMCBHandle, other.SMCBHandle)
				&& sendPreview == other.sendPreview && Objects.equals(idpBaseURL, other.idpBaseURL) 
				&& Objects.equals(idpAuthRequestRedirectURL, other.idpAuthRequestRedirectURL) && Objects.equals(idpClientId, other.idpClientId);
                
    }
	
	@Override
    public int hashCode() {
        return Objects.hash(
        		erixaHotfolder, erixaDrugstoreEmail, erixaUserEmail, erixaApiKey, muster16TemplateProfile, connectorBaseURL, mandantId,
        		workplaceId, clientSystemId, userId, version, tvMode, eHBAHandle, SMCBHandle, sendPreview, idpBaseURL, idpAuthRequestRedirectURL,
        		idpClientId
        );
    }

	 @Override
	    public String toString() {
	        return "SimpleUserConfig{" +
	               "erixaHotfolder='" + erixaHotfolder + '\'' +
	               ", erixaDrugstoreEmail='" + erixaDrugstoreEmail + '\'' +
	               ", erixaUserEmail=" + erixaUserEmail +
	               ", erixaApiKey='" + erixaApiKey + '\'' +
	               ", muster16TemplateProfile='" + muster16TemplateProfile + '\'' +
	               ", connectorBaseURL='" + connectorBaseURL + '\'' +
	               ", mandantId='" + mandantId + '\'' +
	               ", workplaceId='" + workplaceId + '\'' +
	               ", clientSystemId='" + clientSystemId + '\'' +
	               ", userId='" + userId + '\'' +
	               ", version='" + version + '\'' +
	               ", tvMode='" + tvMode + '\'' +
	               ", eHBAHandle='" + eHBAHandle + '\'' +
	               ", SMCBHandle='" + SMCBHandle + '\'' +
	               ", sendPreview='" + sendPreview + '\'' +
	               ", idpBaseURL='" + idpBaseURL + '\'' +
	               ", idpAuthRequestRedirectURL='" + idpAuthRequestRedirectURL + '\'' +
	               ", idpClientId='" + idpClientId + '\'' +
	               "}";
	    }
	
}
