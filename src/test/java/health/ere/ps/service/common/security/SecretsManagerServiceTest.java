package health.ere.ps.service.common.security;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.model.idp.crypto.PkiKeyResolver;
import health.ere.ps.profile.TitusTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
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
}