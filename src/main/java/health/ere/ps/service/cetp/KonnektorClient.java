package health.ere.ps.service.cetp;

import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.v7.GetCards;
import de.gematik.ws.conn.eventservice.v7.GetSubscription;
import de.gematik.ws.conn.eventservice.v7.GetSubscriptionResponse;
import de.gematik.ws.conn.eventservice.v7.RenewSubscriptionsResponse;
import de.gematik.ws.conn.eventservice.v7.SubscriptionRenewal;
import de.gematik.ws.conn.eventservice.v7.SubscriptionType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage;
import de.health.service.cetp.CertificateInfo;
import de.health.service.cetp.IKonnektorClient;
import de.health.service.cetp.domain.CetpStatus;
import de.health.service.cetp.domain.SubscriptionResult;
import de.health.service.cetp.domain.cardterminal.EgkHandle;
import de.health.service.cetp.domain.eventservice.Subscription;
import de.health.service.cetp.domain.eventservice.card.Card;
import de.health.service.cetp.domain.eventservice.card.CardType;
import de.health.service.cetp.domain.eventservice.card.CardsResponse;
import de.health.service.cetp.domain.eventservice.cardTerminal.CardTerminal;
import de.health.service.cetp.domain.fault.CetpFault;
import de.health.service.config.api.UserRuntimeConfig;
import health.ere.ps.service.cetp.mapper.card.CardTypeMapper;
import health.ere.ps.service.cetp.mapper.card.CardsResponseMapper;
import health.ere.ps.service.cetp.mapper.status.StatusMapper;
import health.ere.ps.service.cetp.mapper.subscription.SubscriptionMapper;
import health.ere.ps.service.cetp.mapper.subscription.SubscriptionResultMapper;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.xml.ws.Holder;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;

import javax.xml.datatype.XMLGregorianCalendar;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

import static de.health.service.cetp.SubscriptionManager.FAILED;

@ApplicationScoped
public class KonnektorClient implements IKonnektorClient {

    private final Object emptyInput = new Object();

    private final MultiConnectorServicesProvider servicePortProvider;
    private final SubscriptionResultMapper subscriptionResultMapper;
    private final CardsResponseMapper cardsResponseMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final CardTypeMapper cardTypeMapper;
    private final StatusMapper statusMapper;

    @Inject
    public KonnektorClient(
        MultiConnectorServicesProvider servicePortProvider,
        SubscriptionResultMapper subscriptionResultMapper,
        SubscriptionMapper subscriptionMapper,
        CardsResponseMapper cardsResponseMapper,
        CardTypeMapper cardTypeMapper,
        StatusMapper statusMapper
    ) {
        this.servicePortProvider = servicePortProvider;
        this.subscriptionResultMapper = subscriptionResultMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.cardsResponseMapper = cardsResponseMapper;
        this.cardTypeMapper = cardTypeMapper;
        this.statusMapper = statusMapper;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    public boolean isInitialized() {
        return servicePortProvider.isInitialized();
    }

    @Override
    public List<Subscription> getSubscriptions(UserRuntimeConfig runtimeConfig) throws CetpFault {
        ContextType context = servicePortProvider.getContextType(runtimeConfig);
        EventServicePortType eventService = servicePortProvider.getEventServicePortType(runtimeConfig);
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
        ContextType context = servicePortProvider.getContextType(runtimeConfig);
        EventServicePortType eventService = servicePortProvider.getEventServicePortType(runtimeConfig);

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
        ContextType context = servicePortProvider.getContextType(runtimeConfig);

        EventServicePortType eventService = servicePortProvider.getEventServicePortType(runtimeConfig);
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
        ContextType context = servicePortProvider.getContextType(runtimeConfig);
        EventServicePortType eventService = servicePortProvider.getEventServicePortType(runtimeConfig);
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

    @Override
    public List<Card> getCards(UserRuntimeConfig runtimeConfig, CardType cardType) throws CetpFault {
        ContextType context = servicePortProvider.getContextType(runtimeConfig);
        EventServicePortType eventService = servicePortProvider.getEventServicePortType(runtimeConfig);
        GetCards parameter = new GetCards();
        parameter.setContext(context);
        parameter.setCardType(cardTypeMapper.toSoap(cardType));
        try {
            CardsResponse cardsResponse = cardsResponseMapper.toDomain(eventService.getCards(parameter));
            return cardsResponse.getCards();
        } catch (FaultMessage faultMessage) {
            throw new CetpFault(faultMessage.getMessage());
        }
    }

    @Override
    public List<CardTerminal> getCardTerminals(UserRuntimeConfig runtimeConfig) throws CetpFault {
        throw new NotImplementedException("Not implemented");
    }

    public EgkHandle getEgkHandle(UserRuntimeConfig userRuntimeConfig, String insurantId) throws CetpFault {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public CertificateInfo getSmcbX509Certificate(UserRuntimeConfig userRuntimeConfig, String smcbHandle) throws CetpFault {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public String getTelematikId(UserRuntimeConfig userRuntimeConfig, String smcbHandle) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public String getSmcbHandle(UserRuntimeConfig userRuntimeConfig) throws CetpFault {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public String getKvnr(UserRuntimeConfig userRuntimeConfig, String egkHandle) throws CetpFault {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public X509Certificate getHbaX509Certificate(UserRuntimeConfig userRuntimeConfig, String hbaHandle) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public Pair<Boolean, String> ejectEgkCard(UserRuntimeConfig userRuntimeConfig, EgkHandle egkHandle, String timeout) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public String requestEgkCard(UserRuntimeConfig userRuntimeConfig, EgkHandle egkHandle, String timeout) {
        throw new NotImplementedException("Not implemented");
    }
}
