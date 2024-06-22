package health.ere.ps.service.cetp;

import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.v7.GetSubscription;
import de.gematik.ws.conn.eventservice.v7.GetSubscriptionResponse;
import de.gematik.ws.conn.eventservice.v7.SubscriptionType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.xml.ws.Holder;
import org.apache.commons.lang3.tuple.Triple;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

import static health.ere.ps.service.cetp.config.KonnektorConfig.FAILED;

@ApplicationScoped
public class KonnektorClient {

    public static final String CARD_INSERTED_TOPIC = "CARD/INSERTED";

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;

    public List<SubscriptionType> getSubscriptions(RuntimeConfig runtimeConfig) throws FaultMessage {
        EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);
        GetSubscription getSubscriptionRequest = new GetSubscription();
        getSubscriptionRequest.setMandantWide(false);
        GetSubscriptionResponse subscriptionResponse = eventService.getSubscription(getSubscriptionRequest);
        return subscriptionResponse.getSubscriptions().getSubscription();
    }

    public Triple<Status, String, String> subscribeToKonnektor(
        RuntimeConfig runtimeConfig,
        String cetpHost
    ) throws FaultMessage {
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);

        EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);
        SubscriptionType subscriptionType = new SubscriptionType();

        subscriptionType.setEventTo(cetpHost);
        subscriptionType.setTopic(CARD_INSERTED_TOPIC);
        Holder<Status> status = new Holder<>();
        Holder<String> subscriptionId = new Holder<>();
        Holder<XMLGregorianCalendar> terminationTime = new Holder<>();

        eventService.subscribe(context, subscriptionType, status, subscriptionId, terminationTime);

        return Triple.of(status.value, subscriptionId.value, terminationTime.value.toString());
    }

    public Status unsubscribeFromKonnektor(
        RuntimeConfig runtimeConfig,
        String subscriptionId,
        String cetpHost,
        boolean forceCetp
    ) throws FaultMessage {
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
        EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);
        if (forceCetp) {
            return eventService.unsubscribe(context, null, cetpHost);
        } else {
            if (subscriptionId == null || subscriptionId.startsWith(FAILED)) {
                Status status = new Status();
                status.setResult("Previous subscription is not found");
                return status;
            }
            return eventService.unsubscribe(context, subscriptionId, null);
        }
    }
}
