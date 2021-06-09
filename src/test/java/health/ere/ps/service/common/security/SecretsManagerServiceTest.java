package health.ere.ps.service.common.security;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.logging.Level;

import javax.inject.Inject;

import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.model.idp.crypto.PkiKeyResolver;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@ExtendWith(PkiKeyResolver.class)
class SecretsManagerServiceTest {
    @Inject
    Logger log;

    @Inject
    SecretsManagerService secretsManagerService;

    static char[] trustStorePassword;
    static final String TITUS_IDP_TRUST_STORE = "ps_erp_incentergy_01.p12";

    Path tempTrustStoreFile;

    @BeforeAll
    public static void init() {
        trustStorePassword = "password1".toCharArray();
    }
    
    @BeforeEach
    void setUp() throws IOException {
        tempTrustStoreFile = Files.createTempFile("temp-truststore", ".dat");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempTrustStoreFile);
    }

    @Test
    void createTrustStore()
            throws SecretsManagerException {
        KeyStore ks =
                secretsManagerService.createTrustStore(tempTrustStoreFile.toFile().getAbsolutePath(),
                SecretsManagerService.KeyStoreType.PKCS12, trustStorePassword);
        assertTrue(ks.getType().equalsIgnoreCase(
                SecretsManagerService.KeyStoreType.PKCS12.getKeyStoreType()));
    }

    @Test
    void saveTrustedCertificate(
            @PkiKeyResolver.Filename(TITUS_IDP_TRUST_STORE)
            final PkiIdentity titusIdpIdentity) throws CertificateException, KeyStoreException,
            IOException, NoSuchAlgorithmException, SecretsManagerException {
        //KeyStore must already exist which would be the case in a production environment.
        secretsManagerService.createTrustStore(tempTrustStoreFile.toFile().getAbsolutePath(),
                SecretsManagerService.KeyStoreType.PKCS12, trustStorePassword);
        
        String certificateAlias = "testCert1";
        String keyAlias = "testKey1";
        KeyStore ks = secretsManagerService.saveTrustedCertificate(
                tempTrustStoreFile.toFile().getAbsolutePath(), trustStorePassword,
                certificateAlias, titusIdpIdentity.getCertificate());

        assertTrue(ks.containsAlias(certificateAlias));
        assertTrue(ks.isCertificateEntry(certificateAlias));
    }

    @Test
    void initializeTrustStoreFromFile() throws IOException, CertificateException,
            KeyStoreException, NoSuchAlgorithmException {
        int ksSize;

        try(InputStream is = getClass().getResourceAsStream("/certs/" + TITUS_IDP_TRUST_STORE)) {
            KeyStore ks =
                    secretsManagerService.initializeTrustStoreFromInputStream(is,
                            SecretsManagerService.KeyStoreType.PKCS12, "00".toCharArray());
            ksSize = ks.size();
        }

        assertTrue(ksSize > 0);
    }

    @Disabled
    @Test
    void createSSLContext() {
        Assertions.fail("Implement me");
    }

    @Test
    void test_Read_Base64_Cert() throws IOException, CertificateException {
//        String certB64 = "MIIHFDCCBfygAwIBAgIIK2o4sL7KHQgwDQYJKoZIhvcNAQELBQAwSTELMAkGA1UEBhMCVVMxEzARBgNVBAoTCkdvb2dsZSBJbmMxJTAjBgNVBAMTHEdvb2dsZSBJbnRlcm5ldCBBdXRob3JpdHkgRzIwHhcNMTYxMjE1MTQwNDE1WhcNMTcwMzA5MTMzNTAwWjBmMQswCQYDVQQGEwJVUzETMBEGA1UECAwKQ2FsaWZvcm5pYTEWMBQGA1UEBwwNTW91bnRhaW4gVmlldzETMBEGA1UECgwKR29vZ2xlIEluYzEVMBMGA1UEAwwMKi5nb29nbGUuY29tMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEG1y99TYpFSSiawnjJKYI8hyEzJ4M+IELfLjmSsYI7fW/V8AT61quCswtBMikJYqzYBZrV2Reu5sHlLr6936cR6OCBKwwggSoMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjCCA2sGA1UdEQSCA2IwggNeggwqLmdvb2dsZS5jb22CDSouYW5kcm9pZC5jb22CFiouYXBwZW5naW5lLmdvb2dsZS5jb22CEiouY2xvdWQuZ29vZ2xlLmNvbYIWKi5nb29nbGUtYW5hbHl0aWNzLmNvbYILKi5nb29nbGUuY2GCCyouZ29vZ2xlLmNsgg4qLmdvb2dsZS5jby5pboIOKi5nb29nbGUuY28uanCCDiouZ29vZ2xlLmNvLnVrgg8qLmdvb2dsZS5jb20uYXKCDyouZ29vZ2xlLmNvbS5hdYIPKi5nb29nbGUuY29tLmJygg8qLmdvb2dsZS5jb20uY2+CDyouZ29vZ2xlLmNvbS5teIIPKi5nb29nbGUuY29tLnRygg8qLmdvb2dsZS5jb20udm6CCyouZ29vZ2xlLmRlggsqLmdvb2dsZS5lc4ILKi5nb29nbGUuZnKCCyouZ29vZ2xlLmh1ggsqLmdvb2dsZS5pdIILKi5nb29nbGUubmyCCyouZ29vZ2xlLnBsggsqLmdvb2dsZS5wdIISKi5nb29nbGVhZGFwaXMuY29tgg8qLmdvb2dsZWFwaXMuY26CFCouZ29vZ2xlY29tbWVyY2UuY29tghEqLmdvb2dsZXZpZGVvLmNvbYIMKi5nc3RhdGljLmNugg0qLmdzdGF0aWMuY29tggoqLmd2dDEuY29tggoqLmd2dDIuY29tghQqLm1ldHJpYy5nc3RhdGljLmNvbYIMKi51cmNoaW4uY29tghAqLnVybC5nb29nbGUuY29tghYqLnlvdXR1YmUtbm9jb29raWUuY29tgg0qLnlvdXR1YmUuY29tghYqLnlvdXR1YmVlZHVjYXRpb24uY29tggsqLnl0aW1nLmNvbYIaYW5kcm9pZC5jbGllbnRzLmdvb2dsZS5jb22CC2FuZHJvaWQuY29tghtkZXZlbG9wZXIuYW5kcm9pZC5nb29nbGUuY26CBGcuY2+CBmdvby5nbIIUZ29vZ2xlLWFuYWx5dGljcy5jb22CCmdvb2dsZS5jb22CEmdvb2dsZWNvbW1lcmNlLmNvbYIKdXJjaGluLmNvbYIKd3d3Lmdvby5nbIIIeW91dHUuYmWCC3lvdXR1YmUuY29tghR5b3V0dWJlZWR1Y2F0aW9uLmNvbTALBgNVHQ8EBAMCB4AwaAYIKwYBBQUHAQEEXDBaMCsGCCsGAQUFBzAChh9odHRwOi8vcGtpLmdvb2dsZS5jb20vR0lBRzIuY3J0MCsGCCsGAQUFBzABhh9odHRwOi8vY2xpZW50czEuZ29vZ2xlLmNvbS9vY3NwMB0GA1UdDgQWBBThPf/3oDfxFM/hdOi5kLv8qrZbsjAMBgNVHRMBAf8EAjAAMB8GA1UdIwQYMBaAFErdBhYbvPZotXb1gba7Yhq6WoEvMCEGA1UdIAQaMBgwDAYKKwYBBAHWeQIFATAIBgZngQwBAgIwMAYDVR0fBCkwJzAloCOgIYYfaHR0cDovL3BraS5nb29nbGUuY29tL0dJQUcyLmNybDANBgkqhkiG9w0BAQsFAAOCAQEAWZQy0Kvn9cPnIh7Z4kfUCXX/dhdvjLJYFAn3b3d5DVs1BLYuukfIjilVdAeTUHZH7TLn/uVejg3yS0ssRg1ds1iv2O9DJbnl5FHcjNAvwfN533FulWP41OC6B6dC6BGGTXTvQobDup7/EKg1GWX9ksBtTfKLH5wrjhN955Itnd25Sjw2bSjLaWEtTrjINXmnBoc2+qHFzF/fNxK1KbmkBboUIGoaGsThe3AF0Ye+XAeaZH08+GdrorknlHDQLLtHIcJ3C6PrQ/kTpwWd/TVXW42BN+N7xZiGJbvKOg0S0rk2hzhgX4QoUKZHMqqh1sS6ypkfnWx75nh325y4Tenk+A==";
        String certB64 = "MIIE+jCCA+KgAwIBAgIHAgHpEyMBmzANBgkqhkiG9w0BAQsFADCBmjELMAkGA1UEBhMCREUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxSDBGBgNVBAsMP0luc3RpdHV0aW9uIGRlcyBHZXN1bmRoZWl0c3dlc2Vucy1DQSBkZXIgVGVsZW1hdGlraW5mcmFzdHJ1a3R1cjEgMB4GA1UEAwwXR0VNLlNNQ0ItQ0EyNCBURVNULU9OTFkwHhcNMjAxMTI3MDAwMDAwWhcNMjMxMTE2MDAwMDAwWjCBhDELMAkGA1UEBhMCREUxHDAaBgNVBAoMEzIwMjExMDEyMiBOT1QtVkFMSUQxEjAQBgNVBAQMCURyb21idXNjaDERMA8GA1UEKgwIV2FsdHJhdXQxMDAuBgNVBAMMJ0FyenRwcmF4aXMgV2FsdHJhdXQgRHJvbWJ1c2NoIFRFU1QtT05MWTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMEI0VNeP25qjKue36MFmSWMtZsxv197JdhxV75Q4AMq/FK67yqku8sQF9OmhYloaeuziNkNSTHd+8fAIqsbSxQYRwobOhweciOBknbVIALJRtAkYPJJBxahh4zHPLbEzDLWYhnVV7Wy60d4wZF/f94cHm9qBlhWiDmqXCObUv+alscCxST2Ll5MFCe4Iz2CNP+5LzX4jfBnGVWYooQJKwKPKpIhyAFUHywHuaZ/KfpIHPlyXq8vuMJ5ZCPDfW7n8GFRB2Dx1MGOWiJMa63f5DBjF0ozZSxVyof3A4z22sJ+YG0jYfuyhVRwsSciycWFxh9CwtcNkfzfhQG3AHa5Z8ECAwEAAaOCAVcwggFTMAwGA1UdEwEB/wQCMAAwHwYDVR0jBBgwFoAUeunhb+oUWRYF7gPp0/0hq97p2Z4wHQYDVR0OBBYEFJFDx5Q9Gonx3vTQ/Mg/vb4FVsXLMCwGA1UdHwQlMCMwIaAfoB2GG2h0dHA6Ly9laGNhLmdlbWF0aWsuZGUvY3JsLzA4BggrBgEFBQcBAQQsMCowKAYIKwYBBQUHMAGGHGh0dHA6Ly9laGNhLmdlbWF0aWsuZGUvb2NzcC8wEwYDVR0lBAwwCgYIKwYBBQUHAwIwVAYFKyQIAwMESzBJMEcwRTBDMEEwFgwUQmV0cmllYnNzdMOkdHRlIEFyenQwCQYHKoIUAEwEMhMcMS0yLUFSWlQtV2FsdHJhdXREcm9tYnVzY2gwMTAOBgNVHQ8BAf8EBAMCBaAwIAYDVR0gBBkwFzAKBggqghQATASBIzAJBgcqghQATARNMA0GCSqGSIb3DQEBCwUAA4IBAQBNVJKRDnooSI29jIG5O0G2nZbB82oigJh0FUgBwV1qOnbLhtx4+9u2QyBMI8ZZ+CUwxbGVRDMXnE5ONclgjAeolizhAFD1nAD91UhnXZJQZ84ZhkDVc2tulyVWoYiP3on/x0LD7hZlNhhAJzov+6cV4QbzIWahWMaBiWk3itKUwE7tu/jmS7+Y8UGbOo3L4A71F92CuJS2GIltySC3IdSYSKGW3I1O2/fkgTBlkv1Gv6ez3oNW90L4zrgUZ3Fr4XAzgegx95rn0b7TTt/N/xHBIWtSNDFS5gx1H4JREvQxF1I3sjxF9CvIeCTL6ZdS4FuV163cWL0Nu++tIe9GO+JJ";
        byte encodedCert[] = Base64.getDecoder().decode(certB64);
        try(ByteArrayInputStream inputStream  =  new ByteArrayInputStream(encodedCert)) {

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

            log.info("x509 cert = " + certFactory.generateCertificate(inputStream));
        }
    }
}