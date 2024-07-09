package health.ere.ps.service.idp;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.idp.client.IdpClient;
import jakarta.enterprise.event.Event;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class BearerTokenServiceTest {
    private static final ExecutorService pool = Executors.newSingleThreadExecutor();

    @AfterAll
    public static void tearDown() {
        pool.shutdownNow();
    }

    @Test
    void testParallelAccess() throws InterruptedException {
        var mockjwt = "this is a jwt";
        var bearerTokenService = new BearerTokenService(null, null, null, null, null, pool, 5, 6) {
            @Override
            String requestBearerToken(RuntimeConfig runtimeConfig) {
                return mockjwt;
            }
        };

        int parallel = 10;
        var latch = new CountDownLatch(parallel);
        var hasError = new AtomicBoolean();
        var threads = IntStream.range(0, parallel)
                .mapToObj(i -> new Thread(() -> {
                    latch.countDown();
                    try {
                        var runtimeConfig = new RuntimeConfig();
                        runtimeConfig.setEHBAHandle("HBA-1");
                        latch.await(10, TimeUnit.SECONDS);
                        String bearerToken = bearerTokenService.getBearerToken(runtimeConfig);
                        if (!mockjwt.equals(bearerToken)) hasError.set(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        hasError.set(true);
                    }
                })).toList();
        for (var t : threads) {
            t.start();
        }
        latch.await(5, TimeUnit.SECONDS);
        for (var t : threads) {
            long millis = Duration.ofSeconds(10).toMillis();
            t.join(millis);
        }
        assertFalse(hasError.get());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testError() {
        Event<Exception> mockEvent = mock(Event.class);
        var bearerTokenService = new BearerTokenService(null, null, null, null, mockEvent, pool, 4, 5) {
            @Override
            String requestBearerToken(RuntimeConfig runtimeConfig) {
                throw new RuntimeException("This Test is supposed to throw an exception");
            }
        };

        var runtimeConfig = mock(RuntimeConfig.class);
        runtimeConfig.setEHBAHandle("HBA-1");
        assertThrows(RuntimeException.class, () -> bearerTokenService.getBearerToken(runtimeConfig));
        verifyNoInteractions(mockEvent);

        assertThrows(RuntimeException.class, () -> bearerTokenService.getBearerToken(runtimeConfig, null, "ignore"));
        verify(mockEvent, times(1)).fireAsync(notNull());
    }


    @Test
    void testExpiredToken() throws Exception {
        String jwt = createJwt();

        Event<Exception> mockEvent = mock(Event.class);
        var cardCertificateReaderService = mock(CardCertificateReaderService.class);
        var connectorCardsService = mock(ConnectorCardsService.class);
        var idpClient = mock(IdpClient.class);
        IdpTokenResult idpTokenResult = new IdpTokenResult();
        JsonWebToken accessToken = new JsonWebToken(jwt);
        idpTokenResult.setAccessToken(accessToken);
        when(idpClient.login(any(), any())).thenReturn(idpTokenResult);

        var counter = new AtomicInteger();
        var bearerTokenService = new BearerTokenService(null, idpClient, cardCertificateReaderService, connectorCardsService, mockEvent, pool, 60, 70) {
            @Override
            void evictCacheEntryAt(RuntimeConfig runtimeConfig, ZonedDateTime targetTime) {
                counter.incrementAndGet();
            }
        };
        var runtimeConfig = mock(RuntimeConfig.class);
        runtimeConfig.setEHBAHandle("HBA-1");

        var token = bearerTokenService.requestBearerToken(runtimeConfig);

        assertEquals(jwt, token);
        assertEquals(1, counter.get());
    }

    private static String createJwt() throws Exception {
        var claims = new JwtClaims();
        claims.setIssuer("Issuer");
        claims.setAudience("Audience");
        claims.setExpirationTimeMinutesInTheFuture(1);
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(2);
        claims.setSubject("subject");
        claims.setClaim("email", "mail@example.com");
        claims.setStringListClaim("roles", "role-1", "role-2");

        var jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        jws.setKey(new HmacKey(key));
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
        return jws.getCompactSerialization();
    }
}