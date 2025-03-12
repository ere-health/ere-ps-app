package health.ere.ps.service.cardlink;

import de.health.service.cetp.IKonnektorClient;
import de.health.service.cetp.cardlink.JwtConfigurator;
import de.health.service.config.api.UserRuntimeConfig;
import de.health.service.cetp.domain.eventservice.card.Card;
import de.health.service.cetp.domain.eventservice.card.CardType;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.idp.BearerTokenService;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EreJwtConfigurator extends JwtConfigurator {

    private static final Logger log = Logger.getLogger(EreJwtConfigurator.class.getName());
    private static final String NULL_WARNING = "Could not get pharmacyService or connectorServicesProvider, won't add JWT to websocket connection";

    BearerTokenService bearerTokenService;
    IKonnektorClient konnektorClient;

    String smcbHandle = null;
    
    public EreJwtConfigurator(
        UserRuntimeConfig userRuntimeConfig,
        @NotNull(message = NULL_WARNING) IKonnektorClient konnektorClient,
        @NotNull(message = NULL_WARNING) BearerTokenService bearerTokenService
    ) {
        this(userRuntimeConfig, konnektorClient, bearerTokenService, null);
    }

    public EreJwtConfigurator(
        UserRuntimeConfig userRuntimeConfig,
        @NotNull(message = NULL_WARNING) IKonnektorClient konnektorClient,
        @NotNull(message = NULL_WARNING) BearerTokenService bearerTokenService,
        String smcbHandle
    ) {
        super(userRuntimeConfig);
        this.konnektorClient = konnektorClient;
        this.bearerTokenService = bearerTokenService;
        this.smcbHandle = smcbHandle;
    }


    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        RuntimeConfig runtimeConfig = (RuntimeConfig) userRuntimeConfig;
        if(this.smcbHandle != null) {
            log.fine("SMCB handle found from field: " + this.smcbHandle);
            runtimeConfig.setSMCBHandle(this.smcbHandle);
        } else {
            try {
                List<Card> cards = konnektorClient.getCards(userRuntimeConfig, CardType.SMC_B);
                String smcbHandle = cards.stream().map(Card::getCardHandle).findAny().orElse(null);
                log.fine("SMCB handle found from konnektor: " + smcbHandle);
                runtimeConfig.setSMCBHandle(smcbHandle);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Could not get SMC-B for pharmacy", e);
            }
        }
        String bearerToken = bearerTokenService.getBearerToken(runtimeConfig);
        headers.put("Authorization", List.of("Bearer " + bearerToken));
    }
}
