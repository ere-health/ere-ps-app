package health.ere.ps.service.cetp;

import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.v7.GetSubscription;
import de.gematik.ws.conn.eventservice.v7.GetSubscriptionResponse;
import de.gematik.ws.conn.eventservice.v7.RenewSubscriptionsResponse;
import de.gematik.ws.conn.eventservice.v7.SubscriptionRenewal;
import de.gematik.ws.conn.eventservice.v7.SubscriptionType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage;
import de.health.service.cetp.IKonnektorClient;
import de.health.service.cetp.config.UserRuntimeConfig;
import de.health.service.cetp.domain.CetpStatus;
import de.health.service.cetp.domain.SubscriptionResult;
import de.health.service.cetp.domain.eventservice.Subscription;
import de.health.service.cetp.domain.fault.CetpFault;
import health.ere.ps.service.cetp.mapper.StatusMapper;
import health.ere.ps.service.cetp.mapper.SubscriptionMapper;
import health.ere.ps.service.cetp.mapper.SubscriptionResultMapper;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.xml.ws.Holder;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import static de.health.service.cetp.SubscriptionManager.FAILED;

@ApplicationScoped
public class KonnektorClient implements IKonnektorClient {

    private final Object emptyInput = new Object();

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;

    @Inject
    SubscriptionResultMapper subscriptionResultMapper;

    @Inject
    SubscriptionMapper subscriptionMapper;

    @Inject
    StatusMapper statusMapper;

    @Override
    public List<Subscription> getSubscriptions(UserRuntimeConfig runtimeConfig) throws CetpFault {
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
        EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);
        GetSubscription getSubscriptionRequest = new GetSubscription();
        getSubscriptionRequest.setContext(context);
        getSubscriptionRequest.setMandantWide(false);
        try {
            GetSubscriptionResponse subscriptionResponse = eventService.getSubscription(getSubscriptionRequest);
            return subscriptionResponse.getSubscriptions().getSubscription()
                .stream().map(subscriptionMapper::toDomain)
                .collect(Collectors.toList());
        } catch (FaultMessage faultMessage) {
            throw new CetpFault(faultMessage.getMessage());
        }
    }

    @Override
    public SubscriptionResult renewSubscription(UserRuntimeConfig runtimeConfig, String subscriptionId) throws CetpFault {
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
        EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);

        Holder<Status> statusHolder = new Holder<>();
        Holder<RenewSubscriptionsResponse.SubscribeRenewals> renewalHolder = new Holder<>();
        List<String> subscriptions = List.of(subscriptionId);
        try {
            eventService.renewSubscriptions(context, subscriptions, statusHolder, renewalHolder);
            SubscriptionRenewal renewal = renewalHolder.value.getSubscriptionRenewal().get(0);
            return subscriptionResultMapper.toDomain(renewal, statusHolder);
        } catch (FaultMessage faultMessage) {
            throw new CetpFault(faultMessage.getMessage());
        }
    }

    @Override
    public SubscriptionResult subscribe(UserRuntimeConfig runtimeConfig, String cetpHost) throws CetpFault {
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);

        EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);
        SubscriptionType subscriptionType = new SubscriptionType();

        subscriptionType.setEventTo(cetpHost);
        subscriptionType.setTopic(CARD_INSERTED_TOPIC);
        Holder<Status> statusHolder = new Holder<>();
        Holder<String> subscriptionId = new Holder<>();
        Holder<XMLGregorianCalendar> terminationTime = new Holder<>();
        try {
            eventService.subscribe(context, subscriptionType, statusHolder, subscriptionId, terminationTime);
            return subscriptionResultMapper.toDomain(emptyInput, statusHolder, subscriptionId, terminationTime);
        } catch (FaultMessage faultMessage) {
            throw new CetpFault(faultMessage.getMessage());
        }
    }

    @Override
    public CetpStatus unsubscribe(
        UserRuntimeConfig runtimeConfig,
        String subscriptionId,
        String cetpHost,
        boolean forceCetp
    ) throws CetpFault {
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
        EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);
        try {
            if (forceCetp) {
                return statusMapper.toDomain(eventService.unsubscribe(context, null, cetpHost));
            } else {
                if (subscriptionId == null || subscriptionId.startsWith(FAILED)) {
                    CetpStatus status = new CetpStatus();
                    status.setResult("Previous subscription is not found");
                    return status;
                }
                return statusMapper.toDomain(eventService.unsubscribe(context, subscriptionId, null));
            }
        } catch (FaultMessage faultMessage) {
            throw new CetpFault(faultMessage.getMessage());
        }
    }
}
