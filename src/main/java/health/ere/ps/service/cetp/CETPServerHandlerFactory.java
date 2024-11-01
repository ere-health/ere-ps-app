package health.ere.ps.service.cetp;

import de.health.service.cetp.CETPEventHandlerFactory;
import de.health.service.cetp.IKonnektorClient;
import de.health.service.cetp.cardlink.CardlinkWebsocketClient;
import de.health.service.cetp.config.KonnektorConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.cardlink.EreJwtConfigurator;
import health.ere.ps.service.cetp.tracker.TrackerService;
import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.gematik.PharmacyService;
import health.ere.ps.service.health.check.CardlinkWebsocketCheck;
import health.ere.ps.service.idp.BearerTokenService;
import io.netty.channel.ChannelInboundHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CETPServerHandlerFactory implements CETPEventHandlerFactory {

    TrackerService trackerService;
    PharmacyService pharmacyService;
    IKonnektorClient konnektorClient;
    BearerTokenService bearerTokenService;
    SecretsManagerService secretsManagerService;
    CardlinkWebsocketCheck cardlinkWebsocketCheck;

    @Inject
    public CETPServerHandlerFactory(
        TrackerService trackerService,
        PharmacyService pharmacyService,
        IKonnektorClient konnektorClient,
        BearerTokenService bearerTokenService,
        SecretsManagerService secretsManagerService,
        CardlinkWebsocketCheck cardlinkWebsocketCheck
    ) {
        this.trackerService = trackerService;
        this.konnektorClient = konnektorClient;
        this.pharmacyService = pharmacyService;
        this.bearerTokenService = bearerTokenService;
        this.cardlinkWebsocketCheck = cardlinkWebsocketCheck;
        this.secretsManagerService = secretsManagerService;
    }

    @Override
    public ChannelInboundHandler[] build(KonnektorConfig kc) {
        CardlinkWebsocketClient cardlinkWebsocketClient = new CardlinkWebsocketClient(
            kc.getCardlinkEndpoint(),
            new EreJwtConfigurator(
                new RuntimeConfig(kc.getUserConfigurations()),
                konnektorClient,
                bearerTokenService
            )
        );
        cardlinkWebsocketCheck.register(
            kc.getCardlinkEndpoint(),
            cardlinkWebsocketClient.connected()
        );
        return new ChannelInboundHandler[] {
            new CETPServerHandler(trackerService, pharmacyService, cardlinkWebsocketClient)
        };
    }
}
