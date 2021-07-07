package health.ere.ps.config;


import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CertConfig {

    @ConfigProperty(name = "connector.cert.auth.store.file")
    String connectorCertAuthStoreFile;

    @ConfigProperty(name = "connector.cert.auth.store.file.password")
    String connectorCertAuthStoreFilePwd;

    public String getConnectorCertAuthStoreFile() {
        return !connectorCertAuthStoreFile.equals("!") ? connectorCertAuthStoreFile : getDefaultCertificateFile();
    }

    public String getConnectorCertAuthStoreFilePwd() {
        return StringUtils.defaultString(connectorCertAuthStoreFilePwd).trim();
    }

    private String getDefaultCertificateFile() {
        return getClass().getResource("/certs/ps_erp_incentergy_01.p12").getFile();
    }
}
