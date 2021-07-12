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

    @JsonbProperty("connector.certificate.file")
    private String connectorCertificateFile;

    @JsonbProperty("connector.certificate.password")
    private String connectorCertificatePassword;

    @JsonbProperty("extractor.template.profile")
    private String muster16Profile;

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

    public String getConnectorCertificateFile() {
        return connectorCertificateFile;
    }

    public void setConnectorCertificateFile(String connectorCertificateFile) {
        this.connectorCertificateFile = connectorCertificateFile;
    }

    public String getConnectorCertificatePassword() {
        return connectorCertificatePassword;
    }

    public void setConnectorCertificatePassword(String connectorCertificatePassword) {
        this.connectorCertificatePassword = connectorCertificatePassword;
    }

    public String getMuster16Profile() {
        return muster16Profile;
    }

    public void setMuster16Profile(String muster16Profile) {
        this.muster16Profile = muster16Profile;
    }

    @Override
    public String toString() {
        return "UserConfigurations{" +
                "erixaHotfolder='" + erixaHotfolder + '\'' +
                ", erixaDrugstoreEmail='" + erixaDrugstoreEmail + '\'' +
                ", erixaUserEmail='" + erixaUserEmail + '\'' +
                ", erixaUserPassword='" + erixaUserPassword + '\'' +
                ", connectorCertificateFile='" + connectorCertificateFile + '\'' +
                ", connectorCertificatePassword='" + connectorCertificatePassword + '\'' +
                ", muster16Profile='" + muster16Profile + '\'' +
                '}';
    }
}
