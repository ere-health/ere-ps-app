package health.ere.ps.service.gematik;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.service.common.security.SSLSocketFactory;
import health.ere.ps.service.common.security.SecretsManagerService.KeyStoreType;
import health.ere.ps.service.common.security.SecretsManagerService.SslContextType;
import health.ere.ps.service.connector.endpoint.SSLUtilities;
import jakarta.json.bind.JsonbBuilder;

public class KIMFlowtype169ServiceTest {

    @Test
    @Disabled
    public void testSendERezeptToKIMAddress() {
      String fromKimAddress = "incentergy_test_02@arv.kim.telematik-test";
      String toKimAddress = "dsl5@arv.kim.telematik-test";
      String smtpHostServer = "localhost";
      String smtpUser = "incentergy_test_02@arv.kim.telematik-test#10.30.8.6:465#Incentergy#Incentergy#1786_A1";
      String smtpPassword = "z6g9ewzmSURGfP2";
      String eRezeptToken = "Task/4711/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea";

      KIMFlowtype169Service kIMFlowtype169Service = new KIMFlowtype169Service();
      kIMFlowtype169Service.sendERezeptToKIMAddress(fromKimAddress, toKimAddress, "Hallo Apotheke", smtpHostServer, smtpUser, smtpPassword, eRezeptToken);
    }

    @Test
    @Disabled
    public void testSearchSee() throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {

      String searchDisplayName = "See";
      search(searchDisplayName);
    }

    @Test
    @Disabled
    public void testSearchTest() throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {

      String searchDisplayName = "test";
      search(searchDisplayName);
    }

    @Test
    public void testCreatePropertiesTest() {
      Properties prop = KIMFlowtype169Service.createProperties("localhost");
      assertEquals("localhost", prop.getProperty("mail.smtp.host"));
      assertNull(prop.getProperty("mail.smtp.port"));

      prop = KIMFlowtype169Service.createProperties("localhost:8025");
      assertEquals("localhost", prop.getProperty("mail.smtp.host"));
      assertEquals("8025", prop.getProperty("mail.smtp.port"));
      
      prop = KIMFlowtype169Service.createProperties("smtp://localhost");
      assertEquals("localhost", prop.getProperty("mail.smtp.host"));
      assertNull(prop.getProperty("mail.smtp.port"));
      
      prop = KIMFlowtype169Service.createProperties("smtps://localhost");
      assertEquals("localhost", prop.getProperty("mail.smtp.host"));
      assertNull(prop.getProperty("mail.smtp.port"));
      prop = KIMFlowtype169Service.createProperties("smtp://localhost:8025");
      assertEquals("localhost", prop.getProperty("mail.smtp.host"));
      assertEquals("8025", prop.getProperty("mail.smtp.port"));
      prop = KIMFlowtype169Service.createProperties("smtps://localhost:8025");
      assertEquals("localhost", prop.getProperty("mail.smtp.host"));
      assertEquals("8025", prop.getProperty("mail.smtp.port"));
      assertEquals("*", prop.getProperty("mail.smtp.ssl.trust"));
      
    }


    private void search(String searchDisplayName) throws FileNotFoundException, NoSuchAlgorithmException,
        KeyStoreException, IOException, CertificateException, UnrecoverableKeyException, KeyManagementException {
      String connectorTlsCertAuthStorePwd = "N4rouwibGRhne2Fa";
      FileInputStream certificateInputStream = new FileInputStream("/home/manuel/Desktop/RU-Connector-Cert/no_ec_incentergy.p12");

      SSLContext sslContext = SSLContext.getInstance(SslContextType.TLS.getSslContextType());

      KeyStore ks = KeyStore.getInstance(KeyStoreType.PKCS12.getKeyStoreType());
      ks.load(certificateInputStream, connectorTlsCertAuthStorePwd.toCharArray());

      KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(ks, connectorTlsCertAuthStorePwd.toCharArray());

      sslContext.init(kmf.getKeyManagers(), new TrustManager[]{new SSLUtilities.FakeX509TrustManager()},
              null);

      SSLSocketFactory.delegate = sslContext.getSocketFactory();

      KIMFlowtype169Service kIMFlowtype169Service = new KIMFlowtype169Service();
      kIMFlowtype169Service.disableEndpointIdentification();
      RuntimeConfig runtimeConfig = new RuntimeConfig();
      UserConfigurations configurations = new UserConfigurations();
      configurations.setConnectorBaseURL("https://10.0.0.98:443/");
      runtimeConfig.updateProperties(configurations);
      List<Map<String,Object>> list = kIMFlowtype169Service.search(runtimeConfig, searchDisplayName);
      System.out.println(JsonbBuilder.create().toJson(list));
    }
}
