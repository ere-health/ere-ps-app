package health.ere.ps.service.idp.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.MalformedURLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import health.ere.ps.model.idp.client.DiscoveryDocumentResponse;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.idp.tests.PkiKeyResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(PkiKeyResolver.class)
public class IdpClientTest {
    private IdpClient idpClient;
    private AuthenticatorClient authenticatorClient;

    @Disabled
    @BeforeEach
    public void init(final PkiIdentity ecc){
        authenticatorClient = mock(AuthenticatorClient.class);
        doReturn(DiscoveryDocumentResponse.builder()
                .authorizationEndpoint("fdsa")
                .idpSig(ecc.getCertificate())
                .tokenEndpoint("fdsafds")
                .build())
                .when(authenticatorClient)
                .retrieveDiscoveryDocument(anyString());

        doAnswer(call -> ((Function) call.getArguments()[1]).apply(null))
                .when(authenticatorClient)
                .doAuthorizationRequest(any());

        idpClient = IdpClient.builder()
                .discoveryDocumentUrl("fjnkdslaö")
                .authenticatorClient(authenticatorClient)
                .build();

        idpClient.initialize();
    }

    @Disabled
    @Test
    public void testBeforeCallback(final PkiIdentity ecc) {
        final AtomicInteger callCounter = new AtomicInteger(0);
        idpClient.setBeforeAuthorizationCallback(r -> callCounter.incrementAndGet());

        try {
            idpClient.login(ecc);
        } catch (final RuntimeException e) {
            //swallow
        }

        assertEquals(1, callCounter.get());
    }

    @Disabled
    @Test
    public void testBeforeFunction(final PkiIdentity ecc) {
        final AtomicInteger callCounter = new AtomicInteger(0);
        idpClient.setBeforeAuthorizationMapper(r -> {
            callCounter.incrementAndGet();
            return r;
        });

        try {
            idpClient.login(ecc);
        } catch (final RuntimeException e) {
            //swallow
        }

        assertEquals(1, callCounter.get());
    }
}
