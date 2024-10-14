package health.ere.ps.service.cetp;

import de.health.service.cetp.CETPEventHandlerFactory;
import de.health.service.cetp.konnektorconfig.KonnektorConfig;
import health.ere.ps.service.cardlink.CardlinkWebsocketClient;
import health.ere.ps.service.cetp.tracker.TrackerService;
import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.gematik.PharmacyService;
import health.ere.ps.service.health.check.CardlinkWebsocketCheck;
import io.netty.channel.ChannelInboundHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CETPServerHandlerFactory implements CETPEventHandlerFactory {

    TrackerService trackerService;
    PharmacyService pharmacyService;
    SecretsManagerService secretsManagerService;
    CardlinkWebsocketCheck cardlinkWebsocketCheck;

    @Inject
    public CETPServerHandlerFactory(
        TrackerService trackerService,
        PharmacyService pharmacyService,
        SecretsManagerService secretsManagerService,
        CardlinkWebsocketCheck cardlinkWebsocketCheck
    ) {
        this.trackerService = trackerService;
        this.pharmacyService = pharmacyService;
        this.secretsManagerService = secretsManagerService;
        this.cardlinkWebsocketCheck = cardlinkWebsocketCheck;
    }

    @Override
    public ChannelInboundHandler build(KonnektorConfig konnektorConfig) {
        CardlinkWebsocketClient cardlinkWebsocketClient = new CardlinkWebsocketClient(
            konnektorConfig.getCardlinkEndpoint(),
            cardlinkWebsocketCheck
        );
        return new CETPServerHandler(trackerService, pharmacyService, cardlinkWebsocketClient);
    }
}
