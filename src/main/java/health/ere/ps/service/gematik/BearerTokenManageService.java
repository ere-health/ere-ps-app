package health.ere.ps.service.gematik;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.idp.BearerTokenService;
import jakarta.inject.Inject;
import jakarta.websocket.Session;

public class BearerTokenManageService {

    private static final Logger log = Logger.getLogger(BearerTokenManageService.class.getName());

    @Inject
    BearerTokenService bearerTokenService;

    //In the future it should be managed automatically by the webclient, including its renewal
    Map<RuntimeConfig, String> bearerToken = new HashMap<>();


    public void setBearerToken(String bearerToken) {
        this.bearerToken.put(null, bearerToken);
    }

    public void requestNewAccessTokenIfNecessary() {
        requestNewAccessTokenIfNecessary(null, null, null);
    }

    /**
     * Requests a new userConfig if the current one is expired
     */
    public void requestNewAccessTokenIfNecessary(RuntimeConfig runtimeConfig, Session replyTo, String replyToMessageId) {
        if (StringUtils.isEmpty(getBearerToken(runtimeConfig)) || isExpired(bearerToken.get(runtimeConfig))) {
            log.info("Request new bearer token.");
            String bearerTokenString = bearerTokenService.requestBearerToken(runtimeConfig, replyTo, replyToMessageId);
            bearerToken.put(runtimeConfig, bearerTokenString);
        }
    }

    public String getBearerToken() {
        return bearerToken.get(null);
    }

    public String getBearerToken(RuntimeConfig runtimeConfig) {
        return bearerToken.get(runtimeConfig);
    }

    /**
     * Checks if the given bearer token is expired.
     * @param bearerToken2 the bearer token to check
     */
    static boolean isExpired(String bearerToken2) {
        JwtConsumer consumer = new JwtConsumerBuilder()
                .setDisableRequireSignature()
                .setSkipSignatureVerification()
                .setSkipDefaultAudienceValidation()
                .setRequireExpirationTime()
                .build();
        try {
            consumer.process(bearerToken2);
            return false;
        } catch (InvalidJwtException e) {
            return true;
        }
    }
}
