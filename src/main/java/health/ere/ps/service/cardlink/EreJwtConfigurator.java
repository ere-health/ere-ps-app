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

    public EreJwtConfigurator(
        UserRuntimeConfig userRuntimeConfig,
        @NotNull(message = NULL_WARNING) IKonnektorClient konnektorClient,
        @NotNull(message = NULL_WARNING) BearerTokenService bearerTokenService
    ) {
        super(userRuntimeConfig);
        this.konnektorClient = konnektorClient;
        this.bearerTokenService = bearerTokenService;
    }


    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        String bearerToken = bearerTokenService.getBearerToken((RuntimeConfig) userRuntimeConfig);
        headers.put("Authorization", List.of("Bearer " + bearerToken));
    }
}
