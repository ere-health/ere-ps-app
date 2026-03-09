package health.ere.ps.model.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.health.service.cetp.beaninfo.BeanInfoHelper;
import de.health.service.cetp.beaninfo.Synthetic;
import de.health.service.cetp.config.KonnektorAuth;
import de.health.service.config.api.IUserConfigurations;
import jakarta.json.JsonObject;
import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
public class UserConfigurations implements IUserConfigurations {

    private static final Logger log = Logger.getLogger(UserConfigurations.class.getName());

    @JsonbProperty(value = "erixa.hotfolder")
    @JsonbNillable
    private String erixaHotfolder;

    @JsonbProperty(value = "erixa.drugstore.email")
    @JsonbNillable
    private String erixaDrugstoreEmail;

    @JsonbProperty(value = "erixa.user.email")
    @JsonbNillable
    private String erixaUserEmail;

    @JsonbProperty(value = "erixa.user.password")
    @JsonbNillable
    private String erixaUserPassword;

    @JsonbProperty(value = "erixa.api.key")
    @JsonbNillable
    private String erixaApiKey;

    @JsonbProperty(value = "extractor.template.profile")
    @JsonbNillable
    private String muster16TemplateProfile;

    @JsonbProperty(value = "connector.base-url")
    @JsonbNillable
    private String connectorBaseURL;

    @JsonbProperty(value = "connector.mandant-id")
    @JsonbNillable
    private String mandantId;

    @JsonbProperty(value = "connector.workplace-id")
    @JsonbNillable
    private String workplaceId;

    @JsonbProperty(value = "connector.client-system-id")
    @JsonbNillable
    private String clientSystemId;

    @JsonbProperty(value = "connector.user-id")
    @JsonbNillable
    private String userId;

    @JsonbProperty(value = "connector.version")
    @JsonbNillable
    private String version;

    @JsonbProperty(value = "connector.tvMode")
    @JsonbNillable
    private String tvMode;

    @JsonbProperty(value = "connector.client-certificate")
    @JsonbNillable
    private String clientCertificate;

    @JsonbProperty(value = "connector.client-certificate-password")
    @JsonbNillable
    private String clientCertificatePassword;

    @JsonbProperty(value = "connector.basic-auth-username")
    @JsonbNillable
    private String basicAuthUsername;

    @JsonbProperty(value = "connector.basic-auth-password")
    @JsonbNillable
    private String basicAuthPassword;

    @JsonbProperty(value = "kbv.pruefnummer")
    @JsonbNillable
    private String pruefnummer;

    /**
     * Property is added to follow IUserConfigurations interface changes, it might be used once
     * the BASIC authentication feature is implemented in the "cetp-implementation" branch
     */
    @JsonbProperty(value = "connector.auth")
    @JsonbNillable
    @JsonIgnore
    private String auth;

    /**
     * Property is added to follow IUserConfigurations interface changes, it might be used once
     * SMC-B cards should be filtered by different iccsn in the "cetp-implementation" branch
     */
    @JsonbProperty(value = "connector.iccsn")
    @JsonbNillable
    @JsonIgnore
    private String iccsn;

    static BeanInfoHelper beanInfoHelper;

    static {
        try {
            beanInfoHelper = new BeanInfoHelper(Introspector.getBeanInfo(UserConfigurations.class));
        } catch (IntrospectionException e) {
            log.log(Level.SEVERE, "Could not process user configurations", e);
        }
    }

    public UserConfigurations() {
    }

    public UserConfigurations(Properties properties) {
        beanInfoHelper.fillValues(this, properties::getProperty);
    }

    public UserConfigurations(JsonObject jsonObject) {
        beanInfoHelper.fillValues(this, (s) -> {
            try {
                Field declaredField = UserConfigurations.class.getDeclaredField(s);
                JsonbProperty annotation = declaredField.getAnnotation(JsonbProperty.class);
                return jsonObject.getString(annotation.value(), null);
            } catch (NoSuchFieldException | SecurityException e) {
                log.log(Level.SEVERE, "Could not read property", e);
                return null;
            }
        });
    }

    public UserConfigurations(HttpServletRequest httpServletRequest) {
        updateWithRequest(httpServletRequest);
    }

    public Properties properties() {
        return beanInfoHelper.properties(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof UserConfigurations userConfigurations) {
            return Objects.equals(erixaHotfolder, userConfigurations.erixaHotfolder)
                && Objects.equals(erixaDrugstoreEmail, userConfigurations.erixaDrugstoreEmail)
                && Objects.equals(erixaUserEmail, userConfigurations.erixaUserEmail)
                && Objects.equals(erixaUserPassword, userConfigurations.erixaUserPassword)
                && Objects.equals(erixaApiKey, userConfigurations.erixaApiKey)
                && Objects.equals(muster16TemplateProfile, userConfigurations.muster16TemplateProfile)
                && Objects.equals(connectorBaseURL, userConfigurations.connectorBaseURL)
                && Objects.equals(mandantId, userConfigurations.mandantId)
                && Objects.equals(workplaceId, userConfigurations.workplaceId)
                && Objects.equals(clientSystemId, userConfigurations.clientSystemId)
                && Objects.equals(userId, userConfigurations.userId)
                && Objects.equals(version, userConfigurations.version)
                && Objects.equals(tvMode, userConfigurations.tvMode)
                && Objects.equals(clientCertificate, userConfigurations.clientCertificate)
                && Objects.equals(clientCertificatePassword, userConfigurations.clientCertificatePassword)
                && Objects.equals(basicAuthUsername, userConfigurations.basicAuthUsername)
                && Objects.equals(basicAuthPassword, userConfigurations.basicAuthPassword)
                && Objects.equals(pruefnummer, userConfigurations.pruefnummer);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(basicAuthPassword, basicAuthUsername, clientCertificate, clientCertificatePassword,
            clientSystemId, connectorBaseURL, erixaApiKey, erixaDrugstoreEmail, erixaHotfolder, erixaUserEmail,
            erixaUserPassword, mandantId, muster16TemplateProfile, pruefnummer, tvMode, userId, version,
            workplaceId);
    }

    @Override
    public String toString() {
        return "UserConfigurations{" +
            "erixaHotfolder='" + erixaHotfolder + '\'' +
            ", erixaDrugstoreEmail='" + erixaDrugstoreEmail + '\'' +
            ", erixaUserEmail='" + erixaUserEmail + '\'' +
            ", erixaUserPassword='" + erixaUserPassword + '\'' +
            ", erixaApiKey='" + erixaApiKey + '\'' +
            ", muster16TemplateProfile='" + muster16TemplateProfile + '\'' +
            ", connectorBaseURL='" + connectorBaseURL + '\'' +
            ", mandantId='" + mandantId + '\'' +
            ", workplaceId='" + workplaceId + '\'' +
            ", clientSystemId='" + clientSystemId + '\'' +
            ", userId='" + userId + '\'' +
            ", version='" + version + '\'' +
            ", tvMode='" + tvMode + '\'' +
            ", clientCertificate='" + clientCertificate + '\'' +
            ", clientCertificatePassword='" + clientCertificatePassword + '\'' +
            ", basicAuthUsername='" + basicAuthUsername + '\'' +
            ", basicAuthPassword='" + basicAuthPassword + '\'' +
            ", pruefnummer='" + pruefnummer + '\'' +
            '}';
    }
}