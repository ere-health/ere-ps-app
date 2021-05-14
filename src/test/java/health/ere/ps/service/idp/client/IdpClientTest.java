package health.ere.ps.service.idp.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.idp.tests.PkiKeyResolver;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@ExtendWith(PkiKeyResolver.class)
public class IdpClientTest {
    @Inject
    IdpClient idpClient;

    private PkiIdentity smc_bPkIdentity;
    private AuthenticatorClient authenticatorClient;

    @BeforeEach
    public void init(
            @PkiKeyResolver.Filename("109500969_X114428530_c.ch.aut-ecc")
            final PkiIdentity smc_bPkIdentity) {

        this.smc_bPkIdentity = smc_bPkIdentity;
        idpClient.initialize();
    }

    @Test
    public void test_Successful_Idp_Login() {
        IdpTokenResult idpTokenResult = idpClient.login(smc_bPkIdentity);

        Assertions.assertNotNull(idpTokenResult);
    }

}
