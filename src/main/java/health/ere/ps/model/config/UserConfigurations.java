package health.ere.ps.model.config;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.json.JsonObject;
import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.servlet.http.HttpServletRequest;

public class UserConfigurations {

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

    static BeanInfo beanInfo;

    static {
        try {
            beanInfo = Introspector.getBeanInfo(UserConfigurations.class);
        } catch (IntrospectionException e) {
            log.log(Level.SEVERE, "Could not process user configurations", e);
        }
    }

    public UserConfigurations() {
    }

    public UserConfigurations(Properties properties) {
        fillValues(properties::getProperty);
    }

    public UserConfigurations(JsonObject jsonObject) {
        fillValues((s) -> {
            try {
                return jsonObject.getString(UserConfigurations.class.getDeclaredField(s).getAnnotation(JsonbProperty.class).value(), null);
            } catch (NoSuchFieldException | SecurityException e) {
                log.log(Level.SEVERE, "Could not read property", e);
                return null;
            }
        });
    }

    public UserConfigurations(HttpServletRequest httpServletRequest) {
        updateWithRequest(httpServletRequest);
    }

    public UserConfigurations updateWithRequest(HttpServletRequest httpServletRequest) {
        Enumeration<String> enumeration = httpServletRequest.getHeaderNames();
        List<String> list = Collections.list(enumeration);
        for (String headerName : list) {
            if (headerName.startsWith("X-") && !"X-eHBAHandle".equals(headerName) && !"X-SMCBHandle".equals(headerName) && !"X-sendPreview".equals(headerName)) {
                String propertyName = headerName.substring(2);
                Field field;
                try {
                    field = UserConfigurations.class.getDeclaredField(propertyName);
                    if (field != null) {
                        field.set(this, httpServletRequest.getHeader(headerName));
                    }
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException |
                         IllegalAccessException e) {
                    log.log(Level.WARNING, "Could not extract values from header", e);
                }
            }
        }
        return this;
    }

    private void fillValues(Function<String, Object> getValue) {
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            try {
                Method writeMethod = pd.getWriteMethod();
                if (writeMethod != null) {
                    writeMethod.invoke(this, getValue.apply(pd.getName()));
                } else {
                    if (!"class".equals(pd.getName())) {
                        log.warning("No write method for: " + pd.getName());
                    }
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "Could not process user configurations", e);
            }
        }
    }

    public Properties properties() {
        Properties properties = new Properties();
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            try {
                if (pd.getReadMethod().invoke(this) instanceof String str) {
                    properties.setProperty(pd.getName(), str);
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "Could not process user configurations", e);
            }
        }
        return properties;
    }

    public static BeanInfo getBeanInfo() {
        return beanInfo;
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

    public String getClientCertificate() {
        return this.clientCertificate;
    }

    public void setClientCertificate(String clientCertificate) {
        this.clientCertificate = clientCertificate;
    }

    public String getClientCertificatePassword() {
        return this.clientCertificatePassword;
    }

    public void setClientCertificatePassword(String clientCertificatePassword) {
        this.clientCertificatePassword = clientCertificatePassword;
    }

    public String getBasicAuthUsername() {
        return this.basicAuthUsername;
    }

    public void setBasicAuthUsername(String basicAuthUsername) {
        this.basicAuthUsername = basicAuthUsername;
    }

    public String getBasicAuthPassword() {
        return this.basicAuthPassword;
    }

    public void setBasicAuthPassword(String basicAuthPassword) {
        this.basicAuthPassword = basicAuthPassword;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getErixaApiKey() {
        return this.erixaApiKey;
    }

    public void setErixaApiKey(String erixaApiKey) {
        this.erixaApiKey = erixaApiKey;
    }

    public String getPruefnummer() {
        return this.pruefnummer;
    }

    public void setPruefnummer(String pruefnummer) {
        this.pruefnummer = pruefnummer;
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