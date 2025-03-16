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
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EreJwtConfigurator extends JwtConfigurator {

    private static final Logger log = Logger.getLogger(EreJwtConfigurator.class.getName());
    private static final String NULL_WARNING = "Could not get pharmacyService or connectorServicesProvider, won't add JWT to websocket connection";

    BearerTokenService bearerTokenService;
    IKonnektorClient konnektorClient;
    Consumer<Exception> onException = null;

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
        Consumer<Exception> onException
    ) {
        super(userRuntimeConfig);
        this.konnektorClient = konnektorClient;
        this.bearerTokenService = bearerTokenService;
        this.onException = onException;
    }


    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        try {
            String bearerToken = bearerTokenService.getBearerToken((RuntimeConfig) userRuntimeConfig);
            headers.put("Authorization", List.of("Bearer " + bearerToken));
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not get bearer token", e);
            if(onException != null) {
                onException.accept(e);
            }
        }
    }
}
